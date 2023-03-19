package com.peoplein.moiming.exception.handler;

import com.peoplein.moiming.exception.enums.CommonErrorEnum;
import com.peoplein.moiming.exception.enums.ResponseErrorEnum;
import com.peoplein.moiming.model.ErrorResponse;
import com.peoplein.moiming.model.ResponseModel;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class ResponseExceptionHandler {

    /*
    Spring MVC 에서 ResponseEntityException 으로 보는 Exception 클래스.
    Custom ResponseModel 전송을 위해 자체 구현
    필요에 따라 발생하는 것들에 대해서 구현

    추가 응답에 대한 Custom Exception 도 추가 구현 가능
    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            HttpMediaTypeNotAcceptableException.class,
            MissingPathVariableException.class,
            MissingServletRequestParameterException.class,
            ServletRequestBindingException.class,
            ConversionNotSupportedException.class,
            TypeMismatchException.class,
            HttpMessageNotReadableException.class,
            HttpMessageNotWritableException.class,
            MethodArgumentNotValidException.class,
            MissingServletRequestPartException.class,
            BindException.class,
            NoHandlerFoundException.class,
            AsyncRequestTimeoutException.class
    })*/

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseModel<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception, HttpServletResponse response
    ) {

        ResponseErrorEnum responseErrorEnum = ResponseErrorEnum.RESPONSE_HTTP_NOT_READABLE;

        response.setStatus(responseErrorEnum.getStatusCode());

        return ResponseModel.createResponse(
                new ErrorResponse(responseErrorEnum.getErrorCode()
                        , responseErrorEnum.getErrorType()
                        , exception.getLocalizedMessage())
        );
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    protected ResponseModel<ErrorResponse> HttpMessageNotWritableException(
            HttpMessageNotWritableException exception, HttpServletResponse response
    ) {
        ResponseErrorEnum responseErrorEnum = ResponseErrorEnum.RESPONSE_HTTP_NOT_WRITABLE;

        response.setStatus(responseErrorEnum.getStatusCode());

        return ResponseModel.createResponse(
                new ErrorResponse(responseErrorEnum.getErrorCode()
                        , responseErrorEnum.getErrorType()
                        , exception.getLocalizedMessage())
        );
    }
}
