package com.peoplein.moiming.model;

import lombok.*;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResponseModel<T> {

    private T data;

    private ResponseModel(T data) {
        this.data = data;
    }

    public static <T> ResponseModel<T> createResponse(T data) {
        return new ResponseModel<>(data);
    }


}
