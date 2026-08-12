package com.example.nexus.domain.head;

import com.example.nexus.common.model.BaseResponse;
import com.example.nexus.common.model.PageResponse;
import com.example.nexus.domain.head.model.HeadIncomeDto;
import com.example.nexus.domain.head.model.HeadInventoryDto;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Tag(name = "본사 관리", description = "본사 재고 조회 및 정산 관리(수익 내역, 월별 요약, 가맹점별 리스트) API")
@RequestMapping("/head")
@RestController
@RequiredArgsConstructor
public class HeadController {
    private final HeadService headService;
    private final HeadIncomeService headIncomeService;

    // 본사 재고 조회
    @Operation(summary = "본사 재고 목록 조회", description = "본사 창고에 있는 모든 원자재 재고 목록을 페이징하여 조회합니다.")
    @GetMapping("/inventory/list")
    public ResponseEntity<BaseResponse<PageResponse<HeadInventoryDto.ListRes>>> list(
            @AuthenticationPrincipal AuthUserDetails userDetails,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size){
        if (userDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        PageResponse<HeadInventoryDto.ListRes> result = headService.listPaged(page, size);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // 본사 정산 내역 조회
    @Operation(summary = "본사 수익 내역 조회", description = "발주별 본사 수익 내역을 조회합니다. 가맹점명, 정산 상태, 날짜 범위 필터를 지원합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "본사 수익 내역 조회 성공",
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
                                              "incomeIdx": 1,
                                              "storeName": "강남점",
                                              "billingAmount": 1500000,
                                              "settlementStatus": true,
                                              "settlementDate": "2026-05-20"
                                            },
                                            {
                                              "incomeIdx": 2,
                                              "storeName": "홍대점",
                                              "billingAmount": 1200000,
                                              "settlementStatus": false,
                                              "settlementDate": null
                                            }
                                          ]
                                        }
                                        """
                            )
                    )
            )
    })
    @GetMapping("/income")
    public ResponseEntity<BaseResponse<List<HeadIncomeDto.ListRes>>> getIncomeList(
            @Parameter(description = "가맹점명 검색어") @RequestParam(required = false) String storeName,
            @Parameter(description = "정산 상태 (true: 완료, false: 대기)") @RequestParam(required = false) Boolean status,
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<HeadIncomeDto.ListRes> result = headIncomeService.getIncomeList(storeName, status, startDate, endDate);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // 본사 정산 관리 - 상단 요약 카드 조회
    @Operation(summary = "본사 정산 요약 조회", description = "특정 월의 전체 가맹점 청구 합계 등 요약 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "본사 정산 요약 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "success": true,
                                          "code": 1000,
                                          "message": "요청에 성공하였습니다.",
                                          "result": {
                                            "totalBillingAmount": 25450000,
                                            "totalStoreCount": 12
                                          }
                                        }
                                        """
                            )
                    )
            )
    })
    @GetMapping("/settlement/summary")
    public ResponseEntity<BaseResponse<HeadIncomeDto.HeadSettlementSummaryRes>> getSettlementSummary(
            @AuthenticationPrincipal AuthUserDetails userDetails,
            @Parameter(description = "가맹점명 검색어") @RequestParam(required = false) String storeName,
            @Parameter(description = "조회 연도", example = "2026") @RequestParam int year,
            @Parameter(description = "조회 월", example = "4") @RequestParam int month) {
        if (userDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        HeadIncomeDto.HeadSettlementSummaryRes result = headIncomeService.getSettlementSummary(storeName, year, month);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // 본사 정산 관리 - 가맹점별 정산 리스트 페이징 조회
    @Operation(summary = "가맹점별 월 정산 리스트 조회", description = "특정 월의 가맹점별 정산 현황(발주 건수, 총 금액, 상태)을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "가맹점별 월 정산 리스트 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "success": true,
                                          "code": 1000,
                                          "message": "요청에 성공하였습니다.",
                                          "result": {
                                            "content": [
                                              {
                                                "storeIdx": 1,
                                                "storeName": "강남점",
                                                "orderCount": 15,
                                                "totalBillingAmount": 4500000,
                                                "settlementStatus": true
                                              },
                                              {
                                                "storeIdx": 2,
                                                "storeName": "홍대점",
                                                "orderCount": 12,
                                                "totalBillingAmount": 3800000,
                                                "settlementStatus": false
                                              }
                                            ],
                                            "page": {
                                              "size": 10,
                                              "number": 0,
                                              "totalElements": 2,
                                              "totalPages": 1
                                            }
                                          }
                                        }
                                        """
                            )
                    )
            )
    })
    @GetMapping("/settlement/list")
    public ResponseEntity<BaseResponse<PageResponse<HeadIncomeDto.StoreSettlementRes>>> getStoreSettlementList(
            @AuthenticationPrincipal AuthUserDetails userDetails,
            @Parameter(description = "가맹점명 검색어") @RequestParam(required = false, defaultValue = "") String storeName,
            @Parameter(description = "조회 연도", example = "2026") @RequestParam int year,
            @Parameter(description = "조회 월", example = "4") @RequestParam int month,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size) {
        if (userDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        PageResponse<HeadIncomeDto.StoreSettlementRes> result =
                headIncomeService.getStoreSettlementList(storeName, year, month, page, size);

        return ResponseEntity.ok(BaseResponse.success(result));
    }
}
