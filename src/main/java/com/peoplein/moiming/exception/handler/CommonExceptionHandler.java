package com.peoplein.moiming.exception.handler;


import com.peoplein.moiming.exception.enums.CommonErrorEnum;
import com.peoplein.moiming.model.ErrorResponse;
import com.peoplein.moiming.model.ResponseModel;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseModel<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception, HttpServletResponse response) {

        CommonErrorEnum commonErrorEnum = CommonErrorEnum.COMMON_ILLEGAL_PARAMS;

        response.setStatus(commonErrorEnum.getStatusCode());

        return ResponseModel.createResponse(
                new ErrorResponse(commonErrorEnum.getErrorCode()
                        , commonErrorEnum.getErrorType()
                        , exception.getLocalizedMessage())
        );
    }


    @ExceptionHandler(NullPointerException.class)
    protected ResponseModel<ErrorResponse> handleNullPointerException(NullPointerException exception, HttpServletResponse response) {

        CommonErrorEnum commonErrorEnum = CommonErrorEnum.COMMON_NULL_EXCEPTION;

        response.setStatus(commonErrorEnum.getStatusCode());

        return ResponseModel.createResponse(
                new ErrorResponse(commonErrorEnum.getErrorCode()
                        , commonErrorEnum.getErrorType()
                        , exception.getLocalizedMessage())
        );
    }


}
