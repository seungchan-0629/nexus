package com.example.nexus.domain.orders;

import com.example.nexus.common.enums.OrdersType;
import com.example.nexus.common.exception.BaseException;
import com.example.nexus.common.model.BaseResponse;
import com.example.nexus.common.model.BaseResponseStatus;
import com.example.nexus.common.model.PageResponse;
import com.example.nexus.domain.orders.model.DangerDto;
import com.example.nexus.domain.orders.model.OrdersDto;
import com.example.nexus.domain.orders.model.OrdersItemDto;
import com.example.nexus.domain.user.model.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;


@Tag(name = "발주 관리", description = "본사 발주 (자동/확정/이력/이상) + 가맹점 제안 발주서 관리. 배치 서비스(:8090) 와 연동.")
@RequestMapping("/orders")
@RestController
@RequiredArgsConstructor
public class OrdersController {
    private final OrdersService orderService;
    private final RestTemplate restTemplate;

    @Value("${batch.service.url:http://localhost:8090}")
    private String batchServiceUrl;

    /**
     * 자동 발주 목록 조회 겸 검색
     */
    @Operation(
            summary = "자동 발주 목록 조회",
            description = "본사가 보는 자동 발주 목록 (페이징, 검색)."
    )
    @GetMapping("/list/auto")
    public ResponseEntity autoList(
            @Parameter(description = "검색 키워드 (선택)") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        Page<OrdersDto.OrderListRes> result = orderService.findAllAuto(
                keyword, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return ResponseEntity.ok(BaseResponse.success(PageResponse.from(result)));
    }

    /**
     * 확정 발주 목록 조회 겸 검색
     */
    @Operation(
            summary = "확정 발주 목록 조회",
            description = "본사가 보는 확정 발주 목록 (페이징, 검색)."
    )
    @GetMapping("/list/confirmed")
    public ResponseEntity confirmedList(
            @Parameter(description = "검색 키워드 (선택)") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        Page<OrdersDto.OrderListRes> result = orderService.findAllConfirmed(
                keyword, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return ResponseEntity.ok(BaseResponse.success(PageResponse.from(result)));
    }

    /**
     * 확정 발주 일괄 승인
     */
    @Operation(
            summary = "확정 발주 일괄 승인",
            description = "배치 서비스(:8090)에 일괄 승인 작업 요청. RestTemplate 으로 batch-service/batch/jobs/approve 호출."
    )
    @PutMapping("/confirmed/approve")
    public ResponseEntity approveAll() {
        try {
            restTemplate.postForEntity(batchServiceUrl + "/batch/jobs/approve", null, String.class);
        } catch (RestClientException e) {
            throw new BaseException(BaseResponseStatus.BATCH_SERVICE_UNAVAILABLE);
        }
        return ResponseEntity.ok(BaseResponse.success("update success"));
    }

    /**
     * 발주 이력 목록 조회 겸 검색
     */
    @Operation(
            summary = "발주 이력 목록 조회",
            description = "본사가 보는 발주 이력 (타입/날짜 범위/키워드 필터, 페이징)."
    )
    @GetMapping("/list/history")
    public ResponseEntity historyList(
            @Parameter(description = "발주 유형 필터") @RequestParam(required = false) OrdersType ordersType,
            @Parameter(description = "조회 시작일 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료일 (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "검색 키워드 (선택)") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        Page<OrdersDto.OrderListRes> result = orderService.findOrderHistory(
                ordersType, startDate, endDate, keyword,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(BaseResponse.success(PageResponse.from(result)));
    }

    /**
     * 이상 발주 목록 조회 겸 검색
     */
    @Operation(
            summary = "이상 발주 목록 조회",
            description = "이상 발주 기준 (수량 급증 등) 위반 발주 목록 (날짜/키워드, 페이징)."
    )
    @GetMapping("/list/danger")
    public ResponseEntity dangerList(
            @Parameter(description = "조회 시작일") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료일") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "검색 키워드 (선택)") @RequestParam(required = false) String keyword,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        Page<DangerDto.DangerListRes> result = orderService.findDangerOrders(
                startDate, endDate, keyword, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return ResponseEntity.ok(BaseResponse.success(PageResponse.from(result)));
    }

    /**
     * 발주 상세 정보 조회
     */
    @Operation(
            summary = "발주 상세 조회",
            description = "특정 발주의 상세 정보 (발주 항목 포함)."
    )
    @GetMapping("/{ordersIdx}")
    public ResponseEntity ordersDetail(
            @Parameter(description = "발주 고유 ID", examples = {@ExampleObject(name = "정상 (존재하는 발주)", value = "1"), @ExampleObject(name = "NOT_FOUND_ORDERS (없는 발주)", value = "99999")})
            @PathVariable Long ordersIdx
    ) {
        OrdersDto.OrdersRes result = orderService.findById(ordersIdx);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 이상 발주 기준 설정 조회
     */
    @Operation(
            summary = "이상 발주 기준 조회",
            description = "본사가 설정한 이상 발주 판정 기준 (수량/금액 등)."
    )
    @GetMapping("/danger")
    public ResponseEntity find() {
        DangerDto.DangerRes result = orderService.find();
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 이상 발주 기준 설정 수정
     */
    @Operation(
            summary = "이상 발주 기준 수정",
            description = "본사가 이상 발주 판정 기준 변경. 변경 후 새 발주 부터 적용."
    )
    @PutMapping("/danger")
    public ResponseEntity save(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = {
                            @ExampleObject(name = "기본 (200% 초과 / 3개월 평균)", value = "{\"ratio\":200,\"period\":3}"),
                            @ExampleObject(name = "엄격 (50% 초과 / 6개월 평균)", value = "{\"ratio\":50,\"period\":6}")}
                    )
            )
            @RequestBody DangerDto.DangerReq req
    ) {
        orderService.save(req);
        return ResponseEntity.ok(BaseResponse.success("update success"));
    }

    /**
     * 발주 승인
     */
    @Operation(
            summary = "발주 승인",
            description = "본사가 특정 발주 단건 승인. CONFIRMED 상태인 발주만 승인 가능."
    )
    @PutMapping("/{ordersIdx}/approve")
    public ResponseEntity approve(
            @Parameter(
                    description = "승인할 발주 ID (CONFIRMED 상태인 발주만 승인 가능)",
                    examples = {
                            @ExampleObject(name = "정상 (CONFIRMED 발주)", value = "16"),
                            @ExampleObject(name = "ORDERS_INVALID_STATUS (다른 상태)", value = "1"),
                            @ExampleObject(name = "NOT_FOUND_ORDERS (없는 발주)", value = "99999")
                    }
            )
            @PathVariable Long ordersIdx
    ) {
        orderService.approve(ordersIdx);
        return ResponseEntity.ok(BaseResponse.success("update success"));
    }

    /**
     * 발주 반려
     */
    @Operation(
            summary = "발주 반려",
            description = "본사가 특정 발주 단건 반려. CONFIRMED 상태인 발주만 반려 가능."
    )
    @PutMapping("/{ordersIdx}/reject")
    public ResponseEntity reject(
            @Parameter(
                    description = "반려할 발주 ID (CONFIRMED 상태인 발주만 반려 가능)",
                    examples = {
                            @ExampleObject(name = "정상 (CONFIRMED 발주)", value = "16"),
                            @ExampleObject(name = "ORDERS_INVALID_STATUS (다른 상태)", value = "1"),
                            @ExampleObject(name = "NOT_FOUND_ORDERS (없는 발주)", value = "99999")
                    }
            )
            @PathVariable Long ordersIdx
    ) {
        orderService.reject(ordersIdx);
        return ResponseEntity.ok(BaseResponse.success("update success"));
    }

    /**
     * 가맹점 수동 발주 생성
     */
    @Operation(
            summary = "가맹점 수동 발주 생성",
            description = "가맹점 사용자가 직접 발주 작성. 인증 필요."
    )
    @PostMapping("/store/reg/manual")
    public ResponseEntity createStoreManualOrder(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = {
                                    @ExampleObject(name = "정상 (항목 3개)",
                                            value = "{\"ordersItemList\":[{\"productIdx\":1,\"count\":10},{\"productIdx\":4,\"count\":5},{\"productIdx\":7,\"count\":3}]}"),
                                    @ExampleObject(name = "ORDERS_ITEMS_EMPTY (빈 항목)",
                                            value = "{\"ordersItemList\":[]}"),
                                    @ExampleObject(name = "NOT_FOUND_PRODUCT (없는 상품)",
                                            value = "{\"ordersItemList\":[{\"productIdx\":99999,\"count\":10}]}")
                            }
                    )
            )
            @RequestBody OrdersDto.OrdersReq req
    ) {
        if (authUserDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        orderService.createStoreManualOrder(authUserDetails.getIdx(), req);
        return ResponseEntity.ok(BaseResponse.success("create success"));
    }

    /**
     * 가맹점 제안 발주서 조회
     */
    @Operation(
            summary = "가맹점 제안 발주서 조회",
            description = "현재 가맹점의 진행 중인 자동 발주 목록 (제안 상태). 인증 필요."
    )
    @GetMapping("/store/find")
    public ResponseEntity storeFind(@AuthenticationPrincipal AuthUserDetails authUserDetails) {
        if (authUserDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        List<OrdersDto.OrdersRes> result = orderService.findByUserIdxAndOrdersStatus(authUserDetails.getIdx());
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    /**
     * 가맹점 제안 발주서 확정
     */
    @Operation(
            summary = "가맹점 제안 발주서 확정",
            description = "가맹점이 자동 발주 제안을 확정. WAITING 상태인 자신의 발주만 확정 가능."
    )
    @PutMapping("/store/{ordersIdx}/confirm")
    public ResponseEntity confirmStoreOrder(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(
                    description = "확정할 발주 ID (WAITING 상태인 자신의 발주만 확정 가능)",
                    examples = {
                            @ExampleObject(name = "정상 (WAITING + 본인 매장)", value = "2"),
                            @ExampleObject(name = "ORDERS_NOT_AUTHORIZED (다른 매장)", value = "100"),
                            @ExampleObject(name = "ORDERS_INVALID_STATUS (WAITING 아님)", value = "1"),
                            @ExampleObject(name = "NOT_FOUND_ORDERS (없는 발주)", value = "99999")
                    }
            )
            @PathVariable Long ordersIdx
    ) {
        if (authUserDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        orderService.confirmStoreOrder(authUserDetails.getIdx(), ordersIdx);
        return ResponseEntity.ok(BaseResponse.success("update success"));
    }

    /**
     * 가맹점 제안 발주서 항목 추가
     */
    @Operation(
            summary = "가맹점 제안 발주서 항목 추가",
            description = "가맹점이 자동 발주 제안에 항목 추가. 자신의 발주만 수정 가능."
    )
    @PostMapping("/store/{ordersIdx}/items")
    public ResponseEntity addStoreItem(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(
                    description = "항목 추가할 발주 ID",
                    examples = {
                            @ExampleObject(name = "정상 (본인 매장 발주)", value = "2"),
                            @ExampleObject(name = "ORDERS_NOT_AUTHORIZED (다른 매장)", value = "100"),
                            @ExampleObject(name = "NOT_FOUND_ORDERS (없는 발주)", value = "99999")
                    }
            )
            @PathVariable Long ordersIdx,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = {
                                    @ExampleObject(name = "정상 (원두 10개 추가)", value = "{\"productIdx\":1,\"count\":10}"),
                                    @ExampleObject(name = "NOT_FOUND_PRODUCT (없는 상품)", value = "{\"productIdx\":99999,\"count\":10}")
                            }
                    )
            )
            @RequestBody OrdersItemDto.OrdersItemReq req
    ) {
        if (authUserDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        orderService.addStoreItem(authUserDetails.getIdx(), ordersIdx, req);
        return ResponseEntity.ok(BaseResponse.success("create success"));
    }

    /**
     * 가맹점 제안 발주서 항목 수량 수정
     */
    @Operation(
            summary = "가맹점 발주 항목 수량 수정",
            description = "가맹점이 제안 발주서 항목의 수량 변경. 자신의 발주만 수정 가능."
    )
    @PutMapping("/store/{ordersItemIdx}/items")
    public ResponseEntity updateStoreItemCount(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(
                    description = "수정할 발주 항목 ID",
                    examples = {
                            @ExampleObject(name = "정상 (본인 매장 항목)", value = "1"),
                            @ExampleObject(name = "ORDERS_NOT_AUTHORIZED (다른 매장 항목)", value = "500"),
                            @ExampleObject(name = "NOT_FOUND_ORDERS_ITEM (없는 항목)", value = "99999")
                    }
            )
            @PathVariable Long ordersItemIdx,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = {
                                    @ExampleObject(name = "정상 (수량 20개로 변경)", value = "{\"count\":20}"),
                                    @ExampleObject(name = "수량 0", value = "{\"count\":0}")
                            }
                    )
            )
            @RequestBody OrdersItemDto.OrdersItemReq req
    ) {
        if (authUserDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        orderService.updateStoreItemCount(authUserDetails.getIdx(), ordersItemIdx, req.getCount());
        return ResponseEntity.ok(BaseResponse.success("update success"));
    }

    /**
     * 가맹점 제안 발주서 항목 삭제
     */
    @Operation(
            summary = "가맹점 발주 항목 삭제",
            description = "가맹점이 제안 발주서 항목 제거. 자신의 발주만 수정 가능."
    )
    @DeleteMapping("/store/{ordersItemIdx}/items")
    public ResponseEntity deleteStoreItem(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(
                    description = "삭제할 발주 항목 ID",
                    examples = {
                            @ExampleObject(name = "정상 (본인 매장 항목)", value = "1"),
                            @ExampleObject(name = "ORDERS_NOT_AUTHORIZED (다른 매장 항목)", value = "500"),
                            @ExampleObject(name = "NOT_FOUND_ORDERS_ITEM (없는 항목)", value = "99999")
                    }
            )
            @PathVariable Long ordersItemIdx
    ) {
        if (authUserDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        orderService.deleteStoreItem(authUserDetails.getIdx(), ordersItemIdx);
        return ResponseEntity.ok(BaseResponse.success("delete success"));
    }

    /**
     * 가맹점 제안 발주서 거절
     */
    @Operation(
            summary = "가맹점 제안 발주서 거절",
            description = "가맹점이 자동 발주 제안 자체를 거절. WAITING 상태인 자신의 발주만 거절 가능."
    )
    @PutMapping("/store/{ordersIdx}/reject")
    public ResponseEntity rejectStoreOrder(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(
                    description = "거절할 발주 ID (WAITING 상태인 자신의 발주만 거절 가능)",
                    examples = {
                            @ExampleObject(name = "정상 (WAITING + 본인 매장)", value = "2"),
                            @ExampleObject(name = "ORDERS_NOT_AUTHORIZED (다른 매장)", value = "100"),
                            @ExampleObject(name = "ORDERS_INVALID_STATUS (WAITING 아님)", value = "1"),
                            @ExampleObject(name = "NOT_FOUND_ORDERS (없는 발주)", value = "99999")
                    }
            )
            @PathVariable Long ordersIdx
    ) {
        if (authUserDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        orderService.rejectStoreOrder(authUserDetails.getIdx(), ordersIdx);
        return ResponseEntity.ok(BaseResponse.success("update success"));
    }

    /**
     * 가맹점 발주 이력 조회 (페이징)
     */
    @Operation(
            summary = "가맹점 발주 이력 조회",
            description = "현재 가맹점의 발주 이력 (페이징). 인증 필요."
    )
    @GetMapping("/store/list/paged")
    public ResponseEntity storeListPaged(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        if (authUserDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        Page<OrdersDto.OrdersRes> result = orderService.findByUserIdxPaged(authUserDetails.getIdx(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(BaseResponse.success(PageResponse.from(result)));
    }

    /**
     * 가맹점 발주 취소
     */
    @Operation(
            summary = "가맹점 발주 취소",
            description = "가맹점이 본인 발주 취소. CONFIRMED 상태인 자신의 발주만 취소 가능."
    )
    @PutMapping("/store/{ordersIdx}/cancel")
    public ResponseEntity cancel(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(
                    description = "취소할 발주 ID (CONFIRMED 상태인 자신의 발주만 취소 가능)",
                    examples = {
                            @ExampleObject(name = "정상 (CONFIRMED + 본인 매장)", value = "16"),
                            @ExampleObject(name = "ORDERS_NOT_AUTHORIZED (다른 매장)", value = "100"),
                            @ExampleObject(name = "ORDERS_INVALID_STATUS (CONFIRMED 아님)", value = "2"),
                            @ExampleObject(name = "NOT_FOUND_ORDERS (없는 발주)", value = "99999")
                    }
            )
            @PathVariable Long ordersIdx
    ) {
        if (authUserDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }
        orderService.cancelOrder(authUserDetails.getIdx(), ordersIdx);
        return ResponseEntity.ok(BaseResponse.success("update success"));
    }

}
