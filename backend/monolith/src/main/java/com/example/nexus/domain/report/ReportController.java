package com.example.nexus.domain.report;


import com.example.nexus.common.model.BaseResponse;
import com.example.nexus.domain.report.model.ReportDto;
import com.example.nexus.domain.user.model.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "AI 보고서", description = "AI 챗봇(클로드) 기반 데이터 분석 및 보고서 자동 생성/조회 API")
@RequestMapping("/report")
@RestController
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    // 사용자 메세지 통신
    @Operation(summary = "AI 챗봇 대화 및 보고서 생성", description = "AI 챗봇에게 데이터 분석 및 요약을 요청하고, 그 결과로 생성된 보고서를 저장합니다.")
    @PostMapping("/generate")
    public ResponseEntity<BaseResponse<String>> requestReport(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUserDetails userDetails,
            @Valid @RequestBody ReportDto.ChatRequest request) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        Long userIdx = userDetails.getIdx();
        String result = reportService.createAndSaveReport(userIdx, request.message(), request.sessionId());
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // 보고서 조회
    @Operation(summary = "저장된 AI 보고서 목록 조회", description = "로그인한 사용자가 AI 챗봇을 통해 생성하고 저장해둔 보고서 목록을 페이징하여 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<BaseResponse> reportList(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUserDetails userDetails,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size){

        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        Long userIdx = userDetails.getIdx();
        ReportDto.ReportPageRes result = reportService.reportList(userIdx, page, size);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

}
