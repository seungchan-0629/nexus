package com.example.nexus.domain.menu.model;

import com.example.nexus.common.exception.BaseException;
import com.example.nexus.domain.product.model.Product;
import jakarta.persistence.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.nexus.common.model.BaseResponseStatus.*;

public class MenuDto {

    @Builder
    @Getter
    public static class MenuListRes{
        private Long idx;
        private String menuName;
        private Integer price;
        private String menuCategory;
        private Integer menuItemCount;


        public static MenuDto.MenuListRes from(Menu entity){
            return MenuListRes.builder()
                    .idx(entity.getIdx())
                    .menuName(entity.getMenuName())
                    .price(entity.getPrice())
                    .menuCategory(entity.getMenuCategory().getMenuCategoryName())
                    .menuItemCount(entity.getMenuItemList().size())
                    .build();
        }
    }

    // 메뉴 페이징 조회
    @Getter
    @Builder
    public static class MenuPageRes{
        private List<MenuListRes> menuList;
        private int totalPage;
        private long totalCount;
        private int currentPage;
        private int currentSize;

        public static MenuPageRes from(Page<Menu> entity){
            return MenuPageRes.builder()
                    .menuList(entity.map(MenuDto.MenuListRes::from).getContent())
                    .totalPage(entity.getTotalPages())
                    .totalCount(entity.getTotalElements())
                    .currentPage(entity.getPageable().getPageNumber())
                    .currentSize(entity.getPageable().getPageSize())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ItemList{
        private Long idx;
        private Long productIdx;
        private String productName;
        private Integer quantity;
        private String menuUnit;


        public static ItemList from(MenuItem entity){
            return ItemList.builder()
                    .idx(entity.getIdx())
                    .productIdx(entity.getProduct().getIdx())
                    .productName(entity.getProduct().getProductName())
                    .quantity(entity.getQuantity())
                    .menuUnit(entity.getMenuUnit())
                    .build();
        }

    }

    // 메뉴 재료 조회
    @Getter
    @Builder
    public static class MenuItemListRes{
        private Long idx;
        private String menuName;
        private String menuCategory;
        private Integer price;
        private String imgPath;
        private List<ItemList> menuItemList;

        public static MenuDto.MenuItemListRes from(Menu entity){
            return MenuItemListRes.builder()
                    .idx(entity.getIdx())
                    .menuName(entity.getMenuName())
                    .menuCategory(entity.getMenuCategory().getMenuCategoryName())
                    .price(entity.getPrice())
                    .imgPath(entity.getImgPath())
                    .menuItemList(entity.getMenuItemList().stream()
                            .map(ItemList::from)
                            .toList())
                    .build();
        }
    }

    @Getter
    public static class MenuItemReq{
        @Schema(description = "소모량", example = "1")
        private Integer quantity;
        @Schema(description = "단위", example = "개")
        private String menuUnit;
        @Schema(description = "원자재 식별자(ID)", example = "1")
        private Long productIdx;

        public MenuItem toEntity(Menu menu, Product product) {
            return MenuItem.builder()
                    .quantity(this.quantity)
                    .menuUnit(this.menuUnit)
                    .product(product) // 조회된 진짜 객체 주입
                    .menu(menu)
                    .build();
        }
    }

    // 메뉴 등록
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuRegReq{
        @Schema(description = "메뉴 이름", example = "아이스 아메리카노")
        private String menuName;
        @Schema(description = "가격", example = "4500")
        private Integer price;
        @Schema(description = "메뉴 이미지 S3 URL", example = "https://s3.amazonaws.com/venti/ice_americano.jpg")
        private String imgPath;
        @Schema(description = "카테고리 식별자(ID)", example = "1")
        private Long menuCategoryIdx;
        @Schema(description = "레시피 (원자재 구성)")
        private List<MenuItemReq> menuItemList;

        public Menu toEntity(MenuCategory category, List<Product> products) {
            Menu menu = Menu.builder()
                    .menuName(this.menuName)
                    .price(this.price)
                    .imgPath(this.imgPath)
                    .menuCategory(category) // 주입받은 카테고리 설정
                    .build();

            if (this.menuItemList != null) {
                Map<Long, Product> productMap = products.stream()
                        .collect(Collectors.toMap(Product::getIdx, p -> p));

                for (MenuItemReq itemDto : this.menuItemList) {
                    Product product = productMap.get(itemDto.getProductIdx());
                    if (product == null) {
                        throw new BaseException(NOT_FOUND_PRODUCT);
                    }

                    // 1. MenuItem 생성 시 'menu' 객체를 넘겨주어 자식에게 부모를 알려줌
                    MenuItem menuItem = itemDto.toEntity(menu, product);

                    // 2. 부모 객체의 리스트에도 자식을 추가 (양방향 연결)
                    menu.getMenuItemList().add(menuItem);
                }
            }
            return menu;
        }
    }

    // 메뉴 검색
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuSearchPagingReq {
        @Schema(description = "메뉴 이름 검색 키워드입니다. 아무것도 적지 않거나 비워둘 경우, 키워드 필터링 없이 전체 메뉴를 조회합니다.", example = "")
        private String keyword;
        @Schema(description = "조회할 카테고리의 고유 식별자(ID)입니다. 전체 카테고리를 대상으로 검색하려면 이 값을 지우고 비워주세요.", example = "1")
        private Long categoryIdx;
    }

    // 카테고리 조회
    @Getter
    @Builder
    public static class MenuCategoryRes{
        private Long categoryIdx;
        private String menuCategoryName;

        public static MenuDto.MenuCategoryRes from(MenuCategory entity){
            return MenuCategoryRes.builder()
                    .categoryIdx(entity.getIdx())
                    .menuCategoryName(entity.getMenuCategoryName())
                    .build();
        }
    }

    // 카테고리 등록 Req
    @Getter
    public static class MenuCategoryRegReq{
        @Schema(description = "신규 카테고리 이름", example = "프라페")
        private String menuCategoryName;
    }

}
