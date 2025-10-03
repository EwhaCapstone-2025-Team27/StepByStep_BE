package com.dragon.stepbystep.controller;

import com.dragon.stepbystep.dto.LoginRequestDto;
import com.dragon.stepbystep.dto.TokenDto;
import com.dragon.stepbystep.service.PasswordResetService;
import com.dragon.stepbystep.service.TokenBlacklistService;
import com.dragon.stepbystep.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private com.dragon.stepbystep.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private PasswordResetService passwordResetService;

    @Test
    @DisplayName("임시 비밀번호 로그인 시 안내 메시지를 반환한다")
    void loginWithTemporaryPasswordReturnsGuidanceMessage() throws Exception {
        LoginRequestDto requestDto = new LoginRequestDto("test@example.com", "tempPass1!");
        TokenDto tokenDto = new TokenDto("access", "refresh", true);

        given(userService.login(any(LoginRequestDto.class))).willReturn(tokenDto);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("임시 비밀번호로 로그인했습니다. 내 정보 수정 페이지에서 비밀번호를 변경해주세요."));
    }

    @Test
    @DisplayName("일반 로그인 시 기존 메시지를 반환한다")
    void loginWithRegularPasswordReturnsDefaultMessage() throws Exception {
        LoginRequestDto requestDto = new LoginRequestDto("user@example.com", "password1!");
        TokenDto tokenDto = new TokenDto("access", "refresh", false);

        given(userService.login(any(LoginRequestDto.class))).willReturn(tokenDto);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인 성공!"));
    }
}