package com.peoplein.moiming;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.model.dto.domain.MoimDto;
import com.peoplein.moiming.model.dto.request_b.MoimMemberActionRequestDto;
import com.peoplein.moiming.model.dto.request_b.MoimPostRequestDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.List;

public class TestUtils {


    /*
    Member 정보
     */
    public static String uid = "wrock.kang";
    public static String password = "1234";
    public static String encryptedPassword = new BCryptPasswordEncoder().encode(password);
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


    public static String moimName = "모이밍";
    public static String moimInfo = "모이밍을 만드는 사람들입니다";
    public static String moimPfImg = "";
    public static boolean hasRuleJoin = true;
    public static String areaState = "서울시";
    public static String areaCity = "중구";
    public static String createdUid = "wrock.kang";
    public static Area AREA = new Area(areaState, areaCity);
    public static boolean MOIM_DTO_RULE_JOIN = true;


    // Moim Rule
    public static int birthMaxForBigRange = 2020;
    public static int birthMinForBigRange = 1950;

    public static int moimCountBig = 1000;
    public static MemberGender memberGenderAny = MemberGender.N;

    public static boolean dupLeaderAvailable = true;
    public static boolean dupManagerAvailable = true;

    // moim post
    public static String postTitle = "postTitle";
    public static String postContent = "postContent";
    public static boolean isNotice = false;
    public static boolean hasFiles = false;
    public static MoimPostCategory moimPostCategory = MoimPostCategory.GREETING;


    // moimService

    public static MoimMemberStateAction MOIM_MEMBER_STATE_ACTION = MoimMemberStateAction.PERMIT;
    public static MoimMemberRoleType MOIM_SERVICE_ROLE_TYPE = MoimMemberRoleType.NORMAL;
    public static String INACTIVE_REASON = "temporary";
    public static boolean BAN_REJOIN = true;

    // Rule Join DTO
    public static int BIRTH_MAX = 20;
    public static int BIRTH_MIN = 10;
    public static int MOIM_MAX_COUNT = 10;
    public static boolean DUPLICATED_LEADER_ENABLE = true;
    public static boolean DUPLICATED_MANAGER_ENABLE = true;


    public static final Role role = initAdminRole();

    public static Member initOtherMemberAndMemberInfo() {
        Role role = initUserRole();

        Member member = Member.createMember("other" + memberEmail
                , "other" + password
                , "other" + memberName
                , "other" + memberPhone
                , memberGender
                , false
                , memberBirth
                , "other" + fcmToken
                , "ci"
                , role);

        return member;
    }

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


    public static Moim initMoimAndRuleJoin() {
        return null;
//        Moim moim = Moim.createMoim(moimName, moimInfo, moimPfImg, new Area(areaState, areaCity), 1L);
//        RuleJoin ruleJoin = new RuleJoin(1, 1, MemberGender.F, 1, true, true, moim, 1L);
//        return moim;
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

    public static Moim createOtherMoimOnly(String moimName, Area area) {
        return null;
//        return Moim.createMoim(moimName, "other" + moimInfo, "other" + moimPfImg, area, 1L);
    }

    public static MoimPost initMoimPost(Moim moim, Member member) {
        return MoimPost.createMoimPost(postTitle, postContent, moimPostCategory, isNotice, hasFiles, moim, member);
    }

    public static MoimPost initNoticeMoimPost(Moim moim, Member member) {
        return MoimPost.createMoimPost(postTitle, postContent, moimPostCategory, true, hasFiles, moim, member);
    }

    public static MoimDto initMoimDto() {
        return new MoimDto(moimName, moimInfo, moimPfImg, AREA, MOIM_DTO_RULE_JOIN);
    }

    public static MoimDto createOtherMoimDtoForUpdate() {
        return new MoimDto("other" + moimName,
                "other" + moimInfo,
                "other" + moimPfImg, AREA, MOIM_DTO_RULE_JOIN);
    }


    public static MoimMember createLeaderMemberMoimLinker(Member member, Moim moim) {
        return null;
//        return MoimMember.memberJoinMoim(member, moim, MoimMemberRoleType.LEADER, MoimMemberState.ACTIVE);
    }

    public static MoimMember createNormalMemberMoimLinker(Member member, Moim moim) {
        return MoimMember.memberJoinMoim(member, moim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
    }

    public static MoimMember createNormalMemberMoimLinkerWithWait(Member member, Moim moim) {
        return null;
//        return MoimMember.memberJoinMoim(member, moim, MoimMemberRoleType.NORMAL, MoimMemberState.WAIT_BY_MOIM_CNT);
    }


    public static List<CategoryName> initCategoryName() {
        return List.of(CategoryName.DANCE, CategoryName.BELLY_DANCE);
    }


    public static MoimPostRequestDto initMoimPostRequestDto() {

        List<CategoryName> categoryNames = TestUtils.initCategoryName();
        MoimPostRequestDto moimPostRequestDto = new MoimPostRequestDto(
                null, null,
                postTitle, postContent, isNotice, MoimPostCategory.EXTRA);
        return moimPostRequestDto;
    }

    public static MoimMemberActionRequestDto createActionRequestDto(Long moimId, Long memberId, MoimMemberStateAction moimMemberStateAction) {
        return new MoimMemberActionRequestDto(
                moimId, memberId, moimMemberStateAction, MoimMemberRoleType.NORMAL, "", true);
    }

    public static void truncateAllTable(JdbcTemplate jdbcTemplate) {

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        List<String> tableNames = List.of(
                "category",
                "moim_rule",
                "rule_join",
                "rule_persist",
                "member",
                "member_info",
                "member_moim_linker",
                "member_role_linker",
                "member_schedule_linker",
                "moim",
                "moim_category_linker",
                "moim_post",
                "moim_review",
                "post_comment",
                "post_file",
                "review_answer",
                "schedule",
                "role");

        tableNames.forEach(tableName ->
                jdbcTemplate.execute("TRUNCATE TABLE " + tableName));

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }


    public static Area createAreaForTest() {
        return new Area(TestUtils.areaState, TestUtils.areaCity);
    }

    public static Category createCategoryForTest() {
        return new Category(1L, CategoryName.AMITY, 1, null);
    }
}