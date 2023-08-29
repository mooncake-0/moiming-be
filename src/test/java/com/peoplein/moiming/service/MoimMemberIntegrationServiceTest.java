package com.peoplein.moiming.service;

import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimMemberStateAction;
import com.peoplein.moiming.domain.rules.RuleJoin;
import com.peoplein.moiming.model.dto.request_b.MoimJoinRequestDto;
import com.peoplein.moiming.model.dto.request_b.MoimMemberActionRequestDto;
import com.peoplein.moiming.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*
 Integrated Test 는 통합 테스트로 ㄱㄱ
 Repository 와 분리 필요
 - Service 단위테스트만 진행
 */
@SpringBootTest
@Transactional
public class MoimMemberIntegrationServiceTest {


    @Autowired
    MoimMemberService moimMemberService;
    @Autowired
    MoimRepository moimRepository;
    @Autowired
    MoimMemberRepository moimMemberRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MemberRoleLinkerRepository memberRoleLinkerRepository;
    @Autowired
    RoleRepository roleRepository;

    @Autowired
    EntityManager em;

//    @Test
    @DisplayName("성공 @ requestJoin() - RuleJoin이 없는 모임에 가입 요청")
    void requestJoinTestSuccessWithNoRuleJoin() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();

        moimRepository.save(moim);
        memberRepository.save(member);
        member.getRoles().forEach(memberRoleLinker -> roleRepository.save(memberRoleLinker.getRole()));
        flushAndClearEM();

        MoimJoinRequestDto requestDto = new MoimJoinRequestDto(moim.getId());

        // when
        MoimMember moimMember = moimMemberService.requestJoin(requestDto, member);

        // then
        assertThat(moimMember.getMoimMemberRoleType()).isEqualTo(MoimMemberRoleType.NORMAL);
        assertThat(moimMember.getMemberState()).isEqualTo(MoimMemberState.ACTIVE);
    }

//    @Test
    @DisplayName("requestJoin")
    void requestJoinTestSuccess() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();
        RuleJoin ruleJoin = new RuleJoin(TestUtils.birthMaxForBigRange, TestUtils.birthMinForBigRange
                , TestUtils.memberGenderAny, TestUtils.moimCountBig, true
                , true, moim, member.getId(), false
                , false);

        moimRepository.save(moim);
        memberRepository.save(member);
        member.getRoles().forEach(memberRoleLinker -> roleRepository.save(memberRoleLinker.getRole()));
        flushAndClearEM();

        MoimJoinRequestDto requestDto = new MoimJoinRequestDto(moim.getId());

        // when
        MoimMember moimMember = moimMemberService.requestJoin(requestDto, member);

        // then
        Moim findMoim = moimRepository.findById(moim.getId());

        assertThat(moimMember.getMoimMemberRoleType()).isEqualTo(MoimMemberRoleType.NORMAL);
        assertThat(moimMember.getMemberState()).isEqualTo(MoimMemberState.ACTIVE);
        assertThat(findMoim.getCurMemberCount()).isEqualTo(1);
    }

//    @Test
    @DisplayName("성공 @ requestJoin() - IBF 재가입 시도")
    void requestJoinTestSuccessCase2() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();
        MoimMember moimMember = MoimMember.memberJoinMoim(member, moim
                , MoimMemberRoleType.NORMAL, MoimMemberState.IBF);
        moimMember.setBanRejoin(true);
        RuleJoin ruleJoin = new RuleJoin(TestUtils.birthMaxForBigRange
                , TestUtils.birthMinForBigRange, TestUtils.memberGenderAny
                , TestUtils.moimCountBig, true, true
                , moim, member.getId(), false, false);

        moimRepository.save(moim);
        memberRepository.save(member);
        member.getRoles().forEach(memberRoleLinker -> roleRepository.save(memberRoleLinker.getRole()));
        flushAndClearEM();

        MoimJoinRequestDto requestDto = new MoimJoinRequestDto(moim.getId());

        // when
        MoimMember result = moimMemberService.requestJoin(requestDto, member);

        // then
        List<MoimMember> findMoimLinker = moimMemberRepository.findByMemberId(member.getId());
        Moim findMoim = moimRepository.findById(moim.getId());

        assertThat(result.getMemberState()).isEqualTo(MoimMemberState.WAIT_BY_BAN);
        assertThat(findMoimLinker.size()).isEqualTo(1);
        assertThat(findMoim.getCurMemberCount()).isEqualTo(0);
    }

//    @Test
    @DisplayName("성공 @ requestJoin() - IBF 재가입 시도")
    void requestJoinTestSuccessCase3() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();
        MoimMember moimMember = MoimMember.memberJoinMoim(member, moim, MoimMemberRoleType.NORMAL, MoimMemberState.IBF);
        moimMember.setBanRejoin(false);
        RuleJoin ruleJoin = new RuleJoin(TestUtils.birthMaxForBigRange, TestUtils.birthMinForBigRange
                , TestUtils.memberGenderAny, TestUtils.moimCountBig, true, true
                , moim, member.getId(), false, false);

        moimRepository.save(moim);
        memberRepository.save(member);
        member.getRoles().forEach(memberRoleLinker -> roleRepository.save(memberRoleLinker.getRole()));
        flushAndClearEM();

        MoimJoinRequestDto requestDto = new MoimJoinRequestDto(moim.getId());

        // when
        MoimMember result = moimMemberService.requestJoin(requestDto, member);

        // then
        Moim findMoim = moimRepository.findById(moim.getId());

        assertThat(result).isNull();
        assertThat(findMoim.getCurMemberCount()).isEqualTo(0);
    }

//    @Test
    void decideJoinTestSuccess() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();
        MoimMember moimMember = MoimMember.memberJoinMoim(member, moim, MoimMemberRoleType.NORMAL, MoimMemberState.IBF);
        moimMember.setBanRejoin(true);
        RuleJoin ruleJoin = new RuleJoin(TestUtils.birthMaxForBigRange, TestUtils.birthMinForBigRange
                , TestUtils.memberGenderAny, TestUtils.moimCountBig, true, true, moim
                , member.getId(), false, false);

        moimRepository.save(moim);
        memberRepository.save(member);
        member.getRoles().forEach(memberRoleLinker -> roleRepository.save(memberRoleLinker.getRole()));
        flushAndClearEM();

        MoimMemberActionRequestDto actionRequestDto = TestUtils.createActionRequestDto(moim.getId(), member.getId(), MoimMemberStateAction.PERMIT);

        // when
        MoimMember result = moimMemberService.decideJoin(actionRequestDto, member);

        // then
        assertThat(result.getMemberState()).isEqualTo(MoimMemberState.ACTIVE);
        assertThat(result.getMoim().getCurMemberCount()).isEqualTo(1);
    }


    void flushAndClearEM() {
        em.flush();
        em.clear();
    }

}