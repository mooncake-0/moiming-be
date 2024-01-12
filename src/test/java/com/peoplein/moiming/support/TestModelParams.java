package com.peoplein.moiming.support;


import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MemberGender;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;

@Profile("test")
public abstract class TestModelParams {

    // Member Test 공통
    public static String password = "1234";
    public static MemberGender memberGender = MemberGender.M;
    public static boolean notForeigner = false;
    public static boolean foreigner = true;
    public static LocalDate memberBirth = LocalDate.of(1995, 12, 18);
    public static String memberBirthStringFormat = "1995-12-18";

    // 1번 Member
    public static String memberEmail = "a@moiming.io";
    public static String memberName = "강우석";
    public static String nickname = "NICKNAME1";
    public static String memberPhone = "01012345678";
    public static String ci = "A!2B=C3D+4E5F6G7H8I9J0K1L=M3N4O5P6Q7R8S9T0U1V2W3X4Y5Z6a7b8c9d+0e1f2g3h4i5j6k7lM9n0o1p2";


    // 2번 Member
    public static String memberEmail2 = "b@moiming.io";
    public static String memberName2 = "이현주";
    public static String nickname2 = "NICKNAME2";
    public static String memberPhone2 = "01023456789";
    public static String ci2 = "9a8B7C6D5E4F3G2H1I0J9K8L=M6N5O4P3Q2R1S0T9U8V7W6X5Y4Z6A7B8C9D+0E1F2G3H4I5J6K7L9M0N1O2P3";


    // 3번 Member
    public static String memberEmail3 = "c@moiming.io";
    public static String memberName3 = "김우진";
    public static String nickname3 = "NICKNAME3";
    public static String memberPhone3 = "01098765432";
    public static String ci3 = "Q7R8S9T0U1V2W3X4Y5Z6a7b8c9d+0e1f2g3h4i5j6k7lM9n0o1p2A!2B=C3D+4E5F6G7H8I9J0K1L=M3N4O5";


    // 4번 Member
    public static String memberEmail4 = "d@moiming.io";
    public static String memberName4 = "서주연";
    public static String nickname4 = "NICKNAME4";
    public static String memberPhone4 = "01087654321";
    public static String ci4 = "0P6Q7R8S9T0U1V2W3X4Y5Z6a7b8c9d+0e1f2g3h4i5j6k7lM9n0o1p2A!2B=C3D+4E5F6G7H8I9J0K1L=M3N4";


    // 5번 Member
    public static String memberEmail5 = "q@moiming.io";
    public static String memberName5 = "황찬규";
    public static String nickname5 = "NICKNAME5";
    public static String memberPhone5 = "01037838432";
    public static String ci5 = "0P6Q7R8S9Q7R8S2W3X4Y5Z6a7b8c9d+0e1f2g3h4i5j6k7lM9n0o1p2A!2B=C3D+4E5F6G7H8I9J0K1L=M3N4";


    public static String fcmToken = "FCM_TOKEN";
    public static String accessToken = "ACCESS_TOKEN";
    public static String refreshToken = "REFRESH_TOKEN";


    // 1번 Moim
    public static String moimName = "모이밍 모임";
    public static String moimInfo = "모이밍 앱 MVP를 만들어 나가는 사람들의 모임입니다";
    public static int maxMember = 10;
    public static Area moimArea = new Area("서울시", "중구");
    public static String depth1SampleCategory = "운동/스포츠";
    public static String depth2SampleCategory = "골프";


    // 2번 Moim
    public static String moimName2 = "디지몬어드벤쳐";
    public static String moimInfo2 = "선택받은 아이들의 모임입니다";
    public static int maxMember2 = 20;
    public static Area moimArea2 = new Area("성남시", "분당구");
    public static String depth1SampleCategory2 = "댄스/무용";
    public static String depth2SampleCategory2 = "재즈댄스";


    // MoimJoinRule Set1
    public static boolean hasAgeRule1 = false;
    public static int ageMax1 = 100; // -1 로 변해서 저장
    public static int ageMin1 = 15; // -1 로 변해서 저장
    public static MemberGender genderRule1 = MemberGender.N;

    // MoimJoinRule Set2
    public static boolean hasAgeRule2 = true;
    public static int ageMax2 = 40;
    public static int ageMin2 = 20;
    public static MemberGender genderRule2 = MemberGender.N;

}