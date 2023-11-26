package com.peoplein.moiming.exception;

import lombok.Getter;

@Getter
public class MoimingApiException extends RuntimeException{

    private String errCode;

    public MoimingApiException(String errMsg) {
        super(errMsg);
        this.errCode = "-1";
    }

    public MoimingApiException(String errCode, String errMsg){
        super(errMsg);
        this.errCode = errCode;
    }

    public MoimingApiException(ExceptionValue exceptionValue) {
        super(exceptionValue.getErrMsg());
        this.errCode = exceptionValue.getErrCode();
    }
}
