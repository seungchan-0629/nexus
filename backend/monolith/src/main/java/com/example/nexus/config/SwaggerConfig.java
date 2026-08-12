package com.example.nexus.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean

    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nexus SCM API")
                        .description("""
                                Nexus SCM 시스템 API 문서.

                                **인증 방법:**
                                1. `POST /login` 으로 로그인
                                2. 응답 헤더 `Set-Cookie` 에서 `CTOKEN` 값 복사
                                3. 우측 상단 **Authorize** 버튼 클릭
                                4. `Bearer <CTOKEN 값>` 형식으로 입력 후 Authorize
                                """)
                        .version("v1.0"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .name("BearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("로그인 후 발급된 CTOKEN 값을 입력하세요.")));

    }
}
