package com.peoplein.moiming.model.dto.response;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@ApiModel(value = "Auth API - 응답 - Token 갱신")
@Getter
@Setter
@AllArgsConstructor
public class TokenRespDto {

    String refreshToken;

}