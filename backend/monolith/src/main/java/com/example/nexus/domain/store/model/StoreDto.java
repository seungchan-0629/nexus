package com.example.nexus.domain.store.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StoreDto {

    @Builder
    @Getter
    // 가맹점 리스트 조회
    public static class StoreListRes{
        private Long idx;
        private String storeName;
        private String ownerName;
        private String ownerEmail;
        private String address;
        private String business;
        private String status;


        public static StoreDto.StoreListRes from(Store entity){
            return StoreListRes.builder()
                    .idx(entity.getIdx())
                    .storeName(entity.getStoreName())
                    .ownerName(entity.getUser().getUserName())
                    .ownerEmail(entity.getUser().getEmail())
                    .address(entity.getAddress())
                    .business(entity.getBusiness())
                    .status(entity.isDeleted() ? "폐점":"입점")
                    .build();
        }
    }

    // 가맹점 조회 페이징 처리
    @Getter
    @Builder
    public static class StorePageRes{
        private List<StoreDto.StoreListRes> storeList;
        private int totalPage;
        private long totalCount;
        private int currentPage;
        private int currentSize;

        public static StoreDto.StorePageRes from(Page<Store> entity){
            return StorePageRes.builder()
                    .storeList(entity.map(StoreDto.StoreListRes::from).getContent())
                    .totalPage(entity.getTotalPages())
                    .totalCount(entity.getTotalElements())
                    .currentPage(entity.getPageable().getPageNumber())
                    .currentSize(entity.getPageable().getPageSize())
                    .build();
        }
    }

    // 가맹점 수 조회
    @Getter
    @Builder
    public static class StoreTotalRes{
        private Long totalCount;   // 총 가맹점 수
        private Long activeCount;  // 입점
        private Long closedCount;  // 폐점
    }

    // 월별 입점/폐점 추이 (최근 12개월) - 프론트 차트가 labels/openedCounts/closedCounts를 그대로 사용
    @Getter
    @Builder
    public static class MonthlyTrendRes{
        private List<String> labels;        // 월 라벨 (예: "2025-06")
        private List<Long> openedCounts;    // 월별 입점(신규 등록) 건수
        private List<Long> closedCounts;    // 월별 폐점 건수
    }

    @Builder
    @Getter
    // 가맹점 목록 상세 조회
    public static class StoreDetailListRes{
        private Long idx;
        private String storeName;
        private String ownerName;
        private String ownerEmail;
        private String postcode;
        private String address;
        private String addressDetail;
        private String totalAddress;
        private String business;
        private String createdAt;
        private String closedAt;
        private String filePath;

        public static StoreDto.StoreDetailListRes from(Store entity){

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            return StoreDetailListRes.builder()
                    .idx(entity.getIdx())
                    .storeName(entity.getStoreName())
                    .ownerName(entity.getUser().getUserName())
                    .ownerEmail(entity.getUser().getEmail())
                    .postcode(entity.getPostcode())
                    .address(entity.getAddress())
                    .addressDetail(entity.getAddressDetail())
                    .totalAddress(String.format("[%s] %s %s",
                            entity.getPostcode(),
                            entity.getAddress(),
                            entity.getAddressDetail()).trim())
                    .business(entity.getBusiness())
                    .createdAt(entity.getCreatedAt().format(formatter))
                    .closedAt(
                            entity.getClosedAt() == null && !entity.isDeleted()
                                    ? "운영 중"
                                    : entity.getClosedAt().format(formatter)

                    )
                    .filePath(entity.getFilePath())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreSearchReq {
        private String keyword;
    }

    // 가맹점 검색
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreSearchPagingReq {
        @Schema(description = "가맹점 이름 검색 키워드입니다. 아무것도 적지 않거나 비워둘 경우, 키워드 필터링 없이 전체 가맹점을 조회합니다.", example = "")
        private String keyword;
        @Schema(description = "가맹점 운영 상태 필터입니다 (예: 영업중, 폐업 등). 특정 상태만 조회하고 싶지 않다면 이 칸을 비워주세요 (기본 전체 조회).", example = "")
        private String status;
    }

    @Builder
    @Getter
    public static class StoreSearchRes {
        private Long idx;
        private String storeName;
        private String address;

        public static StoreSearchRes from(Store entity) {
            return StoreSearchRes.builder()
                    .idx(entity.getIdx())
                    .storeName(entity.getStoreName())
                    .address(entity.getAddress())
                    .build();
        }
    }

    // 신규 가맹점 등록 req
    @Getter
    public static class StoreRegReq{
        @Schema(description = "가맹점 이름", example = "더벤티 강남역점")
        private String storeName;
        @Schema(description = "가맹점주 이메일 (회원가입된 유저의 이메일)", example = "gangnam@theventi.co.kr")
        private String ownerEmail;
        @Schema(description = "우편번호", example = "06236")
        private String postcode;
        @Schema(description = "기본 주소", example = "서울 강남구 테헤란로 152")
        private String address;
        @Schema(description = "상세 주소", example = "1층 더벤티")
        private String addressDetail;
        @Schema(description = "사업자 등록번호", example = "123-45-67890")
        private String business;
        @Schema(description = "첨부파일(사업자 등록증 등) S3 URL", example = "https://s3.amazonaws.com/venti/document.jpg")
        private String filePath;

        public Store toEntity(){
            return Store.builder()
                    .storeName(this.storeName)
                    .postcode(this.postcode)
                    .address(this.address)
                    .addressDetail(this.addressDetail)
                    .business(this.business)
                    .filePath(this.filePath)
                    .build();
        }
    }

    // 가맹점 수정
    @Getter
    @Builder
    public static class StoreUpdateReq{
        @Schema(description = "가맹점 이름", example = "더벤티 강남역점 (수정)")
        private String storeName;
        @Schema(description = "점주 이름", example = "홍길동")
        private String ownerName;
        @Schema(description = "가맹점주 이메일", example = "gangnam@theventi.co.kr")
        private String ownerEmail;
        @Schema(description = "우편번호", example = "06236")
        private String postcode;
        @Schema(description = "기본 주소", example = "서울 강남구 테헤란로 152")
        private String address;
        @Schema(description = "상세 주소", example = "2층 201호")
        private String addressDetail;
        @Schema(description = "폐점 일시 (운영중일 경우 null)", example = "2026-05-27T07:08:38.615Z")
        private LocalDateTime closedAt;
        @Schema(description = "첨부파일(사업자 등록증 등) S3 URL", example = "https://s3.amazonaws.com/venti/image.jpg")
        private String filePath;

    }
}
