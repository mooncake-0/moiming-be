package com.peoplein.moiming.domain;

import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimMemberStateAction;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.domain.moim.Moim;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MoimMemberTest {

    @Test
    void judgeJoinSuccess() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.initMoimAndRuleJoin();
        MoimMember moimMember = TestUtils.createNormalMemberMoimLinkerWithWait(member, moim);
        MoimMemberStateAction input = MoimMemberStateAction.PERMIT;

        // when
        moimMember.judgeJoin(input);

        // then
        assertThat(moimMember.getMemberState()).isEqualTo(MoimMemberState.ACTIVE);
        assertThat(moimMember.getMoim().getCurMemberCount()).isEqualTo(1);
    }

    @Test
    void judgeJoinDecline() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.initMoimAndRuleJoin();
        MoimMember moimMember = TestUtils.createNormalMemberMoimLinkerWithWait(member, moim);
        MoimMemberStateAction input = MoimMemberStateAction.DECLINE;

        // when
        moimMember.judgeJoin(input);

        // then
        assertThat(moimMember.getMemberState()).isEqualTo(MoimMemberState.DECLINE);
        assertThat(moimMember.getMoim().getCurMemberCount()).isEqualTo(0);
    }

    @Test
    void judgeJoinThrowErrorWithUnexpectedInput() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.initMoimAndRuleJoin();
        MoimMember moimMember = TestUtils.createNormalMemberMoimLinker(member, moim);
        MoimMemberStateAction input = MoimMemberStateAction.IBF;

        // when + then
        assertThatThrownBy(() -> moimMember.judgeJoin(input))
                .isInstanceOf(IllegalArgumentException.class);
    }
}