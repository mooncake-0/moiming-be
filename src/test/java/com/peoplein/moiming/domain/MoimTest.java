//package com.peoplein.moiming.domain;
//
//import com.peoplein.moiming.TestUtils;
//import com.peoplein.moiming.domain.embeddable.Area;
//import com.peoplein.moiming.domain.enums.MemberGender;
//import com.peoplein.moiming.domain.enums.MoimMemberState;
//import com.peoplein.moiming.domain.enums.MoimRoleType;
//import com.peoplein.moiming.domain.enums.RoleType;
//import com.peoplein.moiming.domain.fixed.Role;
//import com.peoplein.moiming.domain.rules.RuleJoin;
//import com.peoplein.moiming.domain.rules.RulePersist;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.assertj.core.api.Assertions.*;
//
//public class MoimTest {
//
//    private static String moimName = "모이밍";
//    private static String moimInfo = "모이밍을 만드는 사람들입니다";
//    private static String moimPfImg = "";
//    private static boolean hasRuleJoin = true;
//    private static String areaState = "서울시";
//    private static String areaCity = "중구";
//    private static String createdUid = "wrock.kang";
//
//    // Rule Join 형성 Data
//    private static int birthMax = 1995;
//    private static int birthMin = 2000;
//    private static MemberGender memberGender = MemberGender.M;
//    private static int moimMaxCount = 3;
//
//    // Rule Persist 형성 Data
//    private static boolean doGreeting = true;
//    private static int attendMonthly = 2;
//    private static int attendCount = 2;
//
//    private Moim moim;
//
//    Moim dummyMoim1;
//    Moim dummyMoim2;
//
//    Moim sanMoim;
//
//    @BeforeEach
//    void be() {
//        moim = Moim.createMoim(TestUtils.moimName, TestUtils.moimInfo, TestUtils.moimPfImg, new Area(TestUtils.areaState, TestUtils.areaCity), TestUtils.createdUid);
//        sanMoim = Moim.createMoim(TestUtils.moimName, TestUtils.moimInfo, TestUtils.moimPfImg, new Area(TestUtils.areaState, TestUtils.areaCity), TestUtils.createdUid);
//    }
//
//    @BeforeEach
//    void createDummyMoim() {
//        dummyMoim1 = Moim.createMoim(TestUtils.moimName + "1", TestUtils.moimInfo + "1", TestUtils.moimPfImg + "1", new Area(TestUtils.areaState, TestUtils.areaCity), TestUtils.createdUid + "1");
//        dummyMoim2 = Moim.createMoim(TestUtils.moimName + "2", TestUtils.moimInfo + "2", TestUtils.moimPfImg + "2", new Area(TestUtils.areaState, TestUtils.areaCity), TestUtils.createdUid + "1");
//    }
//
//    @Test
//    @DisplayName("성공 @ 생성자")
//    void 생성자_성공() {
//        // given + when
//
//        // then
//        assertThat(moim.getMoimName()).isEqualTo(TestUtils.moimName);
//        assertThat(moim.getMoimInfo()).isEqualTo(TestUtils.moimInfo);
//        assertThat(moim.getMoimPfImg()).isEqualTo(TestUtils.moimPfImg);
//        assertThat(moim.getMoimArea().getState()).isEqualTo(TestUtils.areaState);
//        assertThat(moim.getMoimArea().getCity()).isEqualTo(TestUtils.areaCity);
//        assertThat(moim.getCreatedUid()).isEqualTo(TestUtils.createdUid);
//        assertThat(moim.isHasRulePersist()).isFalse();
//        assertThat(moim.getCurMemberCount()).isEqualTo(0);
//    }
//
//    @Test
//    @DisplayName("실패 @ 생성자")
//    void 생성자_실패() {
//        //given
//        //when
//        //then
//        assertThatThrownBy(() -> Moim.createMoim("", moimInfo, moimPfImg, new Area(areaState, areaCity), createdUid)).isInstanceOf(IllegalArgumentException.class);
//        assertThatThrownBy(() -> Moim.createMoim(null, moimInfo, moimPfImg, new Area(areaState, areaCity), createdUid)).isInstanceOf(IllegalArgumentException.class);
//        // 빈값이여도 됨
//        assertThatThrownBy(() -> Moim.createMoim(moimName, moimInfo, moimPfImg, new Area("", areaCity), createdUid)).isInstanceOf(IllegalArgumentException.class);
//        assertThatThrownBy(() -> Moim.createMoim(moimName, moimInfo, moimPfImg, new Area(null, areaCity), createdUid)).isInstanceOf(IllegalArgumentException.class);
//        assertThatThrownBy(() -> Moim.createMoim(moimName, moimInfo, moimPfImg, new Area(areaState, ""), createdUid)).isInstanceOf(IllegalArgumentException.class);
//        assertThatThrownBy(() -> Moim.createMoim(moimName, moimInfo, moimPfImg, new Area(areaState, null), "")).isInstanceOf(IllegalArgumentException.class);
//        assertThatThrownBy(() -> Moim.createMoim(moimName, moimInfo, moimPfImg, new Area(areaState, null), null)).isInstanceOf(IllegalArgumentException.class);
//    }
//
//    @Test
//    @DisplayName("성공 @ RuleJoin 연관관계 설정")
//    void RuleJoin_설정_성공() {
//        //given
//        //when
//        RuleJoin ruleJoin = new RuleJoin(
//                birthMax, birthMin, memberGender, moimMaxCount, false, false, moim, createdUid
//        );
//
//        //then
//        assertTrue(moim.isHasRuleJoin());
//        assertEquals(ruleJoin, moim.getRuleJoin());
//        assertEquals(ruleJoin.getBirthMax(), moim.getRuleJoin().getBirthMax());
//        assertEquals(ruleJoin.getBirthMin(), moim.getRuleJoin().getBirthMin());
//        assertEquals(ruleJoin.getGender(), moim.getRuleJoin().getGender());
//        assertEquals(ruleJoin.getMoimMaxCount(), moim.getRuleJoin().getMoimMaxCount());
//        assertEquals(1, moim.getMoimRules().size());
//    }
//
//    @Test
//    @DisplayName("성공 @ RulePersist 연관관계 설정")
//    void RulePersist_설정_성공() {
//        //given
//        //when
//        RulePersist rulePersist = new RulePersist(
//                doGreeting, attendMonthly, attendCount, moim, createdUid
//        );
//        //then
//        assertTrue(moim.isHasRulePersist());
//        assertEquals(rulePersist, moim.getRulePersist());
//        assertEquals(doGreeting, moim.getRulePersist().isDoGreeting());
//        assertEquals(attendMonthly, moim.getRulePersist().getAttendMonthly());
//        assertEquals(attendCount, moim.getRulePersist().getAttendCount());
//        assertEquals(1, moim.getMoimRules().size());
//    }
//
//    @Test
//    @DisplayName("성공 @ 두 Rules 모두 설정")
//    void Rules_설정_성공() {
//        //given
//        //when
//        RuleJoin ruleJoin = new RuleJoin(
//                birthMax, birthMin, memberGender, moimMaxCount, false, false, moim, createdUid
//        );
//        RulePersist rulePersist = new RulePersist(
//                doGreeting, attendMonthly, attendCount, moim, createdUid
//        );
//
//        // then
//        assertTrue(moim.isHasRuleJoin());
//        assertTrue(moim.isHasRulePersist());
//        assertEquals(ruleJoin, moim.getRuleJoin());
//        assertEquals(rulePersist, moim.getRulePersist());
//        assertEquals(2, moim.getMoimRules().size());
//    }
//
//    @Test
//    @DisplayName("checkRuleJoin : 생일 범위 내면 가입 가능.")
//    void checkRuleJoinSuccessWithBirthdayTest() {
//        // given
//        int birthMax = 2000;
//        int birthMin = 1990;
//        Moim moim = Moim.createMoim(TestUtils.moimName, TestUtils.moimInfo, TestUtils.moimPfImg, new Area(TestUtils.areaState, TestUtils.areaCity), TestUtils.createdUid);
//        Member member = createMember(MemberGender.M, LocalDate.of(birthMin + 1, 1, 1));
//        List<MemberMoimLinker> memberMoimLinkers = List.of();
//
//
//        // birthMax, birthMin
//        RuleJoin ruleJoin = new RuleJoin(birthMax, birthMin, MemberGender.N, TestUtils.MOIM_MAX_COUNT, TestUtils.dupLeaderAvailable, TestUtils.dupManagerAvailable, moim, member.getUid(), false, false);
//
//        // when
//        MoimMemberState moimMemberState = moim.checkRuleJoinCondition(member.getMemberInfo(), memberMoimLinkers);
//
//        // then
//        assertThat(moimMemberState).isEqualTo(MoimMemberState.ACTIVE);
//    }
//
//    @Test
//    @DisplayName("checkRuleJoin : 생일 범위 밖이면 가입 불가능")
//    void checkRuleJoinFailWithBirthdayTest() {
//        // given
//        int birthMax = 2000;
//        int birthMin = 1990;
//        Moim moim = Moim.createMoim(TestUtils.moimName, TestUtils.moimInfo, TestUtils.moimPfImg, new Area(TestUtils.areaState, TestUtils.areaCity), TestUtils.createdUid);
//        Member member = createMember(MemberGender.M, LocalDate.of(birthMin-1, 1, 1));
//        List<MemberMoimLinker> memberMoimLinkers = List.of();
//
//        // birthMax, birthMin
//        RuleJoin ruleJoin = new RuleJoin(birthMax, birthMin, MemberGender.N, TestUtils.MOIM_MAX_COUNT, TestUtils.dupLeaderAvailable, TestUtils.dupManagerAvailable, moim, member.getUid(), false, false);
//
//        // when
//        MoimMemberState moimMemberState = moim.checkRuleJoinCondition(member.getMemberInfo(), memberMoimLinkers);
//
//        // then
//        assertThat(moimMemberState).isEqualTo(MoimMemberState.WAIT_BY_AGE);
//    }
//
//    @Test
//    @DisplayName("checkRuleJoin : 모임에서 지정한 조건 성별 가입 가능")
//    void checkRuleJoinSuccessWithGenderConditionTestCase1() {
//        // given
//        int birthMax = 2000;
//        int birthMin = 1990;
//        Moim moim = Moim.createMoim(TestUtils.moimName, TestUtils.moimInfo, TestUtils.moimPfImg, new Area(TestUtils.areaState, TestUtils.areaCity), TestUtils.createdUid);
//        Member member = createMember(MemberGender.M, LocalDate.of(birthMin + 1, 1, 1));
//        List<MemberMoimLinker> memberMoimLinkers = List.of();
//
//        // birthMax, birthMin
//        RuleJoin ruleJoin = new RuleJoin(birthMax, birthMin, MemberGender.M, TestUtils.MOIM_MAX_COUNT, TestUtils.dupLeaderAvailable, TestUtils.dupManagerAvailable, moim, member.getUid(), false, false);
//
//        // when
//        MoimMemberState moimMemberState = moim.checkRuleJoinCondition(member.getMemberInfo(), memberMoimLinkers);
//
//        // then
//        assertThat(moimMemberState).isEqualTo(MoimMemberState.ACTIVE);
//    }
//
//    @Test
//    @DisplayName("checkRuleJoin : 모임에서 성별 조건 없을 경우, 가입 가능.")
//    void checkRuleJoinSuccessWithGenderConditionTestCase2() {
//        // given
//        int birthMax = 2000;
//        int birthMin = 1990;
//        Moim moim = Moim.createMoim(TestUtils.moimName, TestUtils.moimInfo, TestUtils.moimPfImg, new Area(TestUtils.areaState, TestUtils.areaCity), TestUtils.createdUid);
//        Member member = createMember(MemberGender.M, LocalDate.of(birthMin + 1, 1, 1));
//        List<MemberMoimLinker> memberMoimLinkers = List.of();
//
//        // Gender: N
//        RuleJoin ruleJoin = new RuleJoin(birthMax, birthMin, MemberGender.N, TestUtils.MOIM_MAX_COUNT, TestUtils.dupLeaderAvailable, TestUtils.dupManagerAvailable, moim, member.getUid(), false, false);
//
//        // when
//        MoimMemberState moimMemberState = moim.checkRuleJoinCondition(member.getMemberInfo(), memberMoimLinkers);
//
//        // then
//        assertThat(moimMemberState).isEqualTo(MoimMemberState.ACTIVE);
//    }
//
//    @Test
//    @DisplayName("checkRuleJoin : 모임에서 지정한 조건 성별과 다를 경우 가입 불가능.")
//    void checkRuleJoinFailWithGenderConditionTest() {
//        // given
//        int birthMax = 2000;
//        int birthMin = 1990;
//        Moim moim = Moim.createMoim(TestUtils.moimName, TestUtils.moimInfo, TestUtils.moimPfImg, new Area(TestUtils.areaState, TestUtils.areaCity), TestUtils.createdUid);
//        Member member = createMember(MemberGender.M, LocalDate.of(birthMin + 1, 1, 1));
//        List<MemberMoimLinker> memberMoimLinkers = List.of();
//
//        // Gender : F
//        RuleJoin ruleJoin = new RuleJoin(birthMax, birthMin, MemberGender.F, TestUtils.MOIM_MAX_COUNT, TestUtils.dupLeaderAvailable, TestUtils.dupManagerAvailable, moim, member.getUid(), false, false);
//
//        // when
//        MoimMemberState moimMemberState = moim.checkRuleJoinCondition(member.getMemberInfo(), memberMoimLinkers);
//
//        // then
//        assertThat(moimMemberState).isEqualTo(MoimMemberState.WAIT_BY_GENDER);
//    }
//
//    @Test
//    @DisplayName("checkRuleJoin : 모임이 하나도 없을 때, 첫 가입 가능.")
//    void checkRuleJoinSuccessWithMoimCountTestCase1() {
//        // given
//        int birthMax = 2000;
//        int birthMin = 1990;
//        Moim moim = Moim.createMoim(TestUtils.moimName, TestUtils.moimInfo, TestUtils.moimPfImg, new Area(TestUtils.areaState, TestUtils.areaCity), TestUtils.createdUid);
//        Member member = createMember(MemberGender.M, LocalDate.of(birthMin + 1, 1, 1));
//        List<MemberMoimLinker> memberMoimLinkers = List.of();
//
//        // moimMaxCount: 1
//        RuleJoin ruleJoin = new RuleJoin(birthMax, birthMin, MemberGender.M, 1, TestUtils.dupLeaderAvailable, TestUtils.dupManagerAvailable, moim, member.getUid(), false, false);
//
//        // when
//        MoimMemberState moimMemberState = moim.checkRuleJoinCondition(member.getMemberInfo(), memberMoimLinkers);
//
//        // then
//        assertThat(moimMemberState).isEqualTo(MoimMemberState.ACTIVE);
//    }
//    @Test
//    @DisplayName("checkRuleJoin : 모임 Rule보다 더 많은 모임을 가진 경우, 가입 불가.")
//    void checkRuleJoinFailWithMoimCountTest() {
//        // given
//        int birthMax = 2000;
//        int birthMin = 1990;
//        Moim moim = Moim.createMoim(TestUtils.moimName, TestUtils.moimInfo, TestUtils.moimPfImg, new Area(TestUtils.areaState, TestUtils.areaCity), TestUtils.createdUid);
//        Member member = createMember(MemberGender.M, LocalDate.of(birthMin + 1, 1, 1));
//        MemberMoimLinker memberMoimLinker1 = MemberMoimLinker.memberJoinMoim(member, dummyMoim1, MoimRoleType.NORMAL, MoimMemberState.ACTIVE);
//        List<MemberMoimLinker> memberMoimLinkers = List.of(memberMoimLinker1);
//
//        // moimMaxCount : 1
//        RuleJoin ruleJoin = new RuleJoin(birthMax, birthMin, MemberGender.M, 1, TestUtils.dupLeaderAvailable, TestUtils.dupManagerAvailable, moim, member.getUid(), false, false);
//
//        // when
//        MoimMemberState moimMemberState = moim.checkRuleJoinCondition(member.getMemberInfo(), memberMoimLinkers);
//
//        // then
//        assertThat(moimMemberState).isEqualTo(MoimMemberState.WAIT_BY_MOIM_CNT);
//    }
//
//    @Test
//    @DisplayName("checkRuleJoin : 다른 모임에 리더인 경우, 가입 불가능.")
//    void checkRuleJoinFailWithDupLeaderTestCase1() {
//        // given
//        int birthMax = 2000;
//        int birthMin = 1990;
//        Moim moim = Moim.createMoim(TestUtils.moimName, TestUtils.moimInfo, TestUtils.moimPfImg, new Area(TestUtils.areaState, TestUtils.areaCity), TestUtils.createdUid);
//        Member member = createMember(MemberGender.M, LocalDate.of(birthMin + 1, 1, 1));
//        MemberMoimLinker memberMoimLinker1 = MemberMoimLinker.memberJoinMoim(member, dummyMoim1, MoimRoleType.LEADER, MoimMemberState.ACTIVE);
//        List<MemberMoimLinker> memberMoimLinkers = List.of(memberMoimLinker1);
//
//        // dupLeaderAvailable : false
//        RuleJoin ruleJoin = new RuleJoin(birthMax, birthMin, MemberGender.M, 2, false, TestUtils.dupManagerAvailable, moim, member.getUid(), false, false);
//
//        // when
//        MoimMemberState moimMemberState = moim.checkRuleJoinCondition(member.getMemberInfo(), memberMoimLinkers);
//
//        // then
//        assertThat(moimMemberState).isEqualTo(MoimMemberState.WAIT_BY_DUP);
//    }
//
//    @Test
//    @DisplayName("checkRuleJoin : 다른 모임에 매니저인 경우, 가입 불가능.")
//    void checkRuleJoinFailWithMoimCountTest1() {
//        // given
//        int birthMax = 2000;
//        int birthMin = 1990;
//        Moim moim = Moim.createMoim(TestUtils.moimName, TestUtils.moimInfo, TestUtils.moimPfImg, new Area(TestUtils.areaState, TestUtils.areaCity), TestUtils.createdUid);
//        Member member = createMember(MemberGender.M, LocalDate.of(birthMin + 1, 1, 1));
//        MemberMoimLinker memberMoimLinker1 = MemberMoimLinker.memberJoinMoim(member, dummyMoim1, MoimRoleType.MANAGER, MoimMemberState.ACTIVE);
//        List<MemberMoimLinker> memberMoimLinkers = List.of(memberMoimLinker1);
//
//        // dupManagerAvailable : false
//        RuleJoin ruleJoin = new RuleJoin(birthMax, birthMin, MemberGender.M, 2, TestUtils.dupLeaderAvailable, false, moim, member.getUid(), false, false);
//
//        // when
//        MoimMemberState moimMemberState = moim.checkRuleJoinCondition(member.getMemberInfo(), memberMoimLinkers);
//
//        // then
//        assertThat(moimMemberState).isEqualTo(MoimMemberState.WAIT_BY_DUP);
//    }
//
//
//    Member createMember(MemberGender memberGender, LocalDate memberBirth) {
////        MemberInfo memberInfo = new MemberInfo(TestUtils.memberEmail, TestUtils.memberName, memberGender);
////        memberInfo.setMemberBirth(memberBirth);
////        Member member = Member.createMember(TestUtils.uid, TestUtils.encryptedPassword, memberInfo);
//
//        Role role = new Role(1L, "admin", RoleType.USER);
//
//        Member member = Member.createMember(TestUtils.uid, TestUtils.password, TestUtils.memberEmail,
//                TestUtils.memberName, TestUtils.fcmToken, TestUtils.memberGender, role);
//
//        member.getMemberInfo().setMemberBirth(memberBirth);
//
//
//        return member;
//    }
//}