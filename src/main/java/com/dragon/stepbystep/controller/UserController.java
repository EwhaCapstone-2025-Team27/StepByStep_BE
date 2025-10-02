package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.common.ApiResponse;
import com.dragon.stepbystep.domain.ChangePw;
import com.dragon.stepbystep.dto.ChangePwRequestDto;
import com.dragon.stepbystep.dto.UserResponseDto;
import com.dragon.stepbystep.dto.UserUpdateDto;
import com.dragon.stepbystep.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    @Autowired
    private UserService userService;

    // 내 정보 조회
    @GetMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(Principal principal) {
        Long id = Long.valueOf(principal.getName());
        UserResponseDto response = userService.getMe(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("사용자 정보 조회 성공!",response));
    }

    // 내 정보 수정(닉네임, 성별, 출생년도)
    @PatchMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(Principal principal, @RequestBody UserUpdateDto userUpdateDto) {
        Long id = Long.valueOf(principal.getName());
        UserResponseDto response = userService.updateUser(id, userUpdateDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("사용자 정보 수정 성공!",response));
    }

    // 비밀번호 변경
    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(Principal principal, @Valid @RequestBody ChangePwRequestDto dto) {
        Long id = Long.valueOf(principal.getName());
        userService.changePassword(id, dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("비밀번호 변경 완료!", null));
    }

}