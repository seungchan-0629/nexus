package com.example.nexus.domain.user;

import com.example.nexus.domain.user.model.AuthUserDetails;
import com.example.nexus.domain.user.model.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "사용자 관리", description = "로그인, 회원가입, 마이페이지 및 정보 수정 등 사용자 계정 관련 API")
@RestController
@RequiredArgsConstructor
public class UserController {

    public final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender javaMailSender;

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 사용하여 로그인하고 JWT 토큰을 발급받습니다. (실제 인증은 Security Filter에서 처리)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 실패 (이메일/비밀번호 불일치)")
    })
    @PostMapping("/login")
    public void login(
            @Parameter(description = "로그인 정보 (이메일, 비밀번호)")
            @RequestBody UserDto.LoginReq dto) {
        // 실제 처리는 LoginFilter에서 수행되지만, Swagger 문서화를 위해 메서드를 정의합니다.
    }

    @Operation(summary = "가맹점주 회원가입", description = "새로운 가맹점주 계정을 생성합니다. 입력된 이메일로 임시 비밀번호가 전송됩니다.")
    @PostMapping("/store/signup")
    public ResponseEntity<String> storeSignup(
            @Parameter(description = "가맹점주 가입 정보")
            @RequestBody UserDto.StoreSignupReq dto) {
        UserDto.StoreSignupRes storeSignupRes = userService.storeSignup(dto);

        userService.sendTempPassword(storeSignupRes.getEmail(), storeSignupRes.getPassword());

        return ResponseEntity.ok("임시 비밀번호 : " + storeSignupRes.getPassword());
    }

    @Operation(summary = "마이페이지 조회", description = "로그인된 사용자의 상세 정보(이름, 역할, 소속 가맹점 등)를 조회합니다.")
    @GetMapping("/user/mypage")
    public ResponseEntity<UserDto.StoreInfoRes> mypage(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUserDetails authUserDetails) {
        return ResponseEntity.ok(userService.getStoreInfo(authUserDetails.getIdx()));
    }

    @Operation(summary = "비밀번호 변경", description = "사용자의 현재 비밀번호를 검증한 후 새로운 비밀번호로 변경합니다.")
    @PostMapping("/user/password")
    public ResponseEntity<String> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "현재 비밀번호 및 새 비밀번호 정보")
            @RequestBody UserDto.ChangePasswordDto dto) {

        if (!userService.verifyPassword(authUserDetails, dto.getCurrentPassword())) {
            return ResponseEntity.status(400).body("Password Incorrect");
        }

        if (userService.changePassword(authUserDetails, dto.getNewPassword())) {
           return ResponseEntity.ok("Password Change Success");
        } else {
            return ResponseEntity.status(400).body("Password Change Failed");
        }
    }

    @Operation(summary = "전화번호 변경", description = "사용자의 연락처 정보를 업데이트합니다.")
    @PostMapping("/user/tel")
    public ResponseEntity<String> changeTel(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUserDetails authUserDetails,
            @Parameter(description = "새로운 전화번호 정보")
            @RequestBody UserDto.ChangeTelDto dto) {

        userService.changeTel(authUserDetails, dto.getTel());
        return ResponseEntity.ok("Tel Change Success");
    }

    @Operation(summary = "전체 사용자 목록 조회", description = "시스템에 등록된 모든 사용자의 요약 정보를 조회합니다. (본사 관리자용)")
    @GetMapping("/user/list")
    public ResponseEntity<List<UserDto.UserListRes>> userList() {
        return ResponseEntity.ok(userService.findAllUser());
    }
}
