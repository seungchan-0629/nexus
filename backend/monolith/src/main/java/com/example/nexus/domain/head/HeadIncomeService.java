package com.example.nexus.domain.head;

import com.example.nexus.common.model.PageResponse;
import com.example.nexus.domain.head.model.HeadIncome;
import com.example.nexus.domain.head.model.HeadIncomeDto;
import com.example.nexus.domain.orders.model.Orders;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class HeadIncomeService {

    private final HeadIncomeRepository headIncomeRepository;

//    public List<HeadIncomeDto.UnpaidRes> findUnpaidSettlement (Long storeIdx) {
//        List<HeadIncomeDto.UnpaidRes> unpaidResList = new ArrayList<>();
//        List<HeadIncome> headIncomeList = headIncomeRepository.findAllByStoreIdx(storeIdx);
//
//        for (HeadIncome headIncome : headIncomeList) {
//            if (!headIncome.getStatus()) {
//                HeadIncomeDto.UnpaidRes resDto = HeadIncomeDto.UnpaidRes.builder()
//                        .idx(headIncome.getIdx())
//                        .status(headIncome.getStatus())
//                        .price(headIncome.getPrice())
//                        .storeIdx(headIncome.getStore().getIdx())
//                        .build();
//                unpaidResList.add(resDto);
//            }
//        }
//        return unpaidResList;
//    }

    // ordersIdx 로 HeadIncome을 찾기
    public HeadIncomeDto.FindHeadIncomeRes findByOrdersIdx(Long ordersIdx) {
        HeadIncome headIncome = headIncomeRepository.findByOrdersIdx(ordersIdx);
        if (headIncome != null) {
            HeadIncomeDto.FindHeadIncomeRes resDto = HeadIncomeDto.FindHeadIncomeRes.builder()
                    .idx(headIncome.getIdx())
                    .price(headIncome.getPrice())
                    .paid(headIncome.getStatus())
                    .storeIdx(headIncome.getStore().getIdx())
                    .ordersIdx(headIncome.getOrders().getIdx())
                    .build();
            return resDto;
        }
        return null;
    }


    public List<HeadIncomeDto.ListRes> getIncomeList(
            String storeName, Boolean status, LocalDate startDate, LocalDate endDate) {

        List<HeadIncome> incomes = headIncomeRepository.findByFilters(storeName, status, startDate, endDate);

        List<HeadIncomeDto.ListRes> result = new ArrayList<>();

        for (HeadIncome income : incomes) {

            LocalDate orderDate = LocalDate.now();
            if (income.getOrders() != null && income.getOrders().getCreatedAt() != null) {
                orderDate = income.getOrders().getCreatedAt().toLocalDate();
            }

            HeadIncomeDto.ListRes dto = HeadIncomeDto.ListRes.builder()
                    .headIncomeIdx(income.getIdx())
                    .storeIdx(income.getStore().getIdx())
                    .storeName(income.getStore().getStoreName())
                    .periodStart(orderDate.withDayOfMonth(1))
                    .periodEnd(orderDate.withDayOfMonth(orderDate.lengthOfMonth()))
                    .totalPrice(income.getPrice())
                    .status(income.getStatus())
                    .settlementIdx(income.getSettlementIdx())
                    .orderCount(1)
                    .build();

            result.add(dto);
        }

        return result;
    }

    public HeadIncome findById(Long idx) {
        return headIncomeRepository.findById(idx).orElse(null);
    }

    // 본사 정산 관리 - 가맹점별 월 정산 요약 페이징 조회
    @Transactional(readOnly = true)
    public PageResponse<HeadIncomeDto.StoreSettlementRes> getStoreSettlementList(
            String storeName, int year, int month, int page, int size) {

        LocalDateTime start = LocalDateTime.of(LocalDate.of(year, month, 1), LocalTime.MIN);
        LocalDateTime end = start.plusMonths(1);

        Page<Object[]> resultPage = headIncomeRepository.findStoreSettlementByMonth(
                storeName, start, end, PageRequest.of(page, size)
        );

        LocalDate periodStart = start.toLocalDate();
        LocalDate periodEnd = end.toLocalDate().minusDays(1);

        List<HeadIncomeDto.StoreSettlementRes> content = resultPage.getContent().stream()
                .map(row -> HeadIncomeDto.StoreSettlementRes.builder()
                        .storeIdx(((Number) row[0]).longValue())
                        .storeName((String) row[1])
                        .periodStart(periodStart)
                        .periodEnd(periodEnd)
                        .orderCount(((Number) row[2]).intValue())
                        .totalPrice(((Number) row[3]).longValue())
                        .status((Boolean) row[4])
                        .build())
                .toList();

        return PageResponse.<HeadIncomeDto.StoreSettlementRes>builder()
                .content(content)
                .number(resultPage.getNumber())
                .size(resultPage.getSize())
                .totalPages(resultPage.getTotalPages())
                .totalElements(resultPage.getTotalElements())
                .build();
    }

    // 본사 정산 관리 - 상단 카드 요약 조회
    @Transactional(readOnly = true)
    public HeadIncomeDto.HeadSettlementSummaryRes getSettlementSummary(String storeName, int year, int month) {
        LocalDateTime start = LocalDateTime.of(LocalDate.of(year, month, 1), LocalTime.MIN);
        LocalDateTime end = start.plusMonths(1);

        Long totalBillingAmount = headIncomeRepository.sumTotalBillingByMonth(storeName, start, end);

        return HeadIncomeDto.HeadSettlementSummaryRes.builder()
                .totalBillingAmount(totalBillingAmount != null ? totalBillingAmount : 0L)
                .totalStoreCount(0)
                .build();
    }
}
