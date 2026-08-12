package com.example.nexus.domain.store;

import com.example.nexus.common.exception.BaseException;
import com.example.nexus.common.model.BaseResponseStatus;
import com.example.nexus.common.model.PageResponse;
import com.example.nexus.domain.pos.PosOrdersItemRepository;
import com.example.nexus.domain.pos.model.PosOrdersItem;
import com.example.nexus.domain.pos.model.PosPay;
import com.example.nexus.domain.store.model.Store;
import com.example.nexus.domain.store.model.StoreIncomeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreIncomeService {

    private final StoreRepository storeRepository;
    private final StoreIncomeRepository storeIncomeRepository;
    private final PosOrdersItemRepository posOrdersItemRepository;

    @Transactional(readOnly = true)
    public StoreIncomeDto.TotalSettlementRes getSettlementData(Long userIdx, int year, int month, int page, int size) {
        // 가맹점 확인
        Store store = storeRepository.findByUserIdx(userIdx)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.STORE_NOT_FOUND));

        // 날짜 범위 계산
        LocalDateTime startOfMonth = LocalDateTime.of(LocalDate.of(year, month, 1), LocalTime.MIN);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        // 상단 매출 총액 조회
        Long totalAmount = storeIncomeRepository.sumTotalAmountByMonth(store.getIdx(), startOfMonth, endOfMonth);

        // 하단 결제 내역 페이징 조회
        Pageable pageable = PageRequest.of(page, size);
        Page<PosPay> payPage = storeIncomeRepository.findPayHistoryByMonth(store.getIdx(), startOfMonth, endOfMonth, pageable);

        // 엔티티 -> DTO 변환 (메뉴명 합치기 로직 포함)
        List<Long> payIdxList = payPage.getContent().stream().map(PosPay::getIdx).toList();

        // 주문 내역(Item)들을 한 번에 가져와서 메모리에서 그룹핑 (N+1 문제 방지)
        Map<Long, List<PosOrdersItem>> orderItemMap = payIdxList.isEmpty() ? Map.of() : posOrdersItemRepository
                .findByPosPay_IdxIn(payIdxList)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getPosPay().getIdx()));

        List<StoreIncomeDto.PayHistoryRes> content = payPage.getContent().stream()
                .map(pay -> {
                    List<PosOrdersItem> items = orderItemMap.getOrDefault(pay.getIdx(), List.of());
                    String menuNames = items.stream()
                            .map(item -> item.getMenu().getMenuName())
                            .collect(Collectors.joining(" , "));

                    return StoreIncomeDto.PayHistoryRes.builder()
                            .posPayIdx(pay.getIdx())
                            .menuNames(menuNames)
                            .payCount(items.size())
                            .paidDate(pay.getPaidAt().toLocalDate())
                            .payAmount(pay.getPayAmount())
                            .build();
                })
                .toList();

        // PageResponse 생성
        PageResponse<StoreIncomeDto.PayHistoryRes> historyPageResponse = PageResponse.<StoreIncomeDto.PayHistoryRes>builder()
                .content(content)
                .number(payPage.getNumber())
                .size(payPage.getSize())
                .totalPages(payPage.getTotalPages())
                .totalElements(payPage.getTotalElements())
                .build();

        // 최종 통합 DTO 반환
        return StoreIncomeDto.TotalSettlementRes.builder()
                .monthlyTotalAmount(totalAmount != null ? totalAmount : 0L)
                .payHistory(historyPageResponse)
                .build();
    }
}