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

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        http
                // ✨ CSRF 비활성화 (JWT 사용하므로 불필요)
                .csrf(csrf -> csrf.disable())

                // ✨ 세션 비활성화 (중복 인증 방지)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ✨ CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ✨ 인증 실패 핸들러
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(restAuthenticationEntryPoint())
                )

                .authorizeHttpRequests(reg -> reg
                        .requestMatchers(
                                "/health",
                                "/api/quiz/**",
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/find-email",
                                "/api/auth/find-password"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST,
                                "/api/moderation/guard-input",
                                "/api/moderation/guard-output",
                                "/api/moderation/filter-snippets",
                                "/api/moderation/guard-batch"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/board/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/board/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/board/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/board/**").authenticated()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 401을 JSON으로 깔끔하게 내려주는 엔트리포인트
    @Bean
    public AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("""
            {"status":"error","message":"인증이 필요합니다.","data":null}
            """);
        };
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserService userService) {
        return new JwtAuthenticationFilter(jwtTokenProvider, userService);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        cfg.setAllowedOrigins(List.of(
                "https://seongkeum.com",
                "https://api.seongkeum.com",
                "http://localhost:3000"
        ));
        cfg.setAllowedMethods(List.of("*"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization"));

        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}