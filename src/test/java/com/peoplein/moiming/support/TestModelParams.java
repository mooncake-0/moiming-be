package com.peoplein.moiming.support;


import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MemberGender;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;

@Profile("test")
public abstract class TestModelParams {

    public static String memberEmail = "a@moiming.io";
    public static String memberEmail2 = "b@moiming.io";
    public static String password = "1234";
    public static String memberName = "강우석";
    public static String memberName2 = "이현주";
    public static String nickname = "NICKNAME";
    public static String nickname2 = "NICKNAME2";
    public static String memberPhone = "01012345678";
    public static String memberPhone2 = "01023456789";
    public static MemberGender memberGender = MemberGender.M;
    public static boolean notForeigner = false;
    public static boolean foreigner = true;
    public static LocalDate memberBirth = LocalDate.of(1995, 12, 18);
    public static String memberBirthStringFormat = "1995-12-18";
    public static String fcmToken = "FCM_TOKEN";

    public static String accessToken = "ACCESS_TOKEN";
    public static String refreshToken = "REFRESH_TOKEN";


    // 1번 Moim 변수
    public static String moimName = "모이밍 모임";
    public static String moimInfo = "모이밍 앱 MVP를 만들어 나가는 사람들의 모임입니다";
    public static int maxMember = 10;
    public static Area moimArea = new Area("서울시", "중구");
    public static String depth1SampleCategory = "운동/스포츠";
    public static String depth2SampleCategory = "골프";


    // 2번 Moim 변수
    public static String moimName2 = "디지몬어드벤쳐";
    public static String moimInfo2 = "선택받은 아이들의 모임입니다";
    public static int maxMember2 = 20;
    public static Area moimArea2 = new Area("성남시", "분당구");
    public static String depth1SampleCategory2 = "댄스/무용";
    public static String depth2SampleCategory2 = "재즈댄스";



}