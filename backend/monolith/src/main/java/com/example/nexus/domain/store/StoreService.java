package com.example.nexus.domain.store;

import com.example.nexus.common.exception.BaseException;
import com.example.nexus.common.model.PageResponse;
import com.example.nexus.domain.store.model.Store;
import com.example.nexus.domain.store.model.StoreDto;
import com.example.nexus.domain.store.model.StoreInventory;
import com.example.nexus.domain.store.model.StoreInventoryDto;
import com.example.nexus.domain.user.UserRepository;
import com.example.nexus.domain.user.model.User;
import com.example.nexus.event.KafkaTopics;
import com.example.nexus.event.StoreDomainEvent;
import com.example.nexus.event.StoreEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import static com.example.nexus.common.model.BaseResponseStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    // URL을 발행해주는 핵심 도구
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.store-bucket}")
    private String storeBucket;

    @Value("${spring.cloud.aws.s3.enabled:true}")
    private boolean isS3Enabled;

    @Transactional
    public PageResponse<StoreInventoryDto.ListRes> listByStoreIdxPaged(Long storeIdx, int page, int size) {
        storeRepository.findById(storeIdx)
                .orElseThrow(() -> new BaseException(STORE_NOT_FOUND));

        storeInventoryRepository.bulkUpdateExpiryStatus();

        Page<Long> productPage = storeInventoryRepository.findPagedProductIdsByStoreIdx(storeIdx, PageRequest.of(page, size));
        List<Long> productIds = productPage.getContent();

        List<StoreInventory> lots = storeInventoryRepository.findByStoreIdxAndProductIds(storeIdx, productIds);

        Map<Long, Integer> productOrder = new HashMap<>();

        for (int i = 0; i < productIds.size(); i++) {
            productOrder.put(productIds.get(i), i);
        }

        List<StoreInventoryDto.ListRes> content = lots.stream()
                .map(StoreInventoryDto.ListRes::from)
                .sorted(Comparator
                        .comparingInt((StoreInventoryDto.ListRes dto) -> productOrder.getOrDefault(dto.getProductIdx(), Integer.MAX_VALUE))
                        .thenComparing(StoreInventoryDto.ListRes::getManufacturedDate))
                .toList();

        return PageResponse.<StoreInventoryDto.ListRes>builder()
                .content(content)
                .number(productPage.getNumber())
                .size(productPage.getSize())
                .totalPages(productPage.getTotalPages())
                .totalElements(productPage.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public StoreDto.StorePageRes storeList(StoreDto.StoreSearchPagingReq req, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Store> result;

        String status = (req.getStatus() != null) ? req.getStatus().trim(): "";
        String keyword = (req.getKeyword() != null) ? req.getKeyword().trim() : "";
        boolean hasKeyword = !keyword.isEmpty();

        if ("ACTIVE".equals(status)) {
            result = hasKeyword
                    ? storeRepository.findByStatusAndKeyword(false, keyword, pageRequest)
                    : storeRepository.findByIsDeletedFalseOrderByCreatedAtDesc(pageRequest);
        } else if ("CLOSED".equals(status)) {
            result = hasKeyword
                    ? storeRepository.findByStatusAndKeyword(true, keyword, pageRequest)
                    : storeRepository.findByIsDeletedTrueOrderByClosedAtDesc(pageRequest);
        } else {
            result = hasKeyword
                    ? storeRepository.findByKeywordAll(keyword,pageRequest)
                    : storeRepository.findAllCustom(pageRequest);
        }
        return StoreDto.StorePageRes.from(result);
    }

    // 리스트 total 리스트
    @Transactional(readOnly = true)
    public StoreDto.StoreTotalRes storeTotalList() {
        Long activeCount = storeRepository.countByIsDeletedFalse();
        Long closedCount = storeRepository.countByIsDeletedTrue();

        return StoreDto.StoreTotalRes.builder()
                .totalCount(activeCount+closedCount)
                .activeCount(activeCount)
                .closedCount(closedCount)
                .build();
    }

    // 선택 연도 1월~12월 월별 입점/폐점 추이 집계
    @Transactional(readOnly = true)
    public StoreDto.MonthlyTrendRes getMonthlyTrend(Integer year) {
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");

        // year가 없으면 올해를 기본값으로 사용
        int targetYear = (year != null) ? year : YearMonth.now().getYear();

        // 선택 연도 1월 ~ 12월 구간 (start = 선택 연도 1월 1일 00:00)
        YearMonth start = YearMonth.of(targetYear, 1);
        LocalDateTime since = start.atDay(1).atStartOfDay();

        // 12개월 라벨을 순서대로 만들고, 각 월의 입점/폐점 카운트를 0으로 초기화
        // LinkedHashMap을 써서 라벨 순서(과거 -> 현재)를 그대로 유지
        List<String> labels = new ArrayList<>();
        Map<String, Long> openedMap = new LinkedHashMap<>();
        Map<String, Long> closedMap = new LinkedHashMap<>();
        for (int i = 0; i < 12; i++) {
            String key = start.plusMonths(i).format(monthFmt);
            labels.add(key);
            openedMap.put(key, 0L);
            closedMap.put(key, 0L);
        }

        // 입점: 등록일(createdAt)을 월 단위로 묶어 카운트
        for (LocalDateTime createdAt : storeRepository.findCreatedAtSince(since)) {
            String key = YearMonth.from(createdAt).format(monthFmt);
            // 구간 경계 밖 데이터는 무시 (containsKey로 방어)
            openedMap.computeIfPresent(key, (k, v) -> v + 1);
        }

        // 폐점: 폐점일(closedAt)을 월 단위로 묶어 카운트
        for (LocalDateTime closedAt : storeRepository.findClosedAtSince(since)) {
            String key = YearMonth.from(closedAt).format(monthFmt);
            closedMap.computeIfPresent(key, (k, v) -> v + 1);
        }

        return StoreDto.MonthlyTrendRes.builder()
                .labels(labels)
                .openedCounts(new ArrayList<>(openedMap.values()))
                .closedCounts(new ArrayList<>(closedMap.values()))
                .build();
    }

    public List<StoreDto.StoreSearchRes> searchByStoreName(StoreDto.StoreSearchReq reqDto) {
        String keyword = reqDto.getKeyword() != null ? reqDto.getKeyword().trim() : "";
        List<Store> res = storeRepository.findByStoreNameContainingIgnoreCase(keyword);

        return res.stream().map(StoreDto.StoreSearchRes::from).toList();
    }

    @Transactional(readOnly = true)
    public StoreDto.StoreDetailListRes storeDetailList(Long storeIdx) {
        Optional<Store> res = storeRepository.findById(storeIdx);

        if(res.isPresent()){
            Store data = res.get();
            return StoreDto.StoreDetailListRes.from(data);
        }
        return null;
    }

    @Transactional
    public void storeReg(StoreDto.StoreRegReq dto) {
        String newFilePath = dto.getFilePath();

        try{
            // 이메일을 통한 가맹점 점주 체크
            User owner = userRepository.findByEmail(dto.getOwnerEmail()).orElseThrow(
                    ()-> new BaseException(NOT_FOUND_USER));

            // 점주 중복 체크
            storeRepository.findByUserIdx(owner.getIdx())
                    .ifPresent(s-> {
                        throw new BaseException(ALREADY_HAS_STORE);
                    });

            // 가맹점 이름 중복 체크
            storeRepository.findByStoreName(dto.getStoreName())
                    .ifPresent(s -> {
                        throw new BaseException(STORE_NAME_ALREADY_EXISTS);
                    });

            // 가맹점 사업자 번호 중복 체크
            storeRepository.findByBusiness(dto.getBusiness())
                    .ifPresent(s -> {
                        throw new BaseException(BUSINESS_NUMBER_ALREADY_EXISTS);
                    });

            Store store = Store.builder()
                    .storeName(dto.getStoreName())
                    .postcode(dto.getPostcode())
                    .address(dto.getAddress())
                    .addressDetail(dto.getAddressDetail())
                    .filePath(dto.getFilePath())
                    .business(dto.getBusiness())
                    .createdAt(LocalDateTime.now())
                    .isDeleted(false)
                    .user(owner)
                    .build();

            // kafka 이벤트 발생
            Store saved = storeRepository.save(store);

            StoreEvent event = new StoreEvent(
                    saved.getIdx(),
                    saved.getStoreName(),
                    saved.isDeleted(),
                    saved.getUser().getIdx(),
                    saved.getAddress(),
                    saved.getAddressDetail()
            );

            applicationEventPublisher.publishEvent(new StoreDomainEvent(event, KafkaTopics.STORE_CREATED));

        } catch (Exception e) {
            // 5. 예외 발생 시 S3 롤백 삭제
            // 트랜잭션 도중 에러가 나면 DB는 롤백되지만, S3에 업로드된 파일은 남으므로 삭제 처리
            if (newFilePath != null && !newFilePath.isEmpty()) {
                try {
                    deleteS3Object(newFilePath);
                } catch (Exception ignored) {
                    // 롤백 중 S3 삭제 실패는 무시하여 원래 예외를 보존합니다.
                }
            }

            // 6. 예외 다시 던지기
            if (e instanceof BaseException) throw (BaseException) e;
            throw new RuntimeException(e);
        }
    }

    // Presigned URL을 발급 받는 로직
    public Map<String, String> getPresignedUrl(String fileName, long fileSize) {

        // 1. 경로 및 고유 파일명 생성 (기존 로직 유지)
        String path = createPath(fileName);

        // 🌟 1. 파일 크기 검사 (예: 10MB 제한)
        long maxSize = 10 * 1024 * 1024;
        if (fileSize > maxSize) {
            throw new BaseException(EXCEED_PDF_FILE_SIZE);
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
                .bucket(storeBucket)
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
                "fileName", path // 프론트에서 나중에 DB에 저장할 때 필요하니까 같이 보내주는 게 좋아요!
        );
    }

    private String createPath(String fileName) {
        return UUID.randomUUID().toString() + "-nexus-" + fileName;
    }

    @Transactional
    public void storeUpdate(Long storeIdx, StoreDto.StoreUpdateReq dto) {
        String newFilePath = dto.getFilePath();
        String oldFilePath = "";

        try {
            Store store = storeRepository.findById(storeIdx)
                    .orElseThrow(() -> new BaseException(STORE_NOT_FOUND));

            oldFilePath = store.getFilePath();

            if (store.isDeleted()) throw new BaseException(STORE_ALREADY_CLOSED);

            if (!store.getStoreName().equals(dto.getStoreName())) {
                storeRepository.findByStoreName(dto.getStoreName())
                        .ifPresent(s -> { throw new BaseException(STORE_NAME_ALREADY_EXISTS); });
            }

            User owner = store.getUser();
            if (owner == null || owner.isDeleted()) throw new BaseException(NOT_FOUND_USER);

            if (newFilePath != null && !newFilePath.equals(oldFilePath) && oldFilePath != null && !oldFilePath.isBlank()) {
                String finalOldFile = oldFilePath;
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        deleteS3Object(finalOldFile);
                    }
                });
            }

            store.update(dto);
            owner.updateOwner(dto.getOwnerName(), dto.getOwnerEmail());

            // kafka 이벤트 발생
            StoreEvent event = new StoreEvent(
                    store.getIdx(),
                    store.getStoreName(),
                    store.isDeleted(),
                    store.getUser().getIdx(),
                    store.getAddress(),
                    store.getAddressDetail()
            );

            applicationEventPublisher.publishEvent(new StoreDomainEvent(event, KafkaTopics.STORE_UPDATED));

        } catch (Exception e) {
            if (newFilePath != null && !newFilePath.isEmpty() && !newFilePath.equals(oldFilePath)) {
                try {
                    deleteS3Object(newFilePath);
                } catch (Exception ignored) {
                    // 롤백 중 S3 삭제 실패는 무시하여 원래 예외를 보존합니다.
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
                    .bucket(storeBucket)
                    .key(key)
                    .build());
        } catch (Exception e) {
            // S3 삭제 실패 시 로그를 남기거나 무시하여 원래 예외가 전파되도록 합니다.
        }
    }

    public Long findStoreIdx(Long userIdx) {
        return storeRepository.findByUserIdx(userIdx).orElse(null).getIdx();
    }

    // 모든 재고 조회
    public List<StoreInventoryDto.ListRes> findAllStoreInventory() {
        List<StoreInventory> storeInventoryList = storeInventoryRepository.findAll();
        List<StoreInventoryDto.ListRes> resList = new ArrayList<>();
        for (StoreInventory storeInventory : storeInventoryList) {
            resList.add(StoreInventoryDto.ListRes.from(storeInventory));
        }
        return resList;
    }

    public Float countUseWarnedProductBeforeDueDate(YearMonth inputYearMonth) {

        LocalDateTime startDateTime = inputYearMonth.atDay(1).atStartOfDay();

        LocalDateTime endDateTime = inputYearMonth.atEndOfMonth().atTime(LocalTime.MAX);
        List<StoreInventory> logs = storeInventoryRepository.findAllByManufacturedDateBetween(startDateTime, endDateTime);

        int success = 0;
        int fail = 0;

        for (StoreInventory log : logs) {
            if (log.getCount() != 0) {
                fail++;
            } else {
                success++;
            }
        }


        return (float) (success / (success + fail));
    }

}
