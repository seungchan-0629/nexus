package com.example.pos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 3 설정.
 * - /swagger-ui/index.html : Swagger UI 접속
 * - /v3/api-docs : OpenAPI JSON 스펙
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI nexusPosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nexus POS MSA API")
                        .version("v1")
                        .description("""
                                Nexus POS MSA REST API 문서.

                                ## 구성 (REST API)
                                - **재고 관리** (`/pos/inventory/*`) — 가맹점 POS 재고 페이징 조회 및 수동 보정
                                - **결제 처리** (`/pos/pay/*`) — POS 단건 결제 및 당일 내역 리스트 조회
                                - **영업 마감** (`/pos/close`) — 일일 마감 처리 (본사 재고 차감 및 AI 발주 트리거)

                                ## 데이터 흐름 (MSA 연동)
                                1. **결제 발생**: POS 결제 완료 시 Kafka `pos.payment.created` 토픽 발행 (통계 서버 연동)
                                2. **마감 발생**: POS 마감 시 Kafka `pos.close.completed` 토픽 발행 (본사 서버 연동)
                                3. **데이터 동기화(Consumer)**: 본사 서버의 원자재, 메뉴 변경 시 Kafka를 통해 POS DB 자동 동기화
                                """))
                .addSecurityItem(new SecurityRequirement().addList("X-User-Idx"))
                .components(new Components()
                        .addSecuritySchemes("X-User-Idx", new SecurityScheme()
                                .name("X-User-Idx")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("가맹점주 식별자(user_idx)를 입력하세요.")));
    }
}
