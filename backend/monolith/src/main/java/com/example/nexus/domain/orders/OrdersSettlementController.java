package com.example.nexus.domain.orders;

import com.example.nexus.common.model.BaseResponse;
import com.example.nexus.domain.orders.model.OrdersSettlementDto;
import com.example.nexus.domain.user.model.AuthUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RequestMapping("/store")
@RestController
@RequiredArgsConstructor
public class OrdersSettlementController {

    private final OrdersSettlementService ordersSettlementService;

    // [가맹점] 로그인한 가맹점주의 발주 정산 내역 조회
    @GetMapping("/order/settlement")
    public ResponseEntity<BaseResponse<OrdersSettlementDto.TotalOrderSettlementRes>> getOrderSettlement(
            @AuthenticationPrincipal AuthUserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (userDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        OrdersSettlementDto.TotalOrderSettlementRes result = ordersSettlementService.getOrderSettlement(
                userDetails.getIdx(), year, month, page, size
        );

        return ResponseEntity.ok(BaseResponse.success(result));
    }
}
