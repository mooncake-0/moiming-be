package com.peoplein.moiming.model;

import lombok.*;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResponseModel<T> {

    private Object status;
    private String msg;

    private T data;

    private ResponseModel(Object status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ResponseModel<T> createResponse(HttpStatus status, String msg, T data) {
        return new ResponseModel<>(status, msg, data);
    }


}