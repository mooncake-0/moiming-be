package com.peoplein.moiming.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@ApiModel(value = "Auth API - 요청 - Token 갱신")
@Getter
@Setter
public class TokenReqDto {

    @ApiModelProperty(value = "String 값 'REFRESH_TOKEN' 고정")
    @NotEmpty
    private String grantType;

    @NotEmpty
    private String token;

}