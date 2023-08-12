package com.peoplein.moiming.model.dto.auth;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberLoginDto {

    private String memberEmail;
    private String password;

}
