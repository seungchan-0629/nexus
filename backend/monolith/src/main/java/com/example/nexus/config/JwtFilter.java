package com.example.nexus.config;

import com.example.nexus.common.enums.Role;
import com.example.nexus.domain.user.model.AuthUserDetails;
import com.example.nexus.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();

        return path.startsWith ("/login") ||
                path.startsWith("/signup") ||
                path.startsWith("/store/signup");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String rawToken = null;

        // 1순위: Authorization: Bearer 헤더 (Swagger UI 테스트용)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            rawToken = authHeader.substring(7);
        }

        // 2순위: CTOKEN 쿠키 (실제 프론트엔드)
        if (rawToken == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("CTOKEN")) {
                    rawToken = cookie.getValue();
                    break;
                }
            }
        }

        if (rawToken != null) {
            try {
                Long idx = JwtUtil.getUserIdx(rawToken);
                String username = JwtUtil.getUsername(rawToken);
                String role = JwtUtil.getRole(rawToken);
                AuthUserDetails principal = AuthUserDetails.builder()
                        .idx(idx)
                        .username(username)
                        .role(Role.valueOf(role))
                        .build();

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (RuntimeException ignored) {
                // 만료/파싱 실패 등으로 토큰을 신뢰할 수 없는 경우,
                // 인증 컨텍스트를 건드리지 않고 요청을 그대로 진행한다.
            }
        }

        filterChain.doFilter(request, response);
    }
}
