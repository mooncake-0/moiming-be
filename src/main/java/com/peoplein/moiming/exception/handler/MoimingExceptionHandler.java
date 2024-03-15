package com.peoplein.moiming.exception.handler;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.exception.MoimingAuthApiException;
import com.peoplein.moiming.exception.MoimingValidationException;
import com.peoplein.moiming.exception.repository.InvalidQueryParameterException;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.security.exception.AuthExceptionValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.peoplein.moiming.security.exception.AuthExceptionValue.*;

@RestControllerAdvice
@Slf4j
public class MoimingExceptionHandler {

    @ExceptionHandler(MoimingApiException.class)
    public ResponseEntity<?> moimingApiException(MoimingApiException exception) {

        log.error("Moiming API EXCEPTION : {}", exception.getMessage());

        HttpStatus status = HttpStatus.resolve(exception.getEv().getStatus());
        ResponseBodyDto<Object> responseBody = ResponseBodyDto.createResponse(exception.getEv().getErrCode(), exception.getMessage(), null);

        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

        return new ResponseEntity<>(responseBody, status);

    }

    @ExceptionHandler(MoimingValidationException.class)
    public ResponseEntity<?> moimingValidationException(MoimingValidationException exception) {
        log.error("VALIDATION EXCEPTION : {}", exception.getMessage());
        return ResponseEntity.badRequest().body(ResponseBodyDto.createResponse(exception.getErrCode(), exception.getMessage(), exception.getErrMap()));
    }


    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<?> refreshTokenException(JWTVerificationException exception) {

        log.error("JWT REFRESH API EXCEPTION : {}", exception.getMessage());

        AuthExceptionValue ev = AUTH_REFRESH_TOKEN_EXTRA;

        if (exception instanceof SignatureVerificationException) {
            ev = AUTH_REFRESH_TOKEN_VERIFICATION_FAIL;
        } else if (exception instanceof TokenExpiredException) {
            ev = AUTH_REFRESH_TOKEN_EXPIRED;
        }

        HttpStatus status = HttpStatus.resolve(ev.getStatus());
        ResponseBodyDto<Object> responseBody = ResponseBodyDto.createResponse(ev.getErrCode(), ev.getErrMsg() + " :: " + exception.getMessage(), null);

        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

        return new ResponseEntity<>(responseBody, status);
    }


    @ExceptionHandler(MoimingAuthApiException.class)
    public ResponseEntity<?> ssdAuthApiException(MoimingAuthApiException exception) {

        log.error("Moiming AUTH API EXCEPTION : {}", exception.getMessage());

        HttpStatus status = HttpStatus.resolve(exception.getEv().getStatus());
        ResponseBodyDto<Object> responseBody = ResponseBodyDto.createResponse(exception.getEv().getErrCode(), exception.getMessage(), null);

        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;

        return new ResponseEntity<>(responseBody, status);

    }


    // Body 를 읽지 못할 때 요청 잘못됨 (내가 만들었는데 잡지 못함)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> springHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        log.error("Spring Http Message Not Readable EXCEPTION : {}", exception.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ResponseBodyDto<Object> responseBody = ResponseBodyDto.createResponse("SYS000", exception.getMessage(), null);

        return new ResponseEntity<>(responseBody, status);
    }


    // 5GB 이상의 파일 업로드 시도
    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseEntity<?> springMultipartSizeLimitExceedException(FileSizeLimitExceededException exception) {

        log.error("springMultipartSizeLimitExceedException : {}", exception.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ResponseBodyDto<Object> responseBody = ResponseBodyDto.createResponse("F002", exception.getMessage(), null);

        return new ResponseEntity<>(responseBody, status);
    }


    // Repository Exception
    @ExceptionHandler(InvalidQueryParameterException.class)
    public ResponseEntity<?> invalidQueryParameterException(InvalidQueryParameterException exception) {
        return ResponseEntity.internalServerError().body(ResponseBodyDto.createResponse("-1", "정보를 불러오는 중 오류가 발생하였습니다", null));
    }
}