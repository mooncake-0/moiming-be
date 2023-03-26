package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.rules.RuleJoin;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

public class UtilsRepository {


    /*
    Member 정보
     */
    public static String uid = "wrock.kang";
    public static String password = "1234";
    public static String refreshToken = "REFRESH_TOKEN";
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
    public static String memberPfImg = "/user/pf-img/wrock.kang.jpg";
    public static boolean isDormant = false;


    public static String moimName = "모이밍";
    public static String moimInfo = "모이밍을 만드는 사람들입니다";
    public static String moimPfImg = "";
    public static boolean hasRuleJoin = true;
    public static String areaState = "서울시";
    public static String areaCity = "중구";
    public static String createdUid = "wrock.kang";


    // moim post
    public static String postTitle = "postTitle";
    public static String postContent = "postContent";
    public static boolean isNotice = false;
    public static boolean hasFiles = false;


    public static Member initMemberAndMemberInfo() {
//        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        password = passwordEncoder.encode(password);
//        MemberInfo memberInfo = new MemberInfo(memberEmail, memberName, memberGender);
//        Member member = Member.createMember(uid, password, memberInfo);

        Role role = new Role(1L, "admin", RoleType.ADMIN);
//        MemberRoleLinker memberRoleLinker = MemberRoleLinker.grantRoleToMember(member, role);

        return Member.createMember(uid, password, memberEmail, memberName, memberGender, role);
    }

    public static Moim initMoim() {
        Moim moim = Moim.createMoim(moimName, moimInfo, moimPfImg, new Area(areaState, areaCity), createdUid);
        RuleJoin ruleJoin = new RuleJoin(1, 1, MemberGender.F, 1, true, true, moim, createdUid);
        return moim;
    }

    public static MoimPost initMoimPost(Moim moim, Member member) {
        return MoimPost.createMoimPost(postTitle, postContent, MoimPostCategory.EXTRA, isNotice, hasFiles, moim, member);
    }


}
