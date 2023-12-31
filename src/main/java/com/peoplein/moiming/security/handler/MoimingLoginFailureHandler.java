package com.peoplein.moiming.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.security.exception.AuthExceptionValue;
import com.peoplein.moiming.security.exception.ExtraAuthenticationException;
import com.peoplein.moiming.security.exception.LoginAttemptException;
import lombok.extern.java.Log;
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

import static com.peoplein.moiming.security.exception.AuthExceptionValue.*;

public class MoimingLoginFailureHandler implements AuthenticationFailureHandler {

    private ObjectMapper om = new ObjectMapper();

    /*
     Login 시도 실패시 응답 처리
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        HttpStatus status;
        String errCode = "";
        String errMsg = "";

        // exception 의 종류들을 판단해보자
        if (exception instanceof LoginAttemptException) {
            LoginAttemptException loginException = (LoginAttemptException) exception;
            errCode = loginException.getExceptionValue().getErrCode();
            status = loginException.getExceptionValue().getStatus();
            errMsg = loginException.getMessage();

        } else if (exception instanceof ExtraAuthenticationException) {
            ExtraAuthenticationException extraException = (ExtraAuthenticationException) exception;
            errCode = extraException.getExceptionValue().getErrCode();
            status = extraException.getExceptionValue().getStatus();
            errMsg = extraException.getMessage();

        } else { // ExtraAuthenticationException 으로 넘어올 것
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errCode = "A0";
            errMsg = "알 수 없는 예외 발생";
        }

        ResponseBodyDto<?> responseBody = ResponseBodyDto.createResponse(errCode, errMsg, null);
        response.setStatus(status.value());
        response.getWriter().write(om.writeValueAsString(responseBody));
    }
}