package com.example.nexus.domain.billing;

import com.example.nexus.common.model.BaseResponse;
import com.example.nexus.domain.billing.model.BillingDto;
import com.example.nexus.domain.store.StoreService;
import com.example.nexus.domain.user.model.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "결제 빌링 관리", description = "가맹점 자동 결제를 위한 빌링키 발급, 조회, 삭제 관리 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billingService;
    private final StoreService storeService;

    @Operation(summary = "빌링키 발급 및 등록", description = "포트원 인증키를 통해 가맹점 자동 결제를 위한 빌링키를 발급받고 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "빌링키 발급 및 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "success": true,
                                          "code": 1000,
                                          "message": "빌링키 발급 및 등록 성공",
                                          "result": "빌링키 발급 및 등록 성공"
                                        }
                                        """
                            )
                    )
            )
    })
    @PostMapping("/issue")
    public ResponseEntity issueBillingKey(@AuthenticationPrincipal AuthUserDetails authUserDetails, @RequestBody BillingDto.IssueBillingKeyRequestDto request) {
        Long storeIdx = storeService.findStoreIdx(authUserDetails.getIdx());
        if (storeIdx == null) {
            return ResponseEntity.status(403).body(BaseResponse.fail(com.example.nexus.common.model.BaseResponseStatus.PAYMENT_ENROLL_INVALID_USER));
        }
        billingService.issueBillingKey(storeIdx, request);
        return ResponseEntity.ok(BaseResponse.success("빌링키 발급 및 등록 성공"));
    }

    @Operation(summary = "등록된 빌링 정보 조회", description = "로그인한 가맹점에 등록된 빌링(카드) 정보 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "등록된 빌링 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "success": true,
                                          "code": 1000,
                                          "message": "요청에 성공하였습니다.",
                                          "result": [
                                            {
                                              "id": 1,
                                              "mId": "tvivarepublica2",
                                              "customerKey": "STORE_1",
                                              "authenticatedAt": "2026-05-28T10:00:00",
                                              "method": "카드",
                                              "storeIdx": 1,
                                              "cardCompany": "신한카드",
                                              "cardNumber": "4518********1234"
                                            }
                                          ]
                                        }
                                        """
                            )
                    )
            )
    })
    @GetMapping("/list")
    public ResponseEntity getBillingList(@AuthenticationPrincipal AuthUserDetails authUserDetails) {
        Long storeIdx = storeService.findStoreIdx(authUserDetails.getIdx());
        if (storeIdx == null) {
            return ResponseEntity.ok(BaseResponse.success(List.of()));
        }
        return ResponseEntity.ok(BaseResponse.success(billingService.getBillingListByStore(storeIdx)));
    }

    @Operation(summary = "빌링 정보 삭제", description = "가맹점에 등록된 빌링 정보를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "빌링 정보 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "success": true,
                                          "code": 1000,
                                          "message": "빌링 정보 삭제 성공",
                                          "result": "빌링 정보 삭제 성공"
                                        }
                                        """
                            )
                    )
            )
    })
    @DeleteMapping("/delete/{storeIdx}")
    public ResponseEntity deleteBillingInfo(@Parameter(description = "가맹점 번호") @PathVariable Long storeIdx) {
        billingService.deleteBillingInfo(storeIdx);
        return ResponseEntity.ok(BaseResponse.success("빌링 정보 삭제 성공"));
    }
}
