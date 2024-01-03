package com.peoplein.moiming.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.security.exception.AuthExceptionValue;
import com.peoplein.moiming.security.exception.LoginAttemptException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
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

        AuthExceptionValue errValues;

        if (exception instanceof LoginAttemptException) {
            LoginAttemptException lException = (LoginAttemptException) exception;
            errValues = lException.getExceptionValue();
        } else {
            errValues = AuthExceptionValue.AUTH_LOGIN_EXTRA;
        }

        ResponseBodyDto<Object> loginFailRespBody = ResponseBodyDto.createResponse(errValues.getErrCode(), errValues.getErrMsg(), null);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(om.writeValueAsString(loginFailRespBody));
        response.setStatus(errValues.getStatus());

    }
}