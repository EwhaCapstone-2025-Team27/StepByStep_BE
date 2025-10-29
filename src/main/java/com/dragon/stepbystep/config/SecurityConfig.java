package com.dragon.stepbystep.config;

import com.dragon.stepbystep.security.JwtAuthenticationFilter;
import com.dragon.stepbystep.security.JwtTokenProvider;
import com.dragon.stepbystep.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//
//    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
//        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
                http
                        // CORS/CSRF 세션
                        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                        .csrf(csrf -> csrf.disable())
                        .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                        // 인증 실패 처리 (401 JSON)
                        .exceptionHandling(e -> e.authenticationEntryPoint(restAuthenticationEntryPoint()))

                        // 권한 규칙
                        .authorizeHttpRequests(reg -> reg

                                // 인증 없이 접근 가능한 공개 경로 (데모용)
                                .requestMatchers(
                                        "/api/healthz",
                                        "/api/chat/**", // 데모동안 공개
                                        "/api/quiz/**", // 데모동안 공개
                                        "/api/auth/register",
                                        "/api/auth/login",
                                        "/api/auth/refresh",
                                        "/api/auth/find-email",
                                        "/api/auth/find-password"
                                ).permitAll()

                                // 게시판: 조회(GET)는 공개, 쓰기/수정/삭제는 인증 필요
                                .requestMatchers(HttpMethod.GET, "/api/board/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/board/**").authenticated()
                                .requestMatchers(HttpMethod.PATCH, "/api/board/**").authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/api/board/**").authenticated()

                                // Preflight
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                // 그 외는 인증 필요
                                .anyRequest().authenticated()
                        )
                        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 401을 JSON으로 깔끔하게 내려주는 엔트리포인트
    @Bean
    public AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (request, response, authException) -> writeJson401(response, "인증이 필요합니다.");
    }
    private void writeJson401(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("""
        {"status":"error","message":"%s","data":null}
        """.formatted(message));
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserService userService) {
        return new JwtAuthenticationFilter(jwtTokenProvider, userService);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        // RN 앱은 CORS 영향이 거의 없지만, 웹/도메인 데모를 고려해 와일드카드 허용
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization"));

        // Bearer 헤더로 인증하므로 쿠키 불필요 → false
        cfg.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

}