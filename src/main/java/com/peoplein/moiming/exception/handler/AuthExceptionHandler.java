package com.peoplein.moiming.exception.handler;

import com.peoplein.moiming.exception.BadAuthParameterInputException;
import com.peoplein.moiming.exception.DuplicateAuthValueException;
import com.peoplein.moiming.model.ErrorResponse;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.security.exception.AuthErrorEnum;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(BadAuthParameterInputException.class)
    protected ResponseModel<ErrorResponse> handleBadParameterInputException(BadAuthParameterInputException exception, HttpServletResponse response) {

        AuthErrorEnum authErrorEnum = null;

        if (exception.getErrorCode().equals(AuthErrorEnum.AUTH_SIGNIN_INVALID_INPUT.getErrorCode())) {
            authErrorEnum = AuthErrorEnum.AUTH_SIGNIN_INVALID_INPUT;
        } else {
            authErrorEnum = AuthErrorEnum.AUTH_SIGNIN_UNKNOWN;
        }

        response.setStatus(authErrorEnum.getStatusCode());

        return ResponseModel.createResponse(
                new ErrorResponse(authErrorEnum.getErrorCode()
                        , authErrorEnum.getErrorType()
                        , exception.getLocalizedMessage())
        );

    }

    @ExceptionHandler(DuplicateAuthValueException.class)
    protected ResponseModel<ErrorResponse> handleDuplicateKeyException(DuplicateAuthValueException exception, HttpServletResponse response) {

        AuthErrorEnum authErrorEnum = null;

        if (exception.getErrorCode().equals(AuthErrorEnum.AUTH_SIGNIN_DUPLICATE_UID.getErrorCode())) {
            authErrorEnum = AuthErrorEnum.AUTH_SIGNIN_DUPLICATE_UID;
        } else if (exception.getErrorCode().equals(AuthErrorEnum.AUTH_SIGNIN_DUPLICATE_EMAIL.getErrorCode())) {
            authErrorEnum = AuthErrorEnum.AUTH_SIGNIN_DUPLICATE_EMAIL;
        } else {
            authErrorEnum = AuthErrorEnum.AUTH_SIGNIN_UNKNOWN;
        }

        response.setStatus(authErrorEnum.getStatusCode());

        return ResponseModel.createResponse(
                new ErrorResponse(authErrorEnum.getErrorCode()
                        , authErrorEnum.getErrorType()
                        , exception.getLocalizedMessage())
        );
    }
}
