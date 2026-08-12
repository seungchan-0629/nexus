package com.example.nexus.domain.settlement;

import com.example.nexus.domain.head.HeadIncomeRepository;
import com.example.nexus.domain.head.model.HeadIncome;
import com.example.nexus.domain.settlement.model.Settlement;
import com.example.nexus.domain.settlement.model.SettlementDto;
import com.example.nexus.domain.user.model.AuthUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final HeadIncomeRepository headIncomeRepository;
    private final AfterBillingRepository afterBillingRepository;

    @Transactional
    public void verify(AuthUserDetails authUserDetails, SettlementDto.VerifyReq dto, Long settlementIdx) {
        // 테스트용: PortOne SDK 호출 생략하고 바로 정산 처리
        // dto 에서 0이 들어옴
        Settlement settlement = settlementRepository.findById(settlementIdx)
                .orElseThrow(() -> new IllegalArgumentException("정산 정보를 찾을 수 없습니다."));

        // 정산 금액 검증 (실제로는 PortOne 결제 금액과 비교)
//        long totalPrice = headIncomeRepository.findBySettlementIdx(dto.getSettlementIdx())
//                .stream()
//                .mapToLong(HeadIncome::getPrice)
//                .sum();

        List<HeadIncome> headIncomePriceList = new ArrayList<>();
        headIncomePriceList = headIncomeRepository.findBySettlementIdx(settlementIdx);

        long totalPrice = 0;

        for (HeadIncome headIncome : headIncomePriceList) {
            totalPrice += headIncome.getPrice();
        }


        // 테스트용: 결제 금액이 정산 금액과 일치한다고 가정

        settlement.setPaid(true);
        settlement.setPgPaymentId(dto.getPaymentId());

        // 관련 head_income도 status=true로 업데이트 (paid 상태로 표시)
        List<HeadIncome> headIncomeList = headIncomeRepository.findBySettlementIdx(settlementIdx);
        for (HeadIncome headIncome : headIncomeList) {
            headIncome.setStatus(true);
            headIncomeRepository.save(headIncome);
        }

        // AfterBilling 업데이트 (실패 건 수동 결제 성공 시)
        afterBillingRepository.findByStoreIdxAndPayedMonth(settlement.getStoreIdx(), settlement.getSettlementYm())
                .ifPresent(after -> {
                    after.setIsPaid(true);
                    after.setIsSuccess(true);
                    after.setIsRetryFailed(false);
                    after.setFailReason(null);
                    afterBillingRepository.save(after);
                });

    }

    public List<SettlementDto.FinalRetryFailRes> getFinalRetryFailures(Long storeIdx) {
        return afterBillingRepository.findByStoreIdxAndIsRetryFailedTrue(storeIdx).stream()
                .map(after -> {
                    YearMonth ym = YearMonth.parse(after.getPayedMonth());
                    LocalDateTime start = ym.atDay(1).atStartOfDay();
                    LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();

                    List<Long> ids = headIncomeRepository.findUnpaidByStoreAndMonth(storeIdx, start, end).stream()
                            .map(HeadIncome::getIdx)
                            .toList();

                    return SettlementDto.FinalRetryFailRes.builder()
                            .idx(after.getIdx())
                            .storeIdx(after.getStoreIdx())
                            .amount(after.getTotalPayAmount())
                            .payedMonth(after.getPayedMonth())
                            .failReason(after.getFailReason())
                            .headIncomeIdxList(ids)
                            .build();
                })
                .toList();
    }

    @Transactional
    public Long pay(Long storeIdx, SettlementDto.PayReq dto) {
        YearMonth currentMonth = YearMonth.now();
        YearMonth lastMonth = currentMonth.minusMonths(1);
        String settlementYm = lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        Settlement settlement = Settlement.builder()
                .paid(false)
                .price(dto.getPaymentPrice())
                .storeIdx(storeIdx)
                .settlementYm(settlementYm)
                .build();


        settlementRepository.save(settlement);

        for (Long idx : dto.getHeadIncomeidxList()) {
            HeadIncome headIncome = headIncomeRepository.findById(idx).orElse(null);
            if (headIncome != null) {
                headIncome.setSettlementIdx(settlement.getIdx());
                headIncomeRepository.save(headIncome);
            }
        }

        return settlement.getIdx();
    }

    public Long findSettlementIdx(Long storeIdx, YearMonth lastMonth) {
        Settlement settlement = settlementRepository.findByStoreIdxAndSettlementYm(storeIdx, lastMonth.toString()).orElse(null);
        if (settlement != null) {
            return settlement.getIdx();
        } else {
            return null;
        }
    }
}
