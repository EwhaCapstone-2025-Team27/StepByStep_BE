package com.dragon.stepbystep.security;

import com.dragon.stepbystep.common.ApiResponse;
import com.dragon.stepbystep.exception.JwtAuthenticationException;
import com.dragon.stepbystep.exception.UserNotFoundException;
import com.dragon.stepbystep.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.AntPathMatcher;
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

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<ExcludedRequest> EXCLUDED_REQUESTS = List.of(
            new ExcludedRequest("/health"),
            new ExcludedRequest("/api/auth/register"),
            new ExcludedRequest("/api/auth/login"),
            new ExcludedRequest("/api/auth/refresh"),
            new ExcludedRequest("/api/auth/find-email"),
            new ExcludedRequest("/api/auth/find-password"),
            new ExcludedRequest("/api/moderation/guard-input", HttpMethod.POST),
            new ExcludedRequest("/api/moderation/guard-output", HttpMethod.POST),
            new ExcludedRequest("/api/moderation/filter-snippets", HttpMethod.POST),
            new ExcludedRequest("/api/moderation/guard-batch", HttpMethod.POST)
    );

    private final JwtTokenProvider jwtTokenProvider;

    private final UserService userService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        try {

            String token = getTokenFromRequest(request);

            if (token == null) {
                throw new JwtAuthenticationException("로그인되지 않은 사용자입니다.");
            }

            if (!jwtTokenProvider.validateToken(token)) {
                throw new JwtAuthenticationException("로그인되지 않은 사용자입니다.");
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
            response.setStatus(404);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(), ApiResponse.error(e.getMessage()));
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (HttpMethod.OPTIONS.matches(method)) {
            return true;
        }

        return EXCLUDED_REQUESTS.stream()
                .anyMatch(excludedRequest -> excludedRequest.matches(path, method));
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private static final class ExcludedRequest {
        private final String pattern;
        private final List<HttpMethod> methods;

        private ExcludedRequest(String pattern, HttpMethod... methods) {
            this.pattern = pattern;
            this.methods = List.of(methods);
        }

        private boolean matches(String path, String method) {
            boolean pathMatches = PATH_MATCHER.match(pattern, path);
            boolean methodMatches = methods.isEmpty() || methods.stream().anyMatch(httpMethod -> httpMethod.matches(method));
            return pathMatches && methodMatches;
        }
    }
}