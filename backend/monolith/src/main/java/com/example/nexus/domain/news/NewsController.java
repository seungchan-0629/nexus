package com.example.nexus.domain.news;

import com.example.nexus.common.model.BaseResponse;
import com.example.nexus.domain.news.model.NewsDto;
import com.example.nexus.domain.store.StoreService;
import com.example.nexus.domain.user.model.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "뉴스", description = "AI 뉴스 수집·요약 조회. 본사는 업계 전체 뉴스, 매장은 해당 매장 관련 뉴스 제공.")
@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;
    private final StoreService storeService;

    @Operation(
            summary = "뉴스 요약 목록 조회",
            description = """
                    날짜별 뉴스 AI 요약 목록 조회.

                    - storeIdx 미입력: 본사(HQ) 대상 업계 뉴스
                    - storeIdx 입력: 해당 매장 관련 뉴스
                    - date 미입력: 오늘 날짜 기준
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "뉴스 목록 조회 성공 (해당 날짜 데이터 없으면 빈 배열 반환)")
    })
    @GetMapping
    public ResponseEntity<BaseResponse<List<NewsDto.SummaryListItem>>> list(
            @Parameter(description = "조회 날짜 (yyyy-MM-dd, 생략 시 오늘)", example = "2026-05-27")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "매장 ID (생략 시 본사 뉴스)", example = "1")
            @RequestParam(required = false) Long storeIdx) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(BaseResponse.success(newsService.listSummaries(targetDate, storeIdx)));
    }

    @Operation(
            summary = "뉴스 요약 상세 조회",
            description = "특정 뉴스 요약 ID로 전체 본문 조회. summaryContentsPreview 대신 summaryContents(전문) 반환."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "뉴스 상세 조회 성공"),
            @ApiResponse(responseCode = "400", description = "데이터를 찾을 수 없습니다. (NOT_FOUND_DATA)",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {"success":false,"code":4002,"message":"데이터를 찾을 수 없습니다.","result":null}""")))
    })
    @GetMapping("/{idx}")
    public ResponseEntity<BaseResponse<NewsDto.SummaryDetail>> detail(
            @Parameter(description = "뉴스 요약 ID", example = "1")
            @PathVariable Long idx) {
        return ResponseEntity.ok(BaseResponse.success(newsService.getSummary(idx)));
    }

    @Operation(
            summary = "내 매장 뉴스 조회",
            description = "로그인한 가맹점주의 매장 ID를 자동으로 추출하여 해당 매장 관련 뉴스 목록 반환. 인증 필요."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 매장 뉴스 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다.",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {"success":false,"code":401,"message":"로그인이 필요합니다.","result":null}"""))),
            @ApiResponse(responseCode = "400", description = "등록된 가맹점 정보를 찾을 수 없습니다. (STORE_NOT_FOUND)",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {"success":false,"code":3105,"message":"해당 가맹점 정보를 찾을 수 없습니다.","result":null}""")))
    })
    @GetMapping("/my")
    public ResponseEntity<BaseResponse<List<NewsDto.SummaryListItem>>> myStore(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "조회 날짜 (yyyy-MM-dd, 생략 시 오늘)", example = "2026-05-27")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        Long storeIdx = storeService.findStoreIdx(authUserDetails.getIdx());
        return ResponseEntity.ok(BaseResponse.success(newsService.listSummaries(targetDate, storeIdx)));
    }

    @Operation(
            summary = "뉴스 수집 수동 트리거",
            description = """
                    AI 뉴스 수집 파이프라인을 즉시 실행 (자동 스케줄러 실패 시 복구용).

                    동작:
                    - 본사(HQ) 대상 뉴스 크롤링 및 AI 요약
                    - 각 매장별 관련 뉴스 필터링 및 요약

                    응답: hqInserted(본사 수집 건수), storeInserted(매장 수집 건수), summariesInserted(요약 생성 건수), message
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "뉴스 수집 파이프라인 실행 성공"),
            @ApiResponse(responseCode = "500", description = "뉴스 수집 중 서버 오류 발생",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {"success":false,"code":5000,"message":"요청 실패","result":null}""")))
    })
    @PostMapping("/collect")
    public ResponseEntity<BaseResponse<NewsDto.CollectRunResult>> collect() {
        return ResponseEntity.ok(BaseResponse.success(newsService.runDailyPipeline()));
    }
}
