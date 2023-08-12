package com.peoplein.moiming.exception.handler;

import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.exception.MoimingValidationException;
import com.peoplein.moiming.model.ResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class MoimingExceptionHandler {

    @ExceptionHandler(MoimingApiException.class)
    public ResponseModel<Object> moimingApiException(MoimingApiException exception) {
        return ResponseModel.createResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), null);
    }

    @ExceptionHandler(MoimingValidationException.class)
    public ResponseModel<Object> moimingApiException(MoimingValidationException exception) {
        return ResponseModel.createResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), exception.getErrMap());
    }

}