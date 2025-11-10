package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.common.ApiResponse;
import com.dragon.stepbystep.dto.*;
import com.dragon.stepbystep.exception.JwtAuthenticationException;
import com.dragon.stepbystep.security.JwtTokenProvider;
import com.dragon.stepbystep.service.PasswordResetService;
import com.dragon.stepbystep.service.TokenBlacklistService;
import com.dragon.stepbystep.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private PasswordResetService passwordResetService;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(@Valid @RequestBody UserRegisterDto dto) {
        UserResponseDto response = userService.registerUser(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("사용자 등록 성공!", response));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenDto>> login(@Valid @RequestBody LoginRequestDto dto) {
        TokenDto response = userService.login(dto);

        String message = response.isForcePasswordChange()
                ? "임시 비밀번호로 로그인했습니다. 내 정보 수정 페이지에서 비밀번호를 변경해주세요."
                : "로그인 성공!";

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(message, response));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestBody TokenDto dto) {
        String accessToken = dto.getAccessToken();
        String refreshToken = dto.getRefreshToken();

        // 토큰 무효화
        tokenBlacklistService.blacklistToken(accessToken);
        tokenBlacklistService.blacklistToken(refreshToken);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("로그아웃 성공!", null));
    }

    // 이메일 찾기
    @PostMapping("find-email")
    public ResponseEntity<ApiResponse<FindEmailResponseDto>> findEmail(@Valid @RequestBody FindEmailRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success("이메일 찾기 성공!", userService.findEmail(dto)));
    }

    // 비밀번호 찾기
    @PostMapping("/find-password")
    public ResponseEntity<ApiResponse<Void>> findPassword(@Valid @RequestBody FindPasswordRequestDto dto) {
        passwordResetService.issueTemporaryPassword(dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("임시 비밀번호를 이메일로 전송했습니다.", null));
    }

    // 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenDto>> refreshAccessToken(@RequestBody TokenDto dto) {
        String refreshToken = dto.getRefreshToken();

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new JwtAuthenticationException("로그인되지 않은 사용자입니다.");
        }

        String accessToken = jwtTokenProvider.refreshAccessToken(refreshToken);

        TokenDto response = new TokenDto(accessToken, null, false);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("토큰 갱신 성공!", response));
    }

}