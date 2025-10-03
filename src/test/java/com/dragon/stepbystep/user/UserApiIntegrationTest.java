package com.dragon.stepbystep.user;

import com.dragon.stepbystep.UserApiOnlyApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.hamcrest.Matchers.nullValue;
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
          "email":"test@example.com",
          "password":"Pa55word!",
          "nickname":"테스터",
          "gender":"F",
          "birthyear":2001
        }
        """;
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(register))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("테스터"));

        // 2) 로그인
        var login = """
        {
          "email":"test@example.com",
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
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("테스터"));
    }

    @Test
    void register_delete_flow() throws Exception {
        var register = """
        {
          "loginId":"deleteuser1",
          "password":"Pa55word!",
          "nickname":"삭제테스터",
          "gender":"M",
          "birthyear":1995
        }
        """;
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(register))
                .andExpect(status().isOk());

        var login = """
        {
          "loginId":"deleteuser1",
          "password":"Pa55word!"
        }
        """;
        var loginResult = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andExpect(status().isOk())
                .andReturn();

        var body = loginResult.getResponse().getContentAsString();
        var token = body.replaceAll(".*\\"accessToken\\"\\s*:\\s*\\"([^\\"]+)\\".*", "$1");

        mvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원 탈퇴 성공!"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }
}
