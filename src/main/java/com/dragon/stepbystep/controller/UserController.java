package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.common.ApiResponse;
import com.dragon.stepbystep.dto.UserResponseDto;
import com.dragon.stepbystep.dto.UserUpdateDto;
import com.dragon.stepbystep.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(Principal principal) {
        String userEmail = String.valueOf(principal.getName());
        UserResponseDto response = userService.getUserByEmail(userEmail);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("사용자 정보 조회 성공!",response));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(Principal principal, @RequestBody UserUpdateDto userUpdateDto) {
        String userEmail = String.valueOf(principal.getName());
        UserResponseDto response = userService.updateUser(userEmail, userUpdateDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("사용자 정보 수정 성공!",response));
    }
}