package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.rules.RuleJoin;
import com.peoplein.moiming.domain.rules.RulePersist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

public class MoimTest {

    private static String moimName = "모이밍";
    private static String moimInfo = "모이밍을 만드는 사람들입니다";
    private static String moimPfImg = "";
    private static boolean hasRuleJoin = true;
    private static String areaState = "서울시";
    private static String areaCity = "중구";
    private static String createdUid = "wrock.kang";

    // Rule Join 형성 Data
    private static int birthMax = 1995;
    private static int birthMin = 2000;
    private static MemberGender memberGender = MemberGender.M;
    private static int moimMaxCount = 3;

    // Rule Persist 형성 Data
    private static boolean doGreeting = true;
    private static int attendMonthly = 2;
    private static int attendCount = 2;

    private Moim moim;

    @BeforeEach
    void be() {
        moim = Moim.createMoim(moimName, moimInfo, moimPfImg, new Area(areaState, areaCity), createdUid);
    }

    @Test
    @DisplayName("성공 @ 생성자")
    void 생성자_성공() {
        //given
        //when
//        Moim moim = Moim.createMoim(moimName, moimInfo, moimPfImg, hasRuleJoin, new Area(areaState, areaCity), createdUid);
        //then
        assertEquals(moimName, moim.getMoimName());
        assertEquals(moimInfo, moim.getMoimInfo());
        assertEquals(moimPfImg, moim.getMoimPfImg());
        assertEquals(areaState, moim.getMoimArea().getState());
        assertEquals(areaCity, moim.getMoimArea().getCity());
        assertEquals(createdUid, moim.getCreatedUid());
        assertFalse(moim.isHasRulePersist());
        assertEquals(0, moim.getCurMemberCount());
    }

    @Test
    @DisplayName("실패 @ 생성자")
    void 생성자_실패() {
        //given
        //when
        //then
        assertThatThrownBy(() -> Moim.createMoim("", moimInfo, moimPfImg, new Area(areaState, areaCity), createdUid)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Moim.createMoim(null, moimInfo, moimPfImg, new Area(areaState, areaCity), createdUid)).isInstanceOf(IllegalArgumentException.class);
        // 빈값이여도 됨
//        assertThatThrownBy(() -> Moim.createMoim(moimName, "", moimPfImg, hasRuleJoin, new Area(areaState, areaCity), createdUid)).isInstanceOf(IllegalArgumentException.class);
//        assertThatThrownBy(() -> Moim.createMoim(moimName, null, moimPfImg, hasRuleJoin, new Area(areaState, areaCity), createdUid)).isInstanceOf(IllegalArgumentException.class);
//        assertThatThrownBy(() -> Moim.createMoim(moimName, moimInfo, "", hasRuleJoin, new Area(areaState, areaCity), createdUid)).isInstanceOf(IllegalArgumentException.class);
//        assertThatThrownBy(() -> Moim.createMoim(moimName, moimInfo, null, hasRuleJoin, new Area(areaState, areaCity), createdUid)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Moim.createMoim(moimName, moimInfo, moimPfImg, new Area("", areaCity), createdUid)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Moim.createMoim(moimName, moimInfo, moimPfImg, new Area(null, areaCity), createdUid)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Moim.createMoim(moimName, moimInfo, moimPfImg, new Area(areaState, ""), createdUid)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Moim.createMoim(moimName, moimInfo, moimPfImg, new Area(areaState, null), "")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Moim.createMoim(moimName, moimInfo, moimPfImg, new Area(areaState, null), null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("성공 @ RuleJoin 연관관계 설정")
    void RuleJoin_설정_성공() {
        //given
        //when
        RuleJoin ruleJoin = new RuleJoin(
                birthMax, birthMin, memberGender, moimMaxCount, false, false, moim, createdUid
        );

        //then
        assertTrue(moim.isHasRuleJoin());
        assertEquals(ruleJoin, moim.getRuleJoin());
        assertEquals(ruleJoin.getBirthMax(), moim.getRuleJoin().getBirthMax());
        assertEquals(ruleJoin.getBirthMin(), moim.getRuleJoin().getBirthMin());
        assertEquals(ruleJoin.getGender(), moim.getRuleJoin().getGender());
        assertEquals(ruleJoin.getMoimMaxCount(), moim.getRuleJoin().getMoimMaxCount());
        assertEquals(1, moim.getMoimRules().size());
    }

    @Test
    @DisplayName("성공 @ RulePersist 연관관계 설정")
    void RulePersist_설정_성공() {
        //given
        //when
        RulePersist rulePersist = new RulePersist(
                doGreeting, attendMonthly, attendCount, moim, createdUid
        );
        //then
        assertTrue(moim.isHasRulePersist());
        assertEquals(rulePersist, moim.getRulePersist());
        assertEquals(doGreeting, moim.getRulePersist().isDoGreeting());
        assertEquals(attendMonthly, moim.getRulePersist().getAttendMonthly());
        assertEquals(attendCount, moim.getRulePersist().getAttendCount());
        assertEquals(1, moim.getMoimRules().size());
    }

    @Test
    @DisplayName("성공 @ 두 Rules 모두 설정")
    void Rules_설정_성공() {
        //given
        //when
        RuleJoin ruleJoin = new RuleJoin(
                birthMax, birthMin, memberGender, moimMaxCount, false, false, moim, createdUid
        );
        RulePersist rulePersist = new RulePersist(
                doGreeting, attendMonthly, attendCount, moim, createdUid
        );

        // then
        assertTrue(moim.isHasRuleJoin());
        assertTrue(moim.isHasRulePersist());
        assertEquals(ruleJoin, moim.getRuleJoin());
        assertEquals(rulePersist, moim.getRulePersist());
        assertEquals(2, moim.getMoimRules().size());

    }
}