package com.peoplein.moiming.security.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.model.ResponseBodyDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
 ExceptionTranslationFilter 가
 AuthenticationEntryPoint 와
 AccessDeniedHandler 를 통해 간단하게 사용하는 곳
 */
@Slf4j
public class ExceptionFilterHandler {
    public static void sendExceptionResponse(HttpServletResponse response, String errMsg, HttpStatus status) {
        try {

            ObjectMapper om = new ObjectMapper();
            ResponseBodyDto<?> responseBody = ResponseBodyDto.createResponse(-1, errMsg, null);
            String responseBodyStr = om.writeValueAsString(responseBody);

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(status.value());
            response.getWriter().println(responseBodyStr);

        } catch (JsonProcessingException e) {  // 딱히 뭘 할 필요는 없을듯?
            log.error("ExceptionTranslationFilter 에서 예외 처리 중 OM 예외 발생 : {}", e.getMessage());
        } catch (IOException e) {
            log.error("ExceptionTranslationFilter 에서 예외 처리 중 Response 작성 예외 발생 : {}", e.getMessage());
        }
    }
}