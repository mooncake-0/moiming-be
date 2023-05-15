package com.peoplein.moiming.model.dto.auth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FindPwRequestDto {

    private String memberEmail;

    private String memberPhoneNumber;

}
