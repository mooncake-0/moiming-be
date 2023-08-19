package com.peoplein.moiming.model.dto.requesta;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class TokenReqDto {

    @NotEmpty
    private String expiredAccessToken;

    @NotEmpty
    private String curRefreshToken;

}
