package com.peoplein.moiming.service;

import com.peoplein.moiming.BaseTest;
import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MemberMoimLinker;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimMemberStateAction;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.rules.RuleJoin;
import com.peoplein.moiming.model.dto.domain.MyMoimLinkerDto;
import com.peoplein.moiming.model.dto.request.MoimJoinRequestDto;
import com.peoplein.moiming.model.dto.request.MoimMemberActionRequestDto;
import com.peoplein.moiming.repository.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class MoimMemberIntegrationServiceTest extends BaseTest {


    @Autowired
    MoimMemberService moimMemberService;
    @Autowired
    MoimRepository moimRepository;
    @Autowired
    MemberMoimLinkerRepository memberMoimLinkerRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MemberRoleLinkerRepository memberRoleLinkerRepository;
    @Autowired
    RoleRepository roleRepository;

    @Autowired
    EntityManager em;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void init() {
        TestUtils.truncateAllTable(jdbcTemplate);
    }

    @Test
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
        MemberMoimLinker memberMoimLinker = moimMemberService.requestJoin(requestDto, member);

        // then
        assertThat(memberMoimLinker.getMoimRoleType()).isEqualTo(MoimRoleType.NORMAL);
        assertThat(memberMoimLinker.getMemberState()).isEqualTo(MoimMemberState.ACTIVE);
    }

    @Test
    @DisplayName("requestJoin")
    void requestJoinTestSuccess() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();
        RuleJoin ruleJoin = new RuleJoin(TestUtils.birthMaxForBigRange, TestUtils.birthMinForBigRange, TestUtils.memberGenderAny, TestUtils.moimCountBig, true, true, moim, member.getUid(), false, false);

        moimRepository.save(moim);
        memberRepository.save(member);
        member.getRoles().forEach(memberRoleLinker -> roleRepository.save(memberRoleLinker.getRole()));
        flushAndClearEM();

        MoimJoinRequestDto requestDto = new MoimJoinRequestDto(moim.getId());

        // when
        MemberMoimLinker memberMoimLinker = moimMemberService.requestJoin(requestDto, member);

        // then
        Moim findMoim = moimRepository.findById(moim.getId());

        assertThat(memberMoimLinker.getMoimRoleType()).isEqualTo(MoimRoleType.NORMAL);
        assertThat(memberMoimLinker.getMemberState()).isEqualTo(MoimMemberState.ACTIVE);
        assertThat(findMoim.getCurMemberCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("성공 @ requestJoin() - IBF 재가입 시도")
    void requestJoinTestSuccessCase2() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();
        MemberMoimLinker memberMoimLinker = MemberMoimLinker.memberJoinMoim(member, moim, MoimRoleType.NORMAL, MoimMemberState.IBF);
        memberMoimLinker.setBanRejoin(true);
        RuleJoin ruleJoin = new RuleJoin(TestUtils.birthMaxForBigRange, TestUtils.birthMinForBigRange, TestUtils.memberGenderAny, TestUtils.moimCountBig, true, true, moim, member.getUid(), false, false);

        moimRepository.save(moim);
        memberRepository.save(member);
        member.getRoles().forEach(memberRoleLinker -> roleRepository.save(memberRoleLinker.getRole()));
        flushAndClearEM();

        MoimJoinRequestDto requestDto = new MoimJoinRequestDto(moim.getId());

        // when
        MemberMoimLinker result = moimMemberService.requestJoin(requestDto, member);

        // then
        List<MemberMoimLinker> findMoimLinker = memberMoimLinkerRepository.findByMemberId(member.getId());
        Moim findMoim = moimRepository.findById(moim.getId());

        assertThat(result.getMemberState()).isEqualTo(MoimMemberState.WAIT_BY_BAN);
        assertThat(findMoimLinker.size()).isEqualTo(1);
        assertThat(findMoim.getCurMemberCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("성공 @ requestJoin() - IBF 재가입 시도")
    void requestJoinTestSuccessCase3() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();
        MemberMoimLinker memberMoimLinker = MemberMoimLinker.memberJoinMoim(member, moim, MoimRoleType.NORMAL, MoimMemberState.IBF);
        memberMoimLinker.setBanRejoin(false);
        RuleJoin ruleJoin = new RuleJoin(TestUtils.birthMaxForBigRange, TestUtils.birthMinForBigRange, TestUtils.memberGenderAny, TestUtils.moimCountBig, true, true, moim, member.getUid(), false, false);

        moimRepository.save(moim);
        memberRepository.save(member);
        member.getRoles().forEach(memberRoleLinker -> roleRepository.save(memberRoleLinker.getRole()));
        flushAndClearEM();

        MoimJoinRequestDto requestDto = new MoimJoinRequestDto(moim.getId());

        // when
        MemberMoimLinker result = moimMemberService.requestJoin(requestDto, member);

        // then
        Moim findMoim = moimRepository.findById(moim.getId());

        assertThat(result).isNull();
        assertThat(findMoim.getCurMemberCount()).isEqualTo(0);
    }

    @Test
    void decideJoinTestSuccess() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();
        MemberMoimLinker memberMoimLinker = MemberMoimLinker.memberJoinMoim(member, moim, MoimRoleType.NORMAL, MoimMemberState.IBF);
        memberMoimLinker.setBanRejoin(true);
        RuleJoin ruleJoin = new RuleJoin(TestUtils.birthMaxForBigRange, TestUtils.birthMinForBigRange, TestUtils.memberGenderAny, TestUtils.moimCountBig, true, true, moim, member.getUid(), false, false);

        moimRepository.save(moim);
        memberRepository.save(member);
        member.getRoles().forEach(memberRoleLinker -> roleRepository.save(memberRoleLinker.getRole()));
        flushAndClearEM();

        MoimMemberActionRequestDto actionRequestDto = TestUtils.createActionRequestDto(moim.getId(), member.getId(), MoimMemberStateAction.PERMIT);

        // when
        MemberMoimLinker result = moimMemberService.decideJoin(actionRequestDto);

        // then
        assertThat(result.getMemberState()).isEqualTo(MoimMemberState.ACTIVE);
        assertThat(result.getMoim().getCurMemberCount()).isEqualTo(1);
    }


    void flushAndClearEM() {
        em.flush();
        em.clear();
    }

}