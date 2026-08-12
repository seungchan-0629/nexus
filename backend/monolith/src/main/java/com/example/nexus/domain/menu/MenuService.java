package com.example.nexus.domain.menu;

import com.example.nexus.common.exception.BaseException;
import com.example.nexus.domain.menu.model.Menu;
import com.example.nexus.domain.menu.model.MenuCategory;
import com.example.nexus.domain.menu.model.MenuDto;
import com.example.nexus.domain.menu.model.MenuItem;
import com.example.nexus.domain.product.ProductRepository;
import com.example.nexus.domain.product.model.Product;
import com.example.nexus.event.KafkaTopics;
import com.example.nexus.event.MenuDomainEvent;
import com.example.nexus.event.MenuEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.nexus.common.model.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.menu-bucket}")
    private String menuBucket;

    @Value("${spring.cloud.aws.s3.enabled:true}")
    private boolean isS3Enabled;

    @Transactional(readOnly = true)
    public MenuDto.MenuPageRes list(MenuDto.MenuSearchPagingReq searchReq, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Menu> result = menuRepository.findMenusBySearch(searchReq.getKeyword(), searchReq.getCategoryIdx(), pageRequest);

        return MenuDto.MenuPageRes.from(result);
    }

    @Transactional(readOnly = true)
    public MenuDto.MenuItemListRes menuItemList(Long menuIdx) {
        Optional<Menu> res = menuRepository.findById(menuIdx);

        res.orElseThrow(() -> new BaseException(NOT_FOUND_MENU));

        if(res.isPresent()){
            Menu menu = res.get();
            return MenuDto.MenuItemListRes.from(menu);
        }
        return null;
    }

    public Map<String, String> getPresignedUrl(String fileName, long fileSize) {
        // 1. 경로 및 고유 파일명 생성 (기존 로직 유지)
        String path = createPath(fileName);


        // 🌟 1. 파일 크기 검사 (예: 5MB 제한)
        long maxSize = 5 * 1024 * 1024;
        if (fileSize > maxSize) {
            throw new BaseException(EXCEED_IMG_FILE_SIZE);
        }

        // 2. 환경별 스위치 검사 (개발 편의성)
        if (!isS3Enabled) {
            System.out.println("[DEV MODE] S3 통신을 건너뛰고 가짜 URL을 반환합니다.");
            return Map.of(
                    "url", "http://local-test-dummy.com/" + path,
                    "fileName", path
            );
        }

        // 2. S3 업로드 요청 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(menuBucket)
                .key(path)
                .contentLength(fileSize)
                .build();

        // 3. Pre-signed 요청 설정 (유효시간 분)
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObjectRequest)
                .build();

        // 4. URL 생성 및 반환
        String url = s3Presigner.presignPutObject(presignRequest).url().toString();

        return Map.of(
                "url", url,
                "fileName", path
        );
    }
    private String createPath(String fileName) {
        return UUID.randomUUID().toString() + "-nexus-" + fileName;
    }

    @Transactional
    public void menuRegister(MenuDto.MenuRegReq dto) {
        String newFilePath = dto.getImgPath();

        try {
            // 메뉴명 존재 확인
            Optional<Menu> existingMenu = menuRepository.findByMenuName(dto.getMenuName());
            if (existingMenu.isPresent()) {
                if (!existingMenu.get().isDeleted()) {
                    throw new BaseException(DUPLICATE_MENU_NAME);
                } else {
                    throw new BaseException(DELETED_MENU_NAME);
                }
            }

            // 카테고리 존재 여부 확인
            MenuCategory category = menuCategoryRepository.findById(dto.getMenuCategoryIdx())
                    .orElseThrow(() -> new BaseException(NOT_FOUND_CATEGORY));


            List<Long> productIds = Collections.emptyList();
            List<Product> products = Collections.emptyList();

            if (dto.getMenuItemList() != null && !dto.getMenuItemList().isEmpty()) {
                productIds = dto.getMenuItemList().stream()
                        .map(MenuDto.MenuItemReq::getProductIdx).toList();
                products = productRepository.findAllById(productIds);
            }


            Menu menu = dto.toEntity(category, products);
            Menu saved = menuRepository.save(menu);

            List<MenuEvent.MenuItemEvent> items = menu.getMenuItemList().stream()
                    .map(mi -> new MenuEvent.MenuItemEvent(
                            mi.getIdx(),
                            mi.getQuantity(),
                            mi.getMenuUnit(),
                            mi.getProduct().getIdx()
                    )).toList();

            // kafka 이벤트 발생
            MenuEvent event = new MenuEvent(
                    saved.getIdx(),
                    saved.getMenuName(),
                    saved.getPrice(),
                    category.getIdx(),
                    category.getMenuCategoryName(),
                    saved.getImgPath(),
                    saved.isDeleted(),
                    items
            );

            applicationEventPublisher.publishEvent(new MenuDomainEvent(event, KafkaTopics.MENU_CREATED));

        } catch (Exception e) {
            // 등록 중 에러가 나면 DB는 롤백되지만, 이미 업로드된 파일은 좀비가 되므로 삭제 처리
            if (newFilePath != null && !newFilePath.isEmpty() && !"theVenti_logo.png".equals(newFilePath)) {
                try {
                    deleteS3Object(newFilePath);
                } catch (Exception ignored) {
                    // 롤백 중 S3 삭제 실패는 무시하여 원래 예외를 보존합니다.
                }
            }

            // 예외 다시 던지기
            if (e instanceof BaseException) throw (BaseException) e;
            throw new RuntimeException(e);
        }

    }

    @Transactional
    public void menuUpdate(Long menuIdx, MenuDto.MenuRegReq dto) {
        String newFilePath = dto.getImgPath();
        String oldFilePath = "";
        String defaultImage = "theVenti_logo.png";

        try{
            Menu menu = menuRepository.findByIdxAndIsDeletedFalse(menuIdx)
                    .orElseThrow(() -> new BaseException(NOT_FOUND_MENU));

            // 카테고리 존재 여부 확인
            MenuCategory category = menuCategoryRepository.findById(dto.getMenuCategoryIdx())
                    .orElseThrow(() -> new BaseException(NOT_FOUND_CATEGORY));

            oldFilePath = menu.getImgPath();

            // 메뉴명 중복 확인
            Optional<Menu> duplicateMenu = menuRepository.findByMenuNameAndIdxNot(dto.getMenuName(), menuIdx);
            if (duplicateMenu.isPresent()) {
                if (!duplicateMenu.get().isDeleted()) {
                    throw new BaseException(DUPLICATE_MENU_NAME);
                } else {
                    throw new BaseException(DELETED_MENU_NAME);
                }
            }

            // 제품 조회
            List<Long> productIds = Optional.ofNullable(dto.getMenuItemList()).orElseGet(Collections::emptyList).stream()
                    .map(MenuDto.MenuItemReq::getProductIdx).toList();
            List<Product> products = productRepository.findAllById(productIds);

            // 새 파일이 들어왔고, 기존 파일과 다를 경우에만 동기화 등록
            if (newFilePath != null && !newFilePath.equals(oldFilePath)
                    && oldFilePath != null && !oldFilePath.isBlank()
                    && !oldFilePath.equals(defaultImage)) { // 기존 파일이 기본 로고가 아닐 때만 삭제 등록

                String finalOldFile = oldFilePath;
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        deleteS3Object(finalOldFile);
                    }
                });
            }

            menu.update(dto.getMenuName(), dto.getPrice(), dto.getImgPath(), category);

            // 해당 메뉴에 재료 리스트(MenuItem) 비우기
            menu.getMenuItemList().clear();

            Map<Long, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getIdx, p -> p));

            for (MenuDto.MenuItemReq itemReq : Optional.ofNullable(dto.getMenuItemList()).orElseGet(Collections::emptyList)) {
                Product product = productMap.get(itemReq.getProductIdx());

                // 상품 찾아보기
                if (product == null) throw new BaseException(NOT_FOUND_PRODUCT);

                MenuItem newItem = itemReq.toEntity(menu, product);
                menu.getMenuItemList().add(newItem);
            }

            //  저장 결과를 새로운 리스트(savedItems)로 반드시 받아옵니다!
            List<MenuItem> savedItems = menuItemRepository.saveAllAndFlush(menu.getMenuItemList());

            List<MenuEvent.MenuItemEvent> items = savedItems.stream()
                    .map(mi -> new MenuEvent.MenuItemEvent(
                            mi.getIdx(),
                            mi.getQuantity(),
                            mi.getMenuUnit(),
                            mi.getProduct().getIdx()
                    )).toList();

            MenuEvent event = new MenuEvent(
                    menu.getIdx(),
                    menu.getMenuName(),
                    menu.getPrice(),
                    category.getIdx(),
                    category.getMenuCategoryName(),
                    menu.getImgPath(),
                    menu.isDeleted(),
                    items
            );

            applicationEventPublisher.publishEvent(new MenuDomainEvent(event, KafkaTopics.MENU_UPDATED));

        }catch (Exception e) {
            if (newFilePath != null && !newFilePath.isEmpty()
                    && !newFilePath.equals(oldFilePath)
                    && !newFilePath.equals(defaultImage)) { // 새 파일이 기본 로고가 아닐 때만 롤백 삭제

                try {
                    deleteS3Object(newFilePath);
                } catch (Exception ignored) {
                }
            }

            if (e instanceof BaseException) throw (BaseException) e;
            throw new RuntimeException(e);
        }
    }

    private void deleteS3Object(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        // 🌟 3. 스위치가 꺼져있으면 삭제 로직 건너뛰기
        if (!isS3Enabled) {
            System.out.println("[연습 모드] S3 파일 삭제를 건너뜁니다. (Key: " + key + ")");
            return;
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(menuBucket)
                    .key(key)
                    .build());
        } catch (Exception e) {
            // S3 삭제 실패 시 로그를 남기거나 무시하여 원래 예외가 전파되도록 합니다.
        }
    }

    @Transactional
    public void menuDelete(Long menuIdx) {
        // 메뉴 삭제 여부 확인
        Menu menu = menuRepository.findByIdxAndIsDeletedFalse(menuIdx)
                .orElseThrow(() -> new BaseException(NOT_FOUND_MENU));

        menu.deleteTrue();

        // kafka 이벤트 발생
        MenuEvent event = new MenuEvent(
                menu.getIdx(),
                menu.getMenuName(),
                menu.getPrice(),
                menu.getMenuCategory() != null ? menu.getMenuCategory().getIdx() : null,
                menu.getMenuCategory() != null ? menu.getMenuCategory().getMenuCategoryName() : null,
                menu.getImgPath(),
                true,
                null
        );

        applicationEventPublisher.publishEvent(new MenuDomainEvent(event, KafkaTopics.MENU_DELETED));
    }

    @Transactional(readOnly = true)
    public List<MenuDto.MenuCategoryRes> menucategory() {
        List<MenuCategory> res = menuCategoryRepository.findAllByIsDeletedFalse();
        List<MenuDto.MenuCategoryRes> result = new ArrayList<>();

        for (MenuCategory data: res){
            result.add(MenuDto.MenuCategoryRes.from(data));
        }
        return result;
    }

    @Transactional
    public void menucategoryReg(MenuDto.MenuCategoryRegReq dto) {
        Optional<MenuCategory> existing = menuCategoryRepository.findByMenuCategoryName(dto.getMenuCategoryName());

        if (existing.isPresent()) {
            MenuCategory category = existing.get();
            // 존재 여부 확인
            if (!category.isDeleted()) {
                throw new BaseException(DUPLICATE_CATEGORY_NAME);
            }
            category.recategory();
        } else {
            menuCategoryRepository.save(MenuCategory.builder()
                    .menuCategoryName(dto.getMenuCategoryName())
                    .build());
        }
    }

    @Transactional
    public void menuCategoryDelete(Long categoryIdx) {
        // 카테고리 존재 여부
        MenuCategory category = menuCategoryRepository.findById(categoryIdx)
                .orElseThrow(() -> new BaseException(NOT_FOUND_CATEGORY));

        // 메뉴에서 현재 카테고리 사용 여부 확인
        if (menuRepository.existsByMenuCategoryIdxAndIsDeletedFalse(categoryIdx)) {
            throw new BaseException(CATEGORY_IN_USE);
        }

        category.deleteTrue();
    }
}
