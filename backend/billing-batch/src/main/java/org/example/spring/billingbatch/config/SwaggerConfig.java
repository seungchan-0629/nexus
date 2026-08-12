package org.example.spring.billingbatch.config;

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
    public OpenAPI billingBatchOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nexus Billing Batch API")
                        .description("""
                                Nexus 정기결제 및 재시도 배치 API 문서.

                                ## 주요 기능
                                - **정기 결제** : 매월 정해진 날짜에 가맹점 결제 처리
                                - **결제 재시도** : 결제 실패 건에 대한 자동 재시도 로직 수행
                                - **결제 이력 관리** : 결제 성공/실패 이력 조회 및 관리
                                """)
                        .version("v1"));
    }
}
