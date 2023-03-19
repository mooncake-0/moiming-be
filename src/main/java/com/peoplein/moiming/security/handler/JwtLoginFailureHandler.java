package com.peoplein.moiming.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.peoplein.moiming.model.ErrorResponse;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.security.exception.AuthErrorEnum;
import com.peoplein.moiming.security.exception.BadLoginInputException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtLoginFailureHandler implements AuthenticationFailureHandler {

    private ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /*
     Login 시도 실패시 응답 처리
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        AuthErrorEnum authErrorEnum = null;

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        if (exception instanceof BadLoginInputException || exception instanceof UsernameNotFoundException) {

            authErrorEnum = AuthErrorEnum.AUTH_LOGIN_INVALID_INPUT;

        } else if (exception instanceof BadCredentialsException) {

            authErrorEnum = AuthErrorEnum.AUTH_LOGIN_PW_ERROR;

        } else if (exception instanceof DisabledException) {

            authErrorEnum = AuthErrorEnum.AUTH_LOGIN_DISABLED_ACCOUNT;

        } else {

            authErrorEnum = AuthErrorEnum.AUTH_LOGIN_UNKNOWN;
        }

        response.setStatus(authErrorEnum.getStatusCode());

        ResponseModel<ErrorResponse> errorResponseModel = ResponseModel.createResponse(
                new ErrorResponse(authErrorEnum.getErrorCode()
                        , authErrorEnum.getErrorType()
                        , exception.getLocalizedMessage()));

        response.getWriter().write(om.writeValueAsString(errorResponseModel));
    }
}
