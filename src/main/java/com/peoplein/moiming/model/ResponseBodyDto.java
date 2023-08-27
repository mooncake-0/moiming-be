package com.peoplein.moiming.model;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResponseBodyDto<T> {

    private int code;
    private String msg;
    private T data;

    private ResponseBodyDto(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ResponseBodyDto<T> createResponse(int code, String msg, T data) {
        return new ResponseBodyDto<>(code, msg, data);
    }


}