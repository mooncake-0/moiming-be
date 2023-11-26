package com.peoplein.moiming.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.security.exception.BadLoginInputException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MoimingLoginFailureHandler implements AuthenticationFailureHandler {

    private ObjectMapper om = new ObjectMapper();

    /*
     Login 시도 실패시 응답 처리
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        int statusCode;

        // exception 의 종류들을 판단해보자
        if (exception instanceof BadLoginInputException) {
            statusCode = HttpStatus.BAD_REQUEST.value();
        } else if (exception instanceof BadCredentialsException) {
            statusCode = HttpStatus.UNAUTHORIZED.value();
        } else if (exception instanceof UsernameNotFoundException) {
            statusCode = HttpStatus.UNAUTHORIZED.value();
        } else { // ExtraAuthenticationException 으로 넘어올 것
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        ResponseBodyDto<?> responseBody = ResponseBodyDto.createResponse("-1", exception.getMessage(), null);
        response.setStatus(statusCode);
        response.getWriter().write(om.writeValueAsString(responseBody));
    }
}