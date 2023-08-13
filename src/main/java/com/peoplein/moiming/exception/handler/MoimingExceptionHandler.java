package com.peoplein.moiming.exception.handler;

import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.exception.MoimingValidationException;
import com.peoplein.moiming.model.ResponseBodyDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class MoimingExceptionHandler {

    @ExceptionHandler(MoimingApiException.class)
    public ResponseEntity<?> moimingApiException(MoimingApiException exception) {
        log.error("API EXCEPTION : {}", exception.getMessage());
        return ResponseEntity.badRequest().body(ResponseBodyDto.createResponse(-1, exception.getMessage(), null));
    }

    @ExceptionHandler(MoimingValidationException.class)
    public ResponseEntity<?> moimingApiException(MoimingValidationException exception) {
        log.error("VALIDATION EXCEPTION : {}", exception.getMessage());
        return ResponseEntity.badRequest().body(ResponseBodyDto.createResponse(-1, exception.getMessage(), exception.getErrMap()));
    }

}