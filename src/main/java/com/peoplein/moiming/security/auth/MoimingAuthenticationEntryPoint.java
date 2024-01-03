package com.peoplein.moiming.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.security.exception.AuthExceptionValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/*
 Authentication 예외를 처리한다
 Moiming 이 잡는 예외를 제외하면, Security 가 기존 처리하던대로 진행한다 (InsufficientAuthException, Http403ForbiddenEntryPoint.java 참고)
 */
@Slf4j
public class MoimingAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private ObjectMapper om = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        AuthExceptionValue errValues = (AuthExceptionValue) request.getAttribute("EXCEPTION_VALUE");

        if (errValues == null) { // Moiming 토큰 예외가 아닐 경우

            if (authException instanceof InsufficientAuthenticationException) {

                errValues = AuthExceptionValue.AUTH_TOKEN_NOT_FOUND;

            } else {
                log.error("{} : {}", "Moiming Exception 외 예외 발생", authException.getMessage());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                return;
            }

        } else {
            errValues = (AuthExceptionValue) request.getAttribute("EXCEPTION_VALUE");
        }

        ResponseBodyDto<Object> errResponseBody = ResponseBodyDto.createResponse(errValues.getErrCode(), errValues.getErrMsg(), null);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(om.writeValueAsString(errResponseBody));
        response.setStatus(errValues.getStatus());

    }
}
