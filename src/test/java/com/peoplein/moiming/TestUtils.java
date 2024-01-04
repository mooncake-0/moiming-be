package com.peoplein.moiming;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.domain.moim.Moim;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;

public class TestUtils {


    /*
    Member 정보
     */
    public static String password = "1234";
    public static String fcmToken = "FCM_TOKEN";

    /*
     Member Info 정보
     */
    public static String memberEmail = "a@moiming.net";
    public static String memberName = "강우석";
    public static String memberPhone = "01087538643";
    public static MemberGender memberGender = MemberGender.M;

    /*
     추가 입력 정보
     */
    public static LocalDate memberBirth = LocalDate.of(1995, 12, 18);


    public static String moimName = "모이밍";
    public static String areaState = "서울시";
    public static String areaCity = "중구";




    public static final Role role = initAdminRole();

    public static Member initMemberAndMemberInfo() {
        Member member = Member.createMember(memberEmail, password, memberName, memberPhone
                , memberGender, false, memberBirth, fcmToken, "ci", role);
        return member;
    }

    public static Member initMemberAndMemberInfo(String memberName, String memberEmail) {
        Member member = Member.createMember(memberEmail, password, memberName, memberPhone
                , memberGender, false, memberBirth, fcmToken, "ci", role);

        return member;
    }

    public static Role initAdminRole() {
        return new Role(1L, "admin", RoleType.ADMIN);
    }

    public static Role initUserRole() {
        return new Role(2L, "admin", RoleType.USER);
    }



    public static Moim createMoimOnly() {
        return null;
//        Moim moim = Moim.createMoim(moimName, moimInfo, moimPfImg, new Area(areaState, areaCity), 1L);
//        moim.setHasRuleJoin(false);
//        return moim;
    }

    public static Moim createMoimOnly(String moimName) {
        return null;
//        return Moim.createMoim(moimName, moimInfo, moimPfImg, new Area(areaState, areaCity), 1L);
    }

    public static Area createAreaForTest() {
        return new Area(TestUtils.areaState, TestUtils.areaCity);
    }

    public static Category createCategoryForTest() {
        return new Category(1L, CategoryName.AMITY, 1, null);
    }
}