package com.example.nexus.domain.pos;

import com.example.nexus.common.model.BaseResponse;
import com.example.nexus.common.model.PageResponse;
import com.example.nexus.domain.pos.model.PosCloseDto;
import com.example.nexus.domain.pos.model.PosPayDto;
import com.example.nexus.domain.pos.model.PosStoreInventoryDto;
import com.example.nexus.domain.user.model.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Tag(name = "POS", description = "가맹점 POS 재고 조회·수정, 결제 처리, 당일 결제 내역 조회, 영업 마감. 인증 필요.")
@RequestMapping("/pos")
@RestController
@RequiredArgsConstructor
public class PosController {
    private final PosService posService;
    private final AutoOrderService autoOrderService;

    @Operation(
            summary = "POS 재고 목록 조회",
            description = """
                    로그인한 가맹점주의 user_idx 기준으로 해당 매장 POS 재고를 페이징 조회.

                    응답 필드 (ListRes):
                    - idx: POS 재고 lot ID
                    - count: 현재 수량
                    - status: 재고 상태 (InventoryStatus)
                    - manufacturedDate: 제조일
                    - productIdx / productName: 제품 정보
                    - minStock: 최소 재고 기준량
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "POS 재고 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {"success":false,"code":401,"message":"로그인이 필요합니다.","result":null}""")))
    })
    @GetMapping("/inventory/list")
    public ResponseEntity<BaseResponse<PageResponse<PosStoreInventoryDto.ListRes>>> list(
            @AuthenticationPrincipal AuthUserDetails userDetails,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size) {
        if (userDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        PageResponse<PosStoreInventoryDto.ListRes> result = posService.listByUserIdxPaged(userDetails.getIdx(), page, size);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    @Operation(
            summary = "POS 재고 수량 수정",
            description = """
                    가맹점주가 특정 POS 재고 lot의 수량을 직접 수정. POS 재고(posCount)와 본사가 조회하는 가맹점 재고(hqCount) 양쪽에 동기 반영.

                    요청 필드 (CountReq):
                    - count: 변경할 수량

                    응답 필드 (SyncCountRes):
                    - posInventoryIdx / posCount: 수정된 POS 재고 lot 정보
                    - hqInventoryIdx / hqCount: 동기화된 본사 측 가맹점 재고 정보
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재고 수량 수정 성공"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {"success":false,"code":401,"message":"로그인이 필요합니다.","result":null}"""))),
            @ApiResponse(responseCode = "400", description = "POS 재고 항목 없음 / 해당 재고에 대한 권한 없음",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {"success":false,"code":3301,"message":"POS 재고 항목을 찾을 수 없습니다.","result":null}""")))
    })
    @PatchMapping("/inventory/{posStoreInventoryIdx}")
    public ResponseEntity<BaseResponse<PosStoreInventoryDto.SyncCountRes>> changeInventoryCount(
            @AuthenticationPrincipal AuthUserDetails userDetails,
            @Parameter(description = "수정할 POS 재고 lot ID", example = "1") @PathVariable Long posStoreInventoryIdx,
            @RequestBody PosStoreInventoryDto.CountReq req) {
        if (userDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        PosStoreInventoryDto.SyncCountRes result = posService.changeCount(userDetails.getIdx(), posStoreInventoryIdx, req.getCount());

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    @Operation(
            summary = "POS 결제 처리",
            description = """
                    POS에서 결제를 저장. 결제 금액은 메뉴 단가 기준으로 서버에서 계산.

                    **요청 필드 (PayReq)**
                    - method: 결제 수단 — `CARD` 또는 `CASH` (필수)
                    - items: 주문 항목 목록 (1개 이상 필수)
                      - menuIdx: 메뉴 ID (필수)
                      - quantity: 수량 (1 이상, 필수)

                    **응답 필드 (PayRes)**
                    - posPayIdx: 결제 ID
                    - storeIdx: 매장 ID
                    - method: 사용한 결제 수단
                    - payAmount: 총 결제 금액 (서버에서 메뉴 단가 기준으로 계산)
                    - paidAt: 결제 일시
                    - items: 항목별 menuIdx·menuName·quantity·unitPrice·lineAmount 포함
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PosPayDto.PayReq.class),
                    examples = {
                            @ExampleObject(
                                    name = "카드 결제",
                                    summary = "카드로 메뉴 2종 주문",
                                    value = """
                                            {
                                              "method": "CARD",
                                              "items": [
                                                {"menuIdx": 10, "quantity": 2},
                                                {"menuIdx": 11, "quantity": 1}
                                              ]
                                            }"""
                            ),
                            @ExampleObject(
                                    name = "현금 결제",
                                    summary = "현금으로 메뉴 1종 주문",
                                    value = """
                                            {
                                              "method": "CASH",
                                              "items": [
                                                {"menuIdx": 10, "quantity": 3}
                                              ]
                                            }"""
                            )
                    }
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "결제 처리 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "code": 1000,
                                      "message": "요청에 성공하였습니다.",
                                      "result": {
                                        "posPayIdx": 42,
                                        "storeIdx": 3,
                                        "method": "CARD",
                                        "payAmount": 15000,
                                        "paidAt": "2026-05-28T14:30:00",
                                        "items": [
                                          {"menuIdx": 10, "menuName": "아메리카노", "quantity": 2, "unitPrice": 4500, "lineAmount": 9000},
                                          {"menuIdx": 11, "menuName": "카페라떼", "quantity": 1, "unitPrice": 6000, "lineAmount": 6000}
                                        ]
                                      }
                                    }""")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청값 검증 실패 / 메뉴 없음 / POS 재고 부족",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "검증 실패",
                                            summary = "method 또는 items 누락",
                                            value = """
                                                    {"success":false,"code":4001,"message":"입력값이 잘못되었습니다.","result":{"method":"널이어서는 안됩니다","items":"비어 있으면 안 됩니다"}}"""
                                    ),
                                    @ExampleObject(
                                            name = "재고 부족",
                                            summary = "POS 재고가 주문 수량보다 적은 경우",
                                            value = """
                                                    {"success":false,"code":3302,"message":"POS 재고가 부족합니다.","result":{"productIdx":5,"required":10,"availableTotal":3}}"""
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "로그인이 필요합니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(value = """
                                    {"success":false,"code":401,"message":"로그인이 필요합니다.","result":null}""")
                    )
            )
    })
    @PostMapping("/pay")
    public ResponseEntity<BaseResponse<PosPayDto.PayRes>> pay(@AuthenticationPrincipal AuthUserDetails userDetails, @Valid @RequestBody PosPayDto.PayReq req) {
        if (userDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        PosPayDto.PayRes result = posService.pay(userDetails.getIdx(), req);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    @Operation(
            summary = "당일 결제 내역 조회",
            description = """
                    로그인한 가맹점주의 매장 기준 당일(오늘) 결제 내역 전체 조회.

                    응답 필드 (TodayPayRes):
                    - posPayIdx: 결제 ID
                    - method: 결제 수단
                    - payAmount: 결제 금액
                    - paidAt: 결제 일시
                    - items: 메뉴명·수량 목록
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "당일 결제 내역 조회 성공 (결제 내역 없으면 빈 배열 반환)"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {"success":false,"code":401,"message":"로그인이 필요합니다.","result":null}""")))
    })
    @GetMapping("/pay/today")
    public ResponseEntity<BaseResponse<List<PosPayDto.TodayPayRes>>> todayPays(@AuthenticationPrincipal AuthUserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        List<PosPayDto.TodayPayRes> result = posService.listTodayPayHistory(userDetails.getIdx());
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    @Operation(
            summary = "영업 마감",
            description = """
                    POS 영업 마감 처리. 당일 결제 기준으로 매장 재고를 차감하고, 자동 발주를 생성.

                    동작 순서:
                    1. 당일 결제(PosOrdersItem) 기준으로 StoreInventory 차감
                    2. AutoOrderService.generateAutoOrder() 호출 → 자동 발주(PROPOSED) 생성

                    응답 필드 (CloseRes):
                    - storeIdx: 마감 처리된 매장 ID
                    - processedPayCount: 처리된 결제 건수
                    - deductedProductKinds: 차감된 제품 종류 수
                    - closedAt: 마감 일시
                    - message: 처리 결과 메시지
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "영업 마감 및 자동 발주 생성 성공"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {"success":false,"code":401,"message":"로그인이 필요합니다.","result":null}"""))),
            @ApiResponse(responseCode = "400", description = "가맹점 정보를 찾을 수 없습니다. (STORE_NOT_FOUND)",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {"success":false,"code":3105,"message":"해당 가맹점 정보를 찾을 수 없습니다.","result":null}""")))
    })
    @PostMapping("/close")
    public ResponseEntity<BaseResponse<PosCloseDto.CloseRes>> close(@AuthenticationPrincipal AuthUserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        PosCloseDto.CloseRes res = posService.deductOnClose(userDetails.getIdx());
        autoOrderService.generateAutoOrder(userDetails.getIdx());
        PosCloseDto.CloseRes body = PosCloseDto.CloseRes.builder()
                .storeIdx(res.getStoreIdx())
                .processedPayCount(res.getProcessedPayCount())
                .deductedProductKinds(res.getDeductedProductKinds())
                .closedAt(res.getClosedAt())
                .message(res.getMessage() + " 자동 발주를 실행했습니다.")
                .build();
        return ResponseEntity.ok(BaseResponse.success(body));
    }
}
