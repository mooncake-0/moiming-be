package com.peoplein.moiming.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
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
        return new OpenAPI()
//                .addServersItem(new Server().url("https://dev.user.gocho-back.com/").description("USER - DEV"))
                .addServersItem(new Server().url("http://localhost:8080/").description("LOCAL"))
                .info(new Info().title("Moiming Devs API 명세서")
                        .description("### 에러코드 문서\n\n" +
                                "https://docs.google.com/spreadsheets/d/1vkgXTrdumMbzeeNeGzC8MvOdo3CQGqyr/edit?usp=share_link&ouid=115296106035275739943&rtpof=true&sd=true\n\n" +
                                "### 하기 문서의 모든 응답 Example Value 는 {\"data\":[Example Value]} 값으로 묶여있는 JSON 값입니다\n\n" +
                                "**`Error Response Model`**\n\n" +
                                "- errorCode : 에러코드명, 에러코드 문서에 명시되어 있습니다\n" +
                                "- errorType : 에러 현황을 간략하게 알 수 있는 타입 값이 전달됩니다. 에러코드 문서에 명시되어 있습니다\n" +
                                "- errorDesc : 서버에서 발생한 error exception 의 메세지를 전달합니다.")
                        .version("v0.0"));
    }

    @Bean
    public GroupedOpenApi groupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("v0.0")
                .pathsToMatch("/**")
                .build();
    }
}
