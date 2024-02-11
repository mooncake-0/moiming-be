package com.peoplein.moiming.model.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenRespDto {
    private String accessToken;
    private Long accessTokenExp;
    private String refreshToken;
    private Long refreshTokenExp;
}
