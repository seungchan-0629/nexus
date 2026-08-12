package com.example.nexus.domain.settlement;

import com.example.nexus.domain.head.HeadIncomeService;
import com.example.nexus.domain.head.model.HeadIncome;
import com.example.nexus.domain.head.model.HeadIncomeDto;
import com.example.nexus.domain.orders.OrdersService;
import com.example.nexus.domain.orders.model.Orders;
import com.example.nexus.domain.settlement.model.Settlement;
import com.example.nexus.domain.settlement.model.SettlementDto;
import com.example.nexus.domain.store.StoreService;
import com.example.nexus.domain.user.model.AuthUserDetails;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "정산 관리", description = "가맹점 미납 내역 조회 및 결제/검증 관리 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/settlement")
public class SettlementController {

    private final HeadIncomeService headIncomeService;
    private final StoreService storeService;
    private final SettlementService settlementService;
    private final OrdersService ordersService;

    @Operation(
            summary = "미납 내역 조회",
            description = "배치 처리 후 재시도(2차) 결제까지 최종 실패한 가맹점의 미납 내역 목록을 조회합니다.",
            parameters = {
                    @Parameter(name = "authUserDetails", hidden = true)
            }
    )
    @GetMapping("/unpaid/list")
    public ResponseEntity<List<SettlementDto.FinalRetryFailRes>> getUnpaidList(
            @AuthenticationPrincipal AuthUserDetails authUserDetails) {

        // 로그인 한 사용자의 지점 번호 받아오기
        Long storeIdx = storeService.findStoreIdx(authUserDetails.getIdx());

        // 재시도까지 최종 실패한 목록 가져오기
        List<SettlementDto.FinalRetryFailRes> finalFailures = settlementService.getFinalRetryFailures(storeIdx);

        return ResponseEntity.ok(finalFailures);
    }

    @Operation(
            summary = "결제 요청",
            description = "미납된 정산 내역에 대해 결제를 시작하기 위한 초기 데이터를 생성합니다.",
            parameters = {
                    @Parameter(name = "authUserDetails", hidden = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "결제할 총 금액과 대상 수입 내역 ID 목록",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = SettlementDto.PayReq.class)
                    )
            )
    )
    @PostMapping("/pay")
    public ResponseEntity<List<HeadIncomeDto.FindHeadIncomeRes>> pay(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestBody SettlementDto.PayReq reqDto) {

        // 로그인 한 사용자의 storeIdx 가져오기
        Long storeIdx = storeService.findStoreIdx(authUserDetails.getIdx());

        // verify 전 단계 완료
        Long settlementIdx = settlementService.pay(storeIdx, reqDto);
        
        List<HeadIncomeDto.FindHeadIncomeRes> headIncomeResList = new ArrayList<>();

        for (Long idx : reqDto.getHeadIncomeidxList()) {
            HeadIncome headIncome = headIncomeService.findById(idx);
            HeadIncomeDto.FindHeadIncomeRes headIncomeResDto = HeadIncomeDto.FindHeadIncomeRes.builder()
                    .idx(headIncome.getIdx())
                    .price(headIncome.getPrice())
                    .paid(headIncome.getStatus())
                    .storeIdx(headIncome.getStore().getIdx())
                    .ordersIdx(headIncome.getOrders().getIdx())
                    .build();
            headIncomeResList.add(headIncomeResDto);
        }

        return ResponseEntity.ok(headIncomeResList);
    }

    @Operation(
            summary = "결제 검증",
            description = """
                    포트원(Portone) 결제 완료 후 서버 측에서 결제 정보의 유효성을 검증합니다.
                    
                    **검증 절차:**
                    1. 클라이언트가 전달한 `paymentId`를 통해 포트원 서버에 결제 상세 정보 조회
                    2. 서버에 기록된 정산 예정 금액과 실제 결제 금액 비교
                    3. 검증 성공 시 정산 상태 및 관련 수입 내역(HeadIncome) 상태를 '결제 완료'로 업데이트
                    """,
            parameters = {
                    @Parameter(name = "authUserDetails", hidden = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "포트원에서 발급받은 결제 ID",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = SettlementDto.VerifyReq.class)
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "결제 검증 성공",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "text/plain",
                                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "결제 성공")
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "결제 금액 불일치 또는 잘못된 요청",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "text/plain",
                                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "결제 금액이 일치하지 않습니다.")
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "정산 내역을 찾을 수 없음",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "text/plain",
                                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "정산 정보를 찾을 수 없습니다.")
                            )
                    )
            }
    )
    @PostMapping("/verify")
    public ResponseEntity<String> verify(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @RequestBody SettlementDto.VerifyReq dto) {

        LocalDateTime now = LocalDateTime.now();
        YearMonth currentYearMonth = YearMonth.from(now);
        YearMonth lastMonth = currentYearMonth.minusMonths(1);
        Long storeIdx = storeService.findStoreIdx(authUserDetails.getIdx());
        
        Long settlementIdx = settlementService.findSettlementIdx(storeIdx, lastMonth);

        settlementService.verify(authUserDetails, dto, settlementIdx);
        return ResponseEntity.ok("결제 성공");
    }
}
