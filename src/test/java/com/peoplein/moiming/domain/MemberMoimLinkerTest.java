package com.peoplein.moiming.domain;

import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimMemberStateAction;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class MemberMoimLinkerTest {

    @Test
    void judgeJoinSuccess() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.initMoimAndRuleJoin();
        MemberMoimLinker memberMoimLinker = TestUtils.createNormalMemberMoimLinkerWithWait(member, moim);
        MoimMemberStateAction input = MoimMemberStateAction.PERMIT;

        // when
        memberMoimLinker.judgeJoin(input);

        // then
        assertThat(memberMoimLinker.getMemberState()).isEqualTo(MoimMemberState.ACTIVE);
        assertThat(memberMoimLinker.getMoim().getCurMemberCount()).isEqualTo(1);
    }

    @Test
    void judgeJoinDecline() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.initMoimAndRuleJoin();
        MemberMoimLinker memberMoimLinker = TestUtils.createNormalMemberMoimLinkerWithWait(member, moim);
        MoimMemberStateAction input = MoimMemberStateAction.DECLINE;

        // when
        memberMoimLinker.judgeJoin(input);

        // then
        assertThat(memberMoimLinker.getMemberState()).isEqualTo(MoimMemberState.DECLINE);
        assertThat(memberMoimLinker.getMoim().getCurMemberCount()).isEqualTo(0);
    }

    @Test
    void judgeJoinThrowErrorWithUnexpectedInput() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.initMoimAndRuleJoin();
        MemberMoimLinker memberMoimLinker = TestUtils.createNormalMemberMoimLinker(member, moim);
        MoimMemberStateAction input = MoimMemberStateAction.IBF;

        // when + then
        assertThatThrownBy(() -> memberMoimLinker.judgeJoin(input))
                .isInstanceOf(IllegalArgumentException.class);
    }
}