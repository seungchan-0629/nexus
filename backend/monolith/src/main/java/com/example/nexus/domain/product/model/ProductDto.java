package com.example.nexus.domain.product.model;

import com.example.nexus.domain.category.model.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

public class ProductDto {
    @Getter
    @Schema(name = "ProductRegReq", description = "제품 등록 및 수정 요청")
    public static class RegReq {
        @Schema(description = "제품 번호 (수정 시 필수)", example = "1")
        private Long idx;

        @Schema(description = "제품명", example = "우유")
        private String productName;

        @Schema(description = "카테고리 번호", example = "2")
        private Long categoryIdx;

        @Schema(description = "제품 단위", example = "L")
        private String productUnit;

        @Schema(description = "최대 재고량", example = "100")
        private Integer maxStock;

        @Schema(description = "최소 재고량", example = "20")
        private Integer minStock;

        @Schema(description = "단가", example = "10000")
        private Integer unitPrice;

        @Schema(description = "위험 재고 판단 기준 일수", example = "3")
        private String dangerDays;

        public Product toEntity(Category category) {
            return Product.builder()
                    .productName(this.productName)
                    .category(category)
                    .productUnit(this.productUnit)
                    .maxStock(this.maxStock)
                    .minStock(this.minStock)
                    .unitPrice(this.unitPrice)
                    .dangerDays(this.dangerDays)
                    .build();
        }

        public Long getIdx() {
            return idx;
        }
    }

    @Getter
    @Builder
    @Schema(name = "ProductRegRes", description = "제품 등록 결과")
    public static class RegRes {
        @Schema(description = "생성된 제품 번호", example = "10")
        private Long idx;

        @Schema(description = "등록된 제품명", example = "감자 10kg")
        private String productName;

        public static RegRes from(Product entity) {
            return RegRes.builder()
                    .idx(entity.getIdx())
                    .productName(entity.getProductName())
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(description = "제품 상세 정보")
    public static class ListRes {
        @Schema(description = "제품 번호", example = "1")
        private Long idx;

        @Schema(description = "제품명", example = "감자 10kg")
        private String productName;

        @Schema(description = "카테고리명", example = "채소류")
        private String categoryName;

        @Schema(description = "단위", example = "박스")
        private String productUnit;

        @Schema(description = "최대 재고량", example = "100")
        private Integer maxStock;

        @Schema(description = "최소 재고량", example = "20")
        private Integer minStock;

        @Schema(description = "단가", example = "25000")
        private Integer unitPrice;

        @Schema(description = "위험 재고 판단 기준 일수", example = "3")
        private String dangerDays;


        public static ListRes from(Product entity) {
            return ListRes.builder()
                    .idx(entity.getIdx())
                    .productName(entity.getProductName())
                    .categoryName(entity.getCategory().getCategoryName())
                    .productUnit(entity.getProductUnit())
                    .maxStock(entity.getMaxStock())
                    .minStock(entity.getMinStock())
                    .unitPrice(entity.getUnitPrice())
                    .dangerDays(entity.getDangerDays())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ProductPageRes {
        private List<ListRes> productList;
        private int totalPage;
        private long totalCount;
        private int currentPage;
        private int currentSize;

        public static ProductPageRes from(Page<Product> entity) {
            return ProductPageRes.builder()
                    .productList(entity.map(ListRes::from).getContent())
                    .totalPage(entity.getTotalPages())
                    .totalCount(entity.getTotalElements())
                    .currentPage(entity.getPageable().getPageNumber())
                    .currentSize(entity.getPageable().getPageSize())
                    .build();
        }
    }
}
