package com.example.statistics.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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
    public OpenAPI nexusStatisticsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nexus Statistics MSA API")
                        .version("v1")
                        .description("""
                                Nexus 통계 MSA REST API 문서.

                                ## 구성
                                - **실시간 통계** (`/statistics/*`) — Redis 사전 집계 기반 O(1) 조회
                                - **장기 통계** (`/statistics/long-term/*`) — daily_*_sales 테이블 기반 영구 보존
                                - **내부 운영 도구** (`/internal/statistics/*`) — 수동 dump 등

                                ## 데이터 흐름
                                1. monolith 결제 → Kafka `pos.payment.created` 발행
                                2. 통계 MSA Consumer 가 Redis 에 사전 집계 (Pipeline)
                                3. 새벽 5시 ShedLock 분산 락 + DailyDumpService 가 MariaDB 로 영구 저장

                                ## 관련 이슈
                                - #855 Redis 사전 집계 도입
                                - #868 Redis-Centric 전환 (DB INSERT 제거)
                                - #870 Pipeline 적용 (RTT 12→2)
                                - #881 장기 통계 페이지
                                - #889 자동 dump 스케줄러 (ShedLock + catch-up)
                                - #893 Redis Cluster (Master 3 + Slave 3, SPOF 해소)
                                - #905 Redis-Centric 누락 정리
                                """));
    }
}
