package com.example.nexus.domain.category.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CategoryDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "CategoryRegReq", description = "카테고리 등록/수정 요청")
    public static class RegReq {
        @Schema(description = "카테고리 번호", example = "1")
        private Long idx;
        @Schema(description = "카테고리 이름", example = "원두/커피")
        private String categoryName;

        public Category toEntity() {
            return Category.builder()
                    .categoryName(this.categoryName)
                    .build();
        }
    }

    @Builder
    @Getter
    @Schema(name = "CategoryRegRes", description = "카테고리 등록 결과")
    public static class RegRes {
        private Long idx;
        private String categoryName;

        public static RegRes from(Category entity) {
            return RegRes.builder()
                    .idx(entity.getIdx())
                    .categoryName(entity.getCategoryName())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ListRes {
        private Long idx;
        private String categoryName;

        public static ListRes from(Category entity) {
            return ListRes.builder()
                    .idx(entity.getIdx())
                    .categoryName(entity.getCategoryName())
                    .build();
        }
    }
}
