package com.example.gateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtGatewayFilterFactory.Config> {
    
    private final JwtUtil jwtUtil;

    public JwtGatewayFilterFactory(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }
    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            HttpCookie cookie = request.getCookies().getFirst("CTOKEN");

            if (cookie == null) {
                log.error("토큰 없음");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = cookie.getValue();

            try {
                Long userIdx = jwtUtil.getUserIdx(token);
                String userName = jwtUtil.getUsername(token);
                String role = jwtUtil.getRole(token);

                ServerHttpRequest newRequest = exchange.getRequest().mutate()
                        .header("X-User-Idx", String.valueOf(userIdx))
                        .header("X-User-Name", userName)
                        .header("X-User-Role", role)
                        .build();

                return chain.filter(exchange.mutate().request(newRequest).build());
            } catch (Exception e) {
                log.error("토큰 검증 실패: {}", e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }
}