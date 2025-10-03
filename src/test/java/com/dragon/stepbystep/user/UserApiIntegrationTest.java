package com.dragon.stepbystep.user;

import com.dragon.stepbystep.UserApiOnlyApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = UserApiOnlyApplication.class)
@ActiveProfiles("useronly")
@AutoConfigureMockMvc
class UserApiIntegrationTest {

    @Autowired MockMvc mvc;

    @Test
    void register_login_me_flow() throws Exception {
        // 1) 회원가입
        var register = """
        {
          "loginId":"abcd1234",
          "password":"Pa55word!",
          "nickname":"테스터",
          "gender":"F",
          "birthyear":2001
        }
        """;
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(register))
                .andExpect(status().isOk());

        // 2) 로그인
        var login = """
        {
          "loginId":"abcd1234",
          "password":"Pa55word!"
        }
        """;
        var loginResult = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andExpect(status().isOk())
                .andReturn();

        // 응답에서 accessToken 꺼내기 (ApiResponse<TokenDto> 구조 기준)
        var body = loginResult.getResponse().getContentAsString();
        // 실프로젝트 포맷에 맞춰 간단 파싱 (테스트 편의상 정규식 사용)
        var token = body.replaceAll(".*\"accessToken\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        // 3) /users/me
        mvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.loginId").value("abcd1234"))
                .andExpect(jsonPath("$.data.nickname").value("테스터"));
    }
}
