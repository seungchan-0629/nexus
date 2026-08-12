package com.example.nexus.domain.product;

import com.example.nexus.common.model.BaseResponse;
import com.example.nexus.domain.product.model.ProductDto;
import com.example.nexus.domain.user.model.AuthUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "제품 관리", description = "원자재 제품 등록, 조회, 수정, 삭제 관리 API (본사 및 가맹점용)")
@RequestMapping("/product")
@RestController
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    // 신규 제품 등록
    @Operation(summary = "신규 제품 등록", description = "새로운 원자재 제품을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "제품 등록 성공",
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
                                            "idx": 1,
                                            "productName": "우유"
                                          }
                                        }
                                        """
                            )
                    )
            )
    })
    @PostMapping("/reg")
    public ResponseEntity<BaseResponse<ProductDto.RegRes>> addNewProduct(@Valid @RequestBody ProductDto.RegReq dto) {
        ProductDto.RegRes result = productService.addProduct(dto);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // 제품 목록으로 조회
    @Operation(summary = "제품 목록 조회", description = "전체 제품 목록을 페이징하여 조회합니다. 카테고리 필터를 지원합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "제품 목록 조회 성공",
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
                                            "productList": [
                                              {
                                                "idx": 2,
                                                "productName": "디카페인 원두",
                                                "categoryName": "원두/커피",
                                                "productUnit": "kg",
                                                "maxStock": 30,
                                                "minStock": 5,
                                                "unitPrice": 18000,
                                                "dangerDays": "30"
                                              }
                                            ],
                                            "totalPage": 1,
                                            "totalCount": 1,
                                            "currentPage": 0,
                                            "currentSize": 10
                                          }
                                        }
                                        """
                            )
                    )
            )
    })
    @GetMapping("/list")
    public ResponseEntity<BaseResponse<ProductDto.ProductPageRes>> readProductList(
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "카테고리명 필터") @RequestParam(required = false) String categoryName) {

        Pageable pageable = PageRequest.of(page, size);
        ProductDto.ProductPageRes result = productService.findAllProduct(pageable, categoryName);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // 기존 제품 수정
    @Operation(summary = "제품 정보 수정", description = "기존 제품의 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "제품 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "success": true,
                                          "code": 1000,
                                          "message": "success modify product",
                                          "result": "success modify product"
                                        }
                                        """
                            )
                    )
            )
    })
    @PutMapping("/update")
    public ResponseEntity<BaseResponse<String>> updateProduct(@Valid @RequestBody ProductDto.RegReq dto) {
        boolean isModified = productService.updateProduct(dto.getIdx(), dto);

        if (isModified) {
            return ResponseEntity.ok(BaseResponse.success("success modify product"));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.success("fail to modify: product or category not found"));
    }

    // 기존 제품 삭제
    @Operation(summary = "제품 삭제", description = "기존 제품을 삭제(소프트 삭제) 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "제품 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "success": true,
                                          "code": 1000,
                                          "message": "success delete product",
                                          "result": "success delete product"
                                        }
                                        """
                            )
                    )
            )
    })
    @DeleteMapping("/delete")
    public ResponseEntity<BaseResponse<String>> deleteProduct(@RequestBody ProductDto.RegReq dto) {

        boolean isDeleted = productService.deleteProduct(dto.getIdx());

        if (isDeleted) {
            return ResponseEntity.ok(BaseResponse.success("success delete product"));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.success("fail to delete: product not found"));
    }

    // 본사 제품 검색
    @Operation(summary = "본사 제품 검색", description = "제품명으로 제품을 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "제품 검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "success": true,
                                          "code": 1000,
                                          "message": "요청에 성공하였습니다.",
                                          "result": [
                                            {
                                                "idx": 2,
                                                "productName": "디카페인 원두",
                                                "categoryName": "원두/커피",
                                                "productUnit": "kg",
                                                "maxStock": 30,
                                                "minStock": 5,
                                                "unitPrice": 18000,
                                                "dangerDays": "30"
                                            }
                                          ]
                                        }
                                        """
                            )
                    )
            )
    })
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<List<ProductDto.ListRes>>> searchProduct(@Parameter(description = "제품명 검색어") @RequestParam String productName) {
        List<ProductDto.ListRes> result = productService.searchProduct(productName);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // 가맹점 별 제품 조회
    @Operation(summary = "가맹점 제품 조회", description = "로그인한 가맹점주가 발주 가능한 제품 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "가맹점 제품 조회 성공",
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
                                            "content": [
                                              {
                                                "idx": 2,
                                                "productName": "디카페인 원두",
                                                "categoryName": "원두/커피",
                                                "productUnit": "kg",
                                                "maxStock": 30,
                                                "minStock": 5,
                                                "unitPrice": 18000,
                                                "dangerDays": "30"
                                              }
                                            ],
                                            "pageable": { "pageNumber": 0, "pageSize": 10 },
                                            "totalPages": 1,
                                            "totalElements": 1
                                          }
                                        }
                                        """
                            )
                    )
            )
    })
    @GetMapping("/store")
    public ResponseEntity<BaseResponse<Page<ProductDto.ListRes>>> getMyStoreProducts(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        if (authUserDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        Pageable pageable = PageRequest.of(page, size);
        Long storeIdx = authUserDetails.getIdx();

        Page<ProductDto.ListRes> result = productService.findProductsByStore(storeIdx, pageable);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // 가맹점 별 제품 검색
    @Operation(summary = "가맹점 제품 검색", description = "로그인한 가맹점주가 발주 가능한 제품을 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "가맹점 제품 검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "success": true,
                                          "code": 1000,
                                          "message": "요청에 성공하였습니다.",
                                          "result": [
                                            {
                                                "idx": 2,
                                                "productName": "디카페인 원두",
                                                "categoryName": "원두/커피",
                                                "productUnit": "kg",
                                                "maxStock": 30,
                                                "minStock": 5,
                                                "unitPrice": 18000,
                                                "dangerDays": "30"
                                            }
                                          ]
                                        }
                                        """
                            )
                    )
            )
    })
    @GetMapping("/store/search")
    public ResponseEntity<BaseResponse<List<ProductDto.ListRes>>> searchInMyStore(
            @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "제품명 검색어") @RequestParam String productName
    ) {
        if (authUserDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        Long storeIdx = authUserDetails.getIdx();

        List<ProductDto.ListRes> result = productService.searchProductInStore(storeIdx, productName);

        return ResponseEntity.ok(BaseResponse.success(result));
    }
}
