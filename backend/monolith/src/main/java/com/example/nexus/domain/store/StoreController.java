package com.example.nexus.domain.store;

import com.example.nexus.common.model.BaseResponse;
import com.example.nexus.common.model.PageResponse;
import com.example.nexus.domain.store.model.StoreDto;
import com.example.nexus.domain.store.model.StoreIncomeDto;
import com.example.nexus.domain.store.model.StoreInventoryDto;
import com.example.nexus.domain.user.model.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Tag(name = "가맹점 관리", description = "가맹점 정보 등록, 조회, 수정, 매출 정산 및 재고 관리 API")
@RequestMapping("/store")
@RestController
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;
    private final StoreIncomeService storeIncomeService;


    // [본사] 선택한 가맹점 store_idx로 재고 조회
    @Operation(summary = "가맹점별 재고 조회", description = "본사 관리자가 특정 가맹점의 재고 현황을 조회합니다.")
    @GetMapping("/inventory/list/{storeIdx}")
    public ResponseEntity<BaseResponse<PageResponse<StoreInventoryDto.ListRes>>> listByStoreIdx(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "가맹점 번호") @PathVariable Long storeIdx,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size) {
        if (userDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        PageResponse<StoreInventoryDto.ListRes> result = storeService.listByStoreIdxPaged(storeIdx, page, size);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // [본사] keyword로 가맹점 검색
    @Operation(summary = "가맹점 이름 검색", description = "가맹점 명칭(키워드)으로 가맹점을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<List<StoreDto.StoreSearchRes>>> searchStore(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "검색할 가맹점 이름의 일부 또는 전체를 입력하세요. (예: 서울, 강남 등). 아무것도 입력하지 않으면 전체 가맹점이 조회될 수 있습니다.", example = "서울") @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword) {
        if (userDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        StoreDto.StoreSearchReq reqDto = new StoreDto.StoreSearchReq(keyword);

        List<StoreDto.StoreSearchRes> result = storeService.searchByStoreName(reqDto);

        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // 가맹점 목록 조회 및 검색 조건
    @Operation(summary = "가맹점 목록 조회", description = "전체 가맹점 목록을 페이징하여 조회합니다. 상태 및 키워드 필터를 지원합니다.")
    @GetMapping("/list")
    public ResponseEntity<BaseResponse<StoreDto.StorePageRes>> storeList(
            @ParameterObject StoreDto.StoreSearchPagingReq searchReq,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size
    ){
        StoreDto.StorePageRes result = storeService.storeList(searchReq, page,size);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    @Operation(summary = "가맹점 전체 상태 집계", description = "현재 운영 중이거나 폐점된 가맹점의 전체 숫자를 집계하여 조회합니다.")
    @GetMapping("/totalCount/list")
    public ResponseEntity<BaseResponse<StoreDto.StoreTotalRes>> storeTotalList(){
        StoreDto.StoreTotalRes result = storeService.storeTotalList();
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // 월별 입점/폐점 추이 (연도 선택) - 본사 가맹점 관리 화면 차트용
    @Operation(summary = "월별 입점/폐점 추이 조회", description = "특정 연도의 월별 신규 입점 및 폐점 가맹점 추이 데이터를 조회합니다.")
    @GetMapping("/stats/monthly")
    public ResponseEntity<BaseResponse<StoreDto.MonthlyTrendRes>> monthlyTrend(
            @Parameter(description = "조회할 연도 (예: 2024). 미입력시 올해 기준") @RequestParam(required = false) Integer year){
        StoreDto.MonthlyTrendRes result = storeService.getMonthlyTrend(year);
        return ResponseEntity.ok(BaseResponse.success(result));
    }


    // 가맹점 목록 상세 조회
    @Operation(summary = "가맹점 상세 정보 조회", description = "특정 가맹점의 상세 정보(점주 명, 연락처, 주소 등)를 조회합니다.")
    @GetMapping("/detail/list/{storeIdx}")
    public ResponseEntity<BaseResponse<StoreDto.StoreDetailListRes>> storeDetailList(@Parameter(description = "가맹점 번호") @PathVariable Long storeIdx){
        StoreDto.StoreDetailListRes result = storeService.storeDetailList(storeIdx);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // 신규 가맹점 등록
    @Operation(summary = "신규 가맹점 등록", description = "새로운 가맹점 정보를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "점주를 찾을 수 없음", content = @Content(schema = @Schema(example = "{\"success\":false, \"code\":404, \"message\":\"존재하지 않는 유저입니다.\"}"))),
            @ApiResponse(responseCode = "409", description = "중복 데이터 발생 (가맹점 보유, 가맹점명 중복, 사업자번호 중복)", content = @Content(schema = @Schema(example = "{\"success\":false, \"code\":409, \"message\":\"이미 가맹점을 보유한 점주입니다.\"}")))
    })
    @PostMapping("/new/register")
    public ResponseEntity<BaseResponse<String>> storeReg(@RequestBody StoreDto.StoreRegReq dto ){
        storeService.storeReg(dto);
        return ResponseEntity.ok(BaseResponse.success("성공"));
    }

    // S3 Pre-signed URL 요청
    @Operation(summary = "S3 업로드용 Pre-signed URL 생성", description = "가맹점 이미지 등을 S3에 직접 업로드하기 위한 Pre-signed URL을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "파일 용량 초과", content = @Content(schema = @Schema(example = "{\"success\":false, \"code\":400, \"message\":\"PDF 파일 용량을 초과했습니다.\"}")))
    })
    @GetMapping("/presignedUrl/{fileName}")
    public ResponseEntity<BaseResponse<Map<String, String>>> presignedUrl(
            @Parameter(description = "업로드할 파일명 (확장자 포함)", example = "business_license.pdf") @PathVariable(name = "fileName") String fileName,
            @Parameter(description = "파일 크기 (Byte 단위)", example = "1024") @RequestParam(name = "fileSize") long fileSize){
        // 1. 서비스를 호출하여 URL과 고유 파일명을 받아옵니다.
        Map<String, String> result = storeService.getPresignedUrl(fileName, fileSize);

        // 2. BaseResponse.success에 결과 Map을 담아 반환합니다.
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // 가맹점 수정
    @Operation(summary = "가맹점 정보 수정", description = "기존 가맹점의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "이미 폐점 처리된 가맹점", content = @Content(schema = @Schema(example = "{\"success\":false, \"code\":400, \"message\":\"이미 폐점 처리된 가맹점입니다.\"}"))),
            @ApiResponse(responseCode = "409", description = "가맹점명 중복", content = @Content(schema = @Schema(example = "{\"success\":false, \"code\":409, \"message\":\"이미 존재하는 가맹점명입니다.\"}")))
    })
    @PutMapping("/update/{storeIdx}")
    public ResponseEntity<BaseResponse<String>> storeUpdate(
            @Parameter(description = "가맹점 번호") @PathVariable(name = "storeIdx") Long storeIdx,
            @RequestBody StoreDto.StoreUpdateReq dto){
        storeService.storeUpdate(storeIdx, dto);
        return ResponseEntity.ok(BaseResponse.success("성공"));
    }

    // 가맹점별 매출 정산 조회
    @Operation(summary = "가맹점 매출 정산 조회", description = "로그인한 가맹점주가 특정 월의 매출 및 POS 정산 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "가맹점 매출 정산 조회 성공",
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
                                            "monthlyTotalAmount": 8500000,
                                            "payHistory": {
                                              "content": [
                                                {
                                                  "posPayIdx": 1,
                                                  "menuNames": "아메리카노 외 2건",
                                                  "payCount": 3,
                                                  "paidDate": "2026-05-20",
                                                  "payAmount": 25000
                                                }
                                              ],
                                              "page": {
                                                "size": 10,
                                                "number": 0,
                                                "totalElements": 1,
                                                "totalPages": 1
                                              }
                                            }
                                          }
                                        }
                                        """
                            )
                    )
            )
    })
    @GetMapping("/income/settlement")
    public ResponseEntity<BaseResponse<StoreIncomeDto.TotalSettlementRes>> getSettlement(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUserDetails userDetails,
            @Parameter(description = "조회할 연도", example = "2024") @RequestParam int year,
            @Parameter(description = "조회할 월", example = "5") @RequestParam int month,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        if (userDetails == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "로그인이 필요합니다.");
        }

        StoreIncomeDto.TotalSettlementRes result = storeIncomeService.getSettlementData(
                userDetails.getIdx(), year, month, page, size
        );

        return ResponseEntity.ok(BaseResponse.success(result));
    }
}
