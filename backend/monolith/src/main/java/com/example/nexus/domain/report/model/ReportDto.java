package com.example.nexus.domain.report.model;

import com.example.nexus.domain.store.model.StoreDto;
import com.example.nexus.domain.user.model.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public class ReportDto {

    // 사용자 요청 DTO
    public record ChatRequest(
            @Schema(description = "보고서 생성을 위한 AI 요청 메시지", example = "지난주 대비 이번주 매출 변화와 원자재 소모량에 대해 분석해줘")
            @NotBlank(message = "메시지는 비어있을 수 없습니다.")
            String message,
            @Schema(description = "대화 세션 식별자 (선택값, 미입력시 사용자 기준 기본 세션 사용)", example = "session-123")
            String sessionId   // 대화 세션 식별자 (선택값, 없으면 서버에서 userIdx 기반 기본값 사용)
    ) {}

    // DB에서 데이터를 가져올때 사용하는 DTO
    @Builder
    public record ReportDataSummaryDto(
            Long totalSales,          // 최근 30일 총 매출액
            List<String> topProducts  // 인기 상품 TOP 3
    ) {}

    @Getter
    @Builder
    public static class ReportList{
        private Long idx;
        private String reportTitle;
        private String reportFilePath;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime createdAt;

        public static ReportDto.ReportList from(Report entity){
            return ReportList.builder()
                    .idx(entity.getIdx())
                    .reportTitle(entity.getReportTitle())
                    .reportFilePath(entity.getReportFilePath())
                    .createdAt(entity.getCreatedAt())
                    .build();
        }
    }

    // 페이지 조회
    @Getter
    @Builder
    public static class ReportPageRes{
        private List<ReportDto.ReportList> reportList;
        private int totalPage;
        private long totalCount;
        private int currentPage;
        private int currentSize;

        public static ReportDto.ReportPageRes from(Page<Report> entity){
            return ReportPageRes.builder()
                    .reportList(entity.map(ReportList::from).getContent())
                    .totalPage(entity.getTotalPages())
                    .totalCount(entity.getTotalElements())
                    .currentPage(entity.getNumber())
                    .currentSize(entity.getSize())
                    .build();
        }
    }
}