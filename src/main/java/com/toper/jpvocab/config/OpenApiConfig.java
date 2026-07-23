package com.toper.jpvocab.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI(springdoc-openapi) 설정.
 *
 * 서버가 뜨면 아래 경로에서 API 문서를 볼 수 있다:
 * - Swagger UI: /swagger-ui/index.html
 * - OpenAPI 원본(JSON): /v3/api-docs
 *
 * 인증이 필요한 API(저장 단어함, 복습 등)를 Swagger UI에서 직접 테스트하려면,
 * 화면 오른쪽 위 "Authorize" 버튼을 누르고 /api/auth/login 으로 받은 accessToken을
 * "Bearer {accessToken}" 형태 없이 토큰 값만 넣으면 된다(springdoc이 Bearer 접두사를 자동으로 붙여준다).
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI jpVocabOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("JP Vocab API")
                        .description("일본어 단어장 서비스 백엔드 API 문서. "
                                + "인증이 필요한 API는 오른쪽 위 Authorize 버튼으로 로그인 후 발급받은 "
                                + "accessToken을 넣고 테스트할 수 있다.")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
