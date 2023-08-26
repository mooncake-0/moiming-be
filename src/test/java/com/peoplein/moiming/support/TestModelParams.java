package com.peoplein.moiming.support;


import com.peoplein.moiming.domain.enums.MemberGender;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;

@Profile("test")
public abstract class TestModelParams {

    public static String memberEmail = "a@moiming.io";
    public static String password = "1234";
    public static String memberName = "강우석";
    public static String nickname = "NICKNAME";
    public static String memberPhone = "01012345678";
    public static MemberGender memberGender = MemberGender.M;
    public static boolean notForeigner = false;
    public static boolean foreigner = true;
    public static LocalDate memberBirth = LocalDate.of(1995, 12, 18);
    public static String memberBirthStringFormat = "1995-12-18";
    public static String fcmToken = "FCM_TOKEN";

    public static String accessToken = "ACCESS_TOKEN";
    public static String refreshToken = "REFRESH_TOKEN";

}