package com.example.pos.kafka;

import com.example.pos.domain.menu.MenuItemRepository;
import com.example.pos.domain.menu.MenuRepository;
import com.example.pos.domain.product.ProductRepository;
import com.example.pos.domain.menu.model.Menu;
import com.example.pos.domain.menu.model.MenuItem;
import com.example.pos.domain.product.model.Product;
import com.example.pos.event.KafkaTopics;
import com.example.pos.event.MenuEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class MenuEventConsumer {
    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;
    private final ProductRepository productRepository;

    @KafkaListener(topics = KafkaTopics.MENU_CREATED, groupId = "pos-kafka-group")
    public void onMenuCreatedEvent(MenuEvent event){
        log.info("[Kafka] Pos - Menu CreatedEvent 수신: menuIdx={}, menuName={}", event.menuIdx(), event.menuName());
        try {
            upsert(event);
            log.info("[Kafka] Pos - Menu 정보 동기화 완료: menuIdx={}, menuName={}", event.menuIdx(), event.menuName());
        }catch (Exception e) {
            log.error("[Kafka] Pos - Menu 정보 동기화 중 에러 발생: menuIdx={}, menuName={}", event.menuIdx(), event.menuName(), e);
        }
    }

    @KafkaListener(topics = KafkaTopics.MENU_UPDATED, groupId = "pos-kafka-group")
    public void onMenuUpdatedEvent(MenuEvent event){
        log.info("[Kafka] Pos - Menu UpdatedEvent 수신: menuIdx={}, menuName={}", event.menuIdx(), event.menuName());
        try {
            upsert(event);
            log.info("[Kafka] Pos - Menu 정보 동기화 완료: menuIdx={}, menuName={}", event.menuIdx(), event.menuName());
        }catch (Exception e) {
            log.error("[Kafka] Pos - Menu 정보 동기화 중 에러 발생: menuIdx={}, menuName={}", event.menuIdx(), event.menuName(), e);
            // 에러 발생 시 DB에 잘못된 데이터가 저장되지 않도록 강제로 롤백(취소) 시킵니다!
            org.springframework.transaction.interceptor.TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    @KafkaListener(topics = KafkaTopics.MENU_DELETED, groupId = "pos-kafka-group")
    public void onMenuDeletedEvent(MenuEvent event){
        log.info("[Kafka] Pos - Menu DeletedEvent 수신: menuIdx={}, menuName={}", event.menuIdx(), event.menuName());
        try {
            upsert(event);
            log.info("[Kafka] Pos - Menu 정보 동기화 완료: menuIdx={}, menuName={}", event.menuIdx(), event.menuName());
        }catch (Exception e) {
            log.error("[Kafka] Pos - Menu 정보 동기화 중 에러 발생: menuIdx={}, menuName={}", event.menuIdx(), event.menuName(), e);
        }
    }

    @Transactional
    public void upsert(MenuEvent event){
        Menu menu = menuRepository.findById(event.menuIdx()).orElse(null);
        boolean isExist = (menu != null);

        if (isExist){
            log.info("[Kafka] 기존 메뉴 정보 수정 및 삭제: menuIdx={}, menuName={}", event.menuIdx(), event.menuName());
            menu.update(event.menuName(), event.price(), event.imgPath(), event.menuCategoryName(), event.isDeleted());
            menu.getMenuItemList().clear();
        }else{
            log.info("[Kafka] 신규 메뉴 정보 등록: menuIdx={}, menuName={}", event.menuIdx(), event.menuName());
            menu = Menu.builder()
                    .idx(event.menuIdx())
                    .menuName(event.menuName())
                    .price(event.price())
                    .imgPath(event.imgPath())
                    .isDeleted(event.isDeleted())
                    .menuCategoryName(event.menuCategoryName())
                    .isNew(true)
                    .build();
        }

        if (event.menuItems() != null) {
            Menu finalMenu = menu;
            List<MenuItem> items = event.menuItems().stream().map(itemEvent -> {
                Product product = productRepository.findById(itemEvent.productIdx())
                        .orElseThrow(() -> new RuntimeException("원자재 미존재: " + itemEvent.productIdx()));

                return MenuItem.builder()
                        .idx(itemEvent.idx())
                        .quantity(itemEvent.quantity())
                        .menuUnit(itemEvent.menuUnit())
                        .menu(finalMenu)
                        .product(product)
                        .isNew(itemEvent.idx() == null || !menuItemRepository.existsById(itemEvent.idx()))
                        .build();
            }).toList();
            menu.getMenuItemList().addAll(items);
        }

        menuRepository.save(menu);
    }
}