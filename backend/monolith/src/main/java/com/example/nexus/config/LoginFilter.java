package com.example.nexus.config;

import com.example.nexus.domain.user.model.AuthUserDetails;
import com.example.nexus.domain.user.model.UserDto;
import com.example.nexus.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;

    public LoginFilter(AuthenticationManager authenticationManager, ObjectMapper objectMapper) {
        super(authenticationManager);
        this.authenticationManager = authenticationManager;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        if (!(authResult.getPrincipal() instanceof AuthUserDetails)) {
            throw new AuthenticationServiceException("principal is not AuthUserDetails");
        }

        AuthUserDetails user = (AuthUserDetails) authResult.getPrincipal();
        String token = JwtUtil.createToken(user.getIdx(), user.getUsername(), String.valueOf(user.getRole()));
        response.setHeader("Set-Cookie", "CTOKEN=" + token + "; Path=/");
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.getWriter().write("login failed");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            UserDto.LoginReq dto = objectMapper.readValue(request.getInputStream(), UserDto.LoginReq.class);

            if (dto == null || dto.getEmail() == null || dto.getPassword() == null) {
                throw new BadCredentialsException("Missing email or password");
            }

            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword(), null);

            return authenticationManager.authenticate(token);
        } catch (IOException e) {
            throw new AuthenticationServiceException("Invalid login request body", e);
        }
    }
}