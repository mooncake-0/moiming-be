package com.peoplein.moiming.exception.handler;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.exception.MoimingValidationException;
import com.peoplein.moiming.exception.repository.InvalidQueryParameterException;
import com.peoplein.moiming.model.ResponseBodyDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> moimingValidationException(MoimingValidationException exception) {
        log.error("VALIDATION EXCEPTION : {}", exception.getMessage());
        return ResponseEntity.badRequest().body(ResponseBodyDto.createResponse(-1, exception.getMessage(), exception.getErrMap()));
    }


    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<?> refreshJwtVerificationException(JWTVerificationException exception) {

        int statusCode;
        int errCode = -1;
        String data = "";

        if (exception instanceof SignatureVerificationException) {
            log.error("SIGNATURE 에러, 올바르지 않은 Signature 로 접근하였습니다 : {}", exception.getMessage());
            statusCode = HttpStatus.FORBIDDEN.value();
            data = "SIGNATURE Error. Reported";
        } else if (exception instanceof TokenExpiredException) {
            errCode = -100;
            statusCode = HttpStatus.UNAUTHORIZED.value();
            data = "ACCESS_TOKEN_EXPIRED";

        } else { // 그 외 모든 JWT Verification Exception
            log.info("Refresh Token Verify 도중 알 수 없는 예외가 발생 : {}", exception.getMessage());
            statusCode = HttpStatus.BAD_REQUEST.value();
        }

        ResponseBodyDto responseBody = ResponseBodyDto.createResponse(errCode, exception.getMessage(), data);

        return new ResponseEntity<>(responseBody, HttpStatus.valueOf(statusCode));
    }


    // Repository Exception
    @ExceptionHandler(InvalidQueryParameterException.class)
    public ResponseEntity<?> invalidQueryParameterException(InvalidQueryParameterException exception) {
        return ResponseEntity.internalServerError().body(ResponseBodyDto.createResponse(-1, "정보를 불러오는 중 오류가 발생하였습니다", null));
    }
}