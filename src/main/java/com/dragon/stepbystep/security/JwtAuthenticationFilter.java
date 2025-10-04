package com.dragon.stepbystep.security;

import com.dragon.stepbystep.common.ApiResponse;
import com.dragon.stepbystep.exception.JwtAuthenticationException;
import com.dragon.stepbystep.exception.UserNotFoundException;
import com.dragon.stepbystep.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/find-email",
            "/api/auth/find-password"
    );

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        String path = request.getRequestURI();

        // 특정 경로는 필터 적용 안 함
        if (EXCLUDED_PATHS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            String token = getTokenFromRequest(request);

            if (token == null) {
                throw new JwtAuthenticationException("로그인되지 않은 사용자입니다.");  // 예외 발생
            }

            if (!jwtTokenProvider.validateToken(token)) {
                throw new JwtAuthenticationException("유효하지 않은 토큰입니다.");  // 예외 발생
            }

            String userId = jwtTokenProvider.getUserIdFromToken(token);
            if (!userService.existsById(Long.valueOf(userId))) {
                throw new UserNotFoundException();
            }

            Authentication authentication = new OAuth2AuthenticationToken(
                    new OAuth2UserPrincipal(
                            userId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    ),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                    "jwt"
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (JwtAuthenticationException e) {
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(), ApiResponse.error(e.getMessage()));
        } catch (UserNotFoundException e) {
            response.setStatus(403);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(), ApiResponse.error(e.getMessage()));
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

}