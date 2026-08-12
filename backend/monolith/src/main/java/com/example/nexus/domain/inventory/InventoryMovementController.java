package com.example.nexus.domain.inventory;

import com.example.nexus.common.model.BaseResponse;
import com.example.nexus.domain.inventory.model.InventoryMovementDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Tag(name = "재고 입출고", description = "본사 기준 제품 입고·출고 처리 및 이동 이력 조회. 인증 필요.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory")
public class InventoryMovementController {
    private final InventoryMovementService inventoryMovementService;

    @Operation(
            summary = "본사 재고 입고",
            description = """
                    본사에 제품을 입고 처리. HeadInventory 수량 증가 및 InventoryMovement 이력 기록.

                    요청 필드:
                    - productIdx: 입고할 제품 ID
                    - quantity: 입고 수량
                    - memo: 입고 메모 (선택)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "입고 처리 성공"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {"success":false,"code":401,"message":"로그인이 필요합니다.","result":null}"""))),
            @ApiResponse(responseCode = "400", description = "제품을 찾을 수 없습니다. (NOT_FOUND_PRODUCT)",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {"success":false,"code":3202,"message":"제품을 찾을 수 없습니다.","result":null}""")))
    })
    @PostMapping("/inbound")
    public ResponseEntity<?> inbound(@AuthenticationPrincipal UserDetails userDetails, @RequestBody InventoryMovementDto.InboundReq req) {
        if (userDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        InventoryMovementDto.MovementRes result = inventoryMovementService.inbound(req);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    @Operation(
            summary = "본사 재고 출고 (매장 배송)",
            description = """
                    본사에서 특정 매장으로 제품을 출고 처리. HeadInventory 차감, StoreInventory 적재, InventoryMovement 이력 기록.

                    요청 필드:
                    - productIdx: 출고할 제품 ID
                    - storeIdx: 배송 대상 매장 ID
                    - quantity: 출고 수량
                    - memo: 출고 메모 (선택)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "출고 처리 성공"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {"success":false,"code":401,"message":"로그인이 필요합니다.","result":null}"""))),
            @ApiResponse(responseCode = "400", description = "제품 또는 매장을 찾을 수 없거나 본사 재고 부족",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {"success":false,"code":3306,"message":"본사 재고가 부족합니다.","result":null}""")))
    })
    @PostMapping("/outbound")
    public ResponseEntity<?> outbound(@AuthenticationPrincipal UserDetails userDetails, @RequestBody InventoryMovementDto.OutboundReq req) {
        if (userDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        InventoryMovementDto.MovementRes result = inventoryMovementService.outbound(req);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    @Operation(
            summary = "입출고 이력 전체 조회",
            description = "본사 기준 전체 입출고 이동 이력 목록 조회. movementType(INBOUND/OUTBOUND), 제품명, 이동 출발지·목적지, 수량, 메모, 일시 포함."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이력 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {"success":false,"code":401,"message":"로그인이 필요합니다.","result":null}""")))
    })
    @GetMapping("/movements")
    public ResponseEntity<?> listMovements(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        List<InventoryMovementDto.MovementListRes> result = inventoryMovementService.findAllMovements();
        return ResponseEntity.ok(BaseResponse.success(result));
    }
}
