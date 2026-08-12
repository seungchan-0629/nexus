package com.example.nexus.domain.menu;

import com.example.nexus.common.model.BaseResponse;
import com.example.nexus.domain.menu.model.MenuDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "메뉴 관리", description = "본사 메뉴 관리(목록/등록/수정/삭제), 레시피(원자재 구성) 및 카테고리 관리 API")
@RequestMapping("/menu")
@RestController
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;

    // 메뉴 리스트 조회
    @Operation(summary = "메뉴 목록 전체 조회 및 검색", description = "본사 메뉴 관리 페이지에서 메뉴 목록을 페이징하여 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<BaseResponse<MenuDto.MenuPageRes>> list(
            @ParameterObject MenuDto.MenuSearchPagingReq searchReq,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size)
    {
        MenuDto.MenuPageRes result =  menuService.list(searchReq, page, size);
        return ResponseEntity.ok(BaseResponse.success(result));
    }


    // 메뉴 재료 조회
    @Operation(summary = "메뉴 구성 재료(레시피) 조회", description = "특정 메뉴(menuIdx)에 포함된 원자재 목록과 각각의 소모량을 조회합니다.")
    @GetMapping("/item/list/{menuIdx}")
    public ResponseEntity<BaseResponse<MenuDto.MenuItemListRes>> menuItemList(@Parameter(description = "메뉴 식별자(ID)") @PathVariable Long menuIdx){
        MenuDto.MenuItemListRes result = menuService.menuItemList(menuIdx);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // S3 Pre-signed URL 요청
    @Operation(summary = "S3 업로드용 Pre-signed URL 발급", description = "메뉴 이미지 파일 첨부를 위해 AWS S3에 직접 업로드할 수 있는 임시 권한 URL을 발급받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "파일 용량 초과", content = @Content(schema = @Schema(example = "{\"success\":false, \"code\":400, \"message\":\"이미지 파일 용량을 초과했습니다.\"}")))
    })
    @GetMapping("/presignedUrl/{fileName}")
    public ResponseEntity<BaseResponse<Map<String, String>>> presignedUrl(
            @Parameter(description = "업로드할 파일명 (확장자 포함)", example = "americano.jpg") @PathVariable(name = "fileName") String fileName,
            @Parameter(description = "파일 크기 (Byte 단위)", example = "1024") @RequestParam(name = "fileSize") long fileSize){
        // 1. 서비스를 호출하여 URL과 고유 파일명을 받아옵니다.
        Map<String, String> result = menuService.getPresignedUrl(fileName, fileSize);

        // 2. BaseResponse.success에 결과 Map을 담아 반환합니다.
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // 메뉴 등록
    @Operation(summary = "신규 메뉴 등록", description = "새로운 메뉴와 해당 메뉴의 레시피(원자재 구성 정보)를 본사 시스템에 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "원자재를 찾을 수 없음", content = @Content(schema = @Schema(example = "{\"success\":false, \"code\":404, \"message\":\"원자재를 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "409", description = "중복되거나 삭제된 메뉴명", content = @Content(schema = @Schema(example = "{\"success\":false, \"code\":409, \"message\":\"이미 존재하는 메뉴명입니다.\"}")))
    })
    @PostMapping("/new/register")
    public ResponseEntity<BaseResponse<String>> menuRegister(@Valid @RequestBody MenuDto.MenuRegReq dto){
        menuService.menuRegister(dto);

        return ResponseEntity.ok(BaseResponse.success("성공"));
    }

    // 메뉴 수정
    @Operation(summary = "메뉴 정보 수정", description = "기존 메뉴의 정보(이름, 가격, 카테고리, 이미지 등) 및 레시피 구성을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 메뉴", content = @Content(schema = @Schema(example = "{\"success\":false, \"code\":3204, \"message\":\"존재하지 않는 메뉴입니다.\"}"))),
            @ApiResponse(responseCode = "404", description = "원자재를 찾을 수 없음", content = @Content(schema = @Schema(example = "{\"success\":false, \"code\":404, \"message\":\"원자재를 찾을 수 없습니다.\"}"))),
            @ApiResponse(responseCode = "409", description = "중복되거나 삭제된 메뉴명", content = @Content(schema = @Schema(example = "{\"success\":false, \"code\":409, \"message\":\"이미 존재하는 메뉴명입니다.\"}")))
    })
    @PutMapping("/update/{menuIdx}")
    public ResponseEntity<BaseResponse<String>> menuUpdate(@Parameter(description = "메뉴 식별자(ID)") @PathVariable(name = "menuIdx") Long menuIdx, @Valid  @RequestBody MenuDto.MenuRegReq dto){
        menuService.menuUpdate(menuIdx, dto);
        return ResponseEntity.ok(BaseResponse.success("성공"));
    }

    // 메뉴 삭제
    @Operation(summary = "메뉴 삭제 (논리 삭제)", description = "특정 메뉴를 비활성화(삭제 처리) 상태로 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 메뉴", content = @Content(schema = @Schema(example = "{\"success\":false, \"code\":3204, \"message\":\"존재하지 않는 메뉴입니다.\"}")))
    })
    @PutMapping("/delete/{menuIdx}")
    public ResponseEntity<BaseResponse<String>> menuDelete(@Parameter(description = "메뉴 식별자(ID)") @PathVariable(name = "menuIdx") Long menuIdx){
        menuService.menuDelete(menuIdx);
        return ResponseEntity.ok(BaseResponse.success("성공"));
    }

    // 메뉴 카테고리 조회
    @Operation(summary = "메뉴 카테고리 목록 조회", description = "등록된 전체 메뉴 카테고리(예: 커피류, 디저트류 등) 목록을 조회합니다.")
    @GetMapping("/category/list")
    public ResponseEntity<BaseResponse<List<MenuDto.MenuCategoryRes>>> menuCategory(){
        List<MenuDto.MenuCategoryRes> result =  menuService.menucategory();
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    // 메뉴 카테고리 등록
    @Operation(summary = "신규 메뉴 카테고리 등록", description = "새로운 메뉴 카테고리를 시스템에 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "409", description = "중복된 카테고리명", content = @Content(schema = @Schema(example = "{\"success\":false, \"code\":409, \"message\":\"중복된 카테고리명입니다.\"}")))
    })
    @PostMapping("/category/register")
    public ResponseEntity<BaseResponse<String>> menuCategoryReg(@Valid @RequestBody MenuDto.MenuCategoryRegReq dto){
        menuService.menucategoryReg(dto);
        return ResponseEntity.ok(BaseResponse.success("성공"));
    }

    // 메뉴 카테고리 삭제
    @Operation(summary = "메뉴 카테고리 삭제", description = "특정 메뉴 카테고리를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "삭제 불가 (사용 중인 카테고리)", content = @Content(schema = @Schema(example = "{\"success\":false, \"code\":400, \"message\":\"사용 중인 카테고리라 삭제할 수 없습니다.\"}")))
    })
    @DeleteMapping("/category/delete/{categoryIdx}")
    public ResponseEntity<BaseResponse<String>> menuCategoryDelete(@Parameter(description = "카테고리 식별자(ID)") @PathVariable Long categoryIdx ){
        menuService.menuCategoryDelete(categoryIdx);
        return ResponseEntity.ok(BaseResponse.success("성공"));
    }
}
