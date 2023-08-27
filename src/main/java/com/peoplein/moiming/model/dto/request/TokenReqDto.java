package com.peoplein.moiming.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@ApiModel(value = "Auth API - 요청 - Token 갱신")
@Getter
@Setter
public class TokenReqDto {

    @ApiModelProperty(value = "String 값 'REFRESH_TOKEN' 고정")
    @Pattern(regexp = "REFRESH_TOKEN", message = "String 값 'REFRESH_TOKEN' 고정")
    private String grantType;

    @NotEmpty
    private String token;

}