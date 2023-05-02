package com.peoplein.moiming.model.dto.auth;

import lombok.*;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberSigninRequestDto {

    private String uid;
    private String password;
    private String email;
    private String fcmToken;

}
