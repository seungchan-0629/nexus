package org.example.spring.billingbatch.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.spring.billingbatch.model.UserResponseDto;
import org.example.spring.billingbatch.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 조회 API (Batch)")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 목록 조회", description = "배치 처리를 위한 사용자 목록을 페이징하여 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<Page<UserResponseDto>> getUserList(@PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponseDto> userList = userService.getUserList(pageable);
        return ResponseEntity.ok(userList);
    }
}
