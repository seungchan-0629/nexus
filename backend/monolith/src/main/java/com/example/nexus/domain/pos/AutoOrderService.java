package com.example.nexus.domain.pos;

import com.example.nexus.common.enums.OrdersStatus;
import com.example.nexus.common.enums.OrdersType;
import com.example.nexus.common.exception.BaseException;
import com.example.nexus.common.model.BaseResponseStatus;
import com.example.nexus.domain.orders.OrdersItemRepository;
import com.example.nexus.domain.orders.OrdersRepository;
import com.example.nexus.domain.orders.model.Orders;
import com.example.nexus.domain.orders.model.OrdersItem;
import com.example.nexus.domain.product.ProductRepository;
import com.example.nexus.domain.product.model.Product;
import com.example.nexus.domain.store.StoreInventoryRepository;
import com.example.nexus.domain.store.StoreRepository;
import com.example.nexus.domain.store.model.Store;
import com.example.nexus.domain.store.model.StoreInventory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoOrderService {

    private final ChatClient.Builder chatClientBuilder;
    private final StoreRepository storeRepository;
    private final StoreInventoryRepository storeInventoryRepository;
    private final OrdersRepository ordersRepository;
    private final OrdersItemRepository ordersItemRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void generateAutoOrder(Long userIdx) {
        // 1. 매장 조회
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND_DATA));

        // 2. 현재 재고 조회
        List<StoreInventory> inventoryList = storeInventoryRepository.findByStoreIdx(store.getIdx());
        if (inventoryList.isEmpty()) {
            return;
        }

        // 3. 프롬프트 생성
        String prompt = buildPrompt(store, inventoryList);

        // 4. OpenAI 호출
        ChatClient chatClient = chatClientBuilder.build();
        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 5. AI 응답 파싱 → 발주서 저장
        saveAutoOrder(store, aiResponse);
    }

    private String buildPrompt(Store store, List<StoreInventory> inventoryList) {
        StringBuilder sb = new StringBuilder();

        sb.append("당신은 프랜차이즈 매장의 재고 관리 전문가입니다.\n");
        sb.append("아래 매장의 현재 재고를 분석하여 발주가 필요한 품목과 적정 수량을 추천해주세요.\n");
        sb.append("추천 시 다음 요소도 함께 고려하세요:\n");
        sb.append("- 매장 지역의 계절/시즌 특성 (여름철 음료 수요 증가, 겨울철 따뜻한 메뉴 등)\n");
        sb.append("- 해당 시기의 이벤트/행사 가능성 (명절, 시험기간, 축제, 연휴 등)\n");
        sb.append("- 신메뉴/트렌드 품목의 수요 변화 가능성\n");
        sb.append("- 요일별 매출 패턴 (주말 vs 평일 수요 차이)\n\n");

        // 요일 정보
        LocalDate today = LocalDate.now();
        DayOfWeek dow = today.getDayOfWeek();
        sb.append(String.format("[마감일] %s (%s)\n", today, dow.name()));

        // 주말 배송 불가 고려
        if (dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY) {
            sb.append("[주의] 주말에는 본사 배송이 불가합니다. 월요일까지 버틸 수 있도록 넉넉하게 발주해주세요.\n");
        }

        sb.append(String.format("\n[매장명] %s\n", store.getStoreName()));
        sb.append(String.format("[매장 위치] %s\n", store.getAddress()));
        sb.append("\n[현재 재고 현황]\n");
        sb.append("상품명 | 현재수량 | 최소재고 | 최대재고 | 단위\n");

        for (StoreInventory inv : inventoryList) {
            Product p = inv.getProduct();
            sb.append(String.format("%s | %d | %d | %d | %s\n",
                    p.getProductName(),
                    inv.getCount(),
                    p.getMinStock(),
                    p.getMaxStock(),
                    p.getProductUnit()));
        }

        sb.append("\n[규칙]\n");
        sb.append("- 현재수량이 최소재고 이하인 품목은 반드시 발주에 포함\n");
        sb.append("- 발주수량은 최대재고 - 현재수량을 기본으로 하되, 상황에 맞게 조정\n");
        sb.append("- 현재수량이 충분한 품목은 발주하지 마세요\n");
        sb.append("\n[응답 형식] 반드시 아래 JSON만 출력하세요. 다른 텍스트 없이 JSON만:\n");
        sb.append("{\n");
        sb.append("  \"reason\": \"발주 추천 사유 (2~3문장으로 구체적으로: 어떤 품목이 왜 부족한지, 요일/주말 고려사항 등 포함)\",\n");
        sb.append("  \"items\": [{\"productIdx\": 숫자, \"productName\": \"상품명\", \"quantity\": 숫자}]\n");
        sb.append("}\n");
        sb.append("발주할 품목이 없으면 {\"reason\": \"\", \"items\": []}을 반환하세요.\n");

        return sb.toString();
    }

    @Transactional
    public void saveAutoOrder(Store store, String aiResponse) {
        try {
            // JSON 파싱
            String json = extractJson(aiResponse);
            AutoOrderResponse response = objectMapper.readValue(json, AutoOrderResponse.class);

            if (response.items() == null || response.items().isEmpty()) {
                log.info("AI 판단: 발주 필요 품목 없음 (store={})", store.getStoreName());
                return;
            }

            // Product 일괄 조회 (중복 DB 호출 방지)
            List<Long> productIds = response.items().stream()
                    .map(AutoOrderItem::productIdx)
                    .toList();
            Map<Long, Product> productMap = productRepository.findAllById(productIds).stream()
                    .collect(Collectors.toMap(Product::getIdx, p -> p));

            // 총 가격 계산
            long totalPrice = 0;
            for (AutoOrderItem item : response.items()) {
                Product product = productMap.get(item.productIdx());
                if (product != null) {
                    totalPrice += (long) product.getUnitPrice() * item.quantity();
                }
            }

            // Orders 생성 (reason 포함)
            Orders orders = ordersRepository.save(Orders.builder()
                    .price(totalPrice)
                    .ordersType(OrdersType.AUTO)
                    .ordersStatus(OrdersStatus.WAITING)
                    .isDanger(false)
                    .reason(response.reason())
                    .createdAt(LocalDateTime.now())
                    .store(store)
                    .build());

            // OrdersItem 저장
            for (AutoOrderItem item : response.items()) {
                Product product = productMap.get(item.productIdx());
                if (product != null) {
                    ordersItemRepository.save(OrdersItem.builder()
                            .count(item.quantity())
                            .product(product)
                            .orders(orders)
                            .build());
                }
            }

            log.info("AI 자동 발주서 생성 완료: store={}, reason={}, items={}",
                    store.getStoreName(), response.reason(), response.items().size());

        } catch (Exception e) {
            log.error("AI 응답 파싱 실패: {}", aiResponse, e);
            throw new BaseException(BaseResponseStatus.REQUEST_ERROR);
        }
    }

    private String extractJson(String response) {
        // AI 응답에서 JSON 객체 부분만 추출
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start == -1 || end == -1) {
            return "{\"reason\": \"\", \"items\": []}";
        }
        return response.substring(start, end + 1);
    }

    private record AutoOrderResponse(String reason, List<AutoOrderItem> items) {}

    private record AutoOrderItem(Long productIdx, String productName, int quantity) {}
}
