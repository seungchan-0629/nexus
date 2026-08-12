package com.example.nexus.domain.category;

import com.example.nexus.common.model.BaseResponse;
import com.example.nexus.domain.category.model.CategoryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "카테고리 관리", description = "원자재 카테고리 등록, 조회, 삭제 관리 API")
@RequestMapping("/category")
@RequiredArgsConstructor
@RestController
public class CategoryController {
    private final CategoryService categoryService;

    // 카테고리 등록
    @Operation(summary = "카테고리 등록", description = "새로운 원자재 카테고리를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "카테고리 등록 성공",
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
                                            "categoryName": "원두/커피"
                                          }
                                        }
                                        """
                            )
                    )
            )
    })
    @PostMapping("/reg")
    public ResponseEntity<BaseResponse<CategoryDto.RegRes>> createCategory(@Valid @RequestBody CategoryDto.RegReq dto) {
        CategoryDto.RegRes result = categoryService.addCategory(dto);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // 카테고리 목록 조회
    @Operation(summary = "카테고리 목록 조회", description = "등록된 모든 카테고리 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "카테고리 목록 조회 성공",
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
                                              "idx": 1,
                                              "categoryName": "원두/커피"
                                            },
                                            {
                                              "idx": 2,
                                              "categoryName": "유제품"
                                            }
                                          ]
                                        }
                                        """
                            )
                    )
            )
    })
    @GetMapping("/list")
    public ResponseEntity readCategoryList() {
        List<CategoryDto.ListRes> dto = categoryService.findAllCategories();
        return ResponseEntity.ok(BaseResponse.success(dto));
    }

    // 카테고리 소프트 삭제
    @Operation(summary = "카테고리 삭제", description = "특정 카테고리를 소프트 삭제 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "카테고리 삭제 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                        {
                                          "success": true,
                                          "code": 1000,
                                          "message": "success delete category",
                                          "result": "success delete category"
                                        }
                                        """
                            )
                    )
            )
    })
    @DeleteMapping("/delete")
    public ResponseEntity<BaseResponse<String>> deleteCategory(@RequestBody CategoryDto.RegReq dto) {
        categoryService.deleteCategory(dto.getIdx());
        return ResponseEntity.ok(BaseResponse.success("success delete category"));
    }
}
