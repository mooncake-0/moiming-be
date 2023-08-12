package com.peoplein.moiming.model.inner;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 Transaction 범위 안에서 Refresh Token 은 재지정 필요하다 -> Service 영역
 Http Response Header 에 Token 정보 전달 필요하다 -> Controller 영역
 - 둘을 이어주는 내부 DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenTransmitter<T> {

    private String accessToken;
    private String refreshToken;
    private T data;

    public TokenTransmitter(String accessToken, String refreshToken, T data) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.data = data;
    }
}