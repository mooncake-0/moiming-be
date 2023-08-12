package com.peoplein.moiming.model.dto.auth;

import com.peoplein.moiming.domain.enums.MemberGender;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberSigninRequestDto {

    //    private String uid;
    private String memberEmail;
    private String password;
    private String memberName;
    private String memberPhone;
    private MemberGender memberGender;
    private LocalDate memberBirth;
    private String fcmToken;

}
