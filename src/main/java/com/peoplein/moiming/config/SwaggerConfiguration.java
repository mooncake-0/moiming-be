package com.peoplein.moiming.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfiguration {
    @Bean
    public OpenAPI openAPI() {

        Info info = new Info().title("Moiming Devs API 명세서")
                .description("### 에러코드 문서\n\n" +
                        "https://docs.google.com/spreadsheets/d/1vkgXTrdumMbzeeNeGzC8MvOdo3CQGqyr/edit?usp=share_link&ouid=115296106035275739943&rtpof=true&sd=true\n\n" +
                        "### 하기 문서의 모든 응답 Example Value 는 {\"data\":[Example Value]} 값으로 묶여있는 JSON 값입니다\n\n" +
                        "**`Error Response Model`**\n\n" +
                        "- errorCode : 에러코드명, 에러코드 문서에 명시되어 있습니다\n" +
                        "- errorType : 에러 현황을 간략하게 알 수 있는 타입 값이 전달됩니다. 에러코드 문서에 명시되어 있습니다\n" +
                        "- errorDesc : 서버에서 발생한 error exception 의 메세지를 전달합니다.\n\n\n" +
                        "**`Execute 실행 시, JWT 토큰을 Authorize에서 포함하세요`**\n" +
                        "- curl -X POST -i -H \"Content-Type: application/json\" -d '{\"uid\":\"<USER_ID>\", \"password\":\"<PASS_WORD>\"}' http://moim.k8s-sha.com:31088/api/v0/auth/login | grep -i \"^access_token:\" | awk -F': ' '{print $2}' | tr -d '\\r'"
                )
                .version("v0.0");

        Server local = new Server().url("http://localhost:8080/").description("LOCAL");
        Server dev = new Server().url("http://moim.k8s-sha.com:31088/").description("DEV");
        List<Server> servers = List.of(local, dev);

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER).name("Authorization");
        SecurityRequirement securityItem = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityItem)
                .servers(servers)
                .info(info);
    }

    @Bean
    public GroupedOpenApi groupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("v0.0")
                .pathsToMatch("/**")
                .build();
    }
}
