package com.peoplein.moiming.service;

import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.rules.RuleJoin;
import com.peoplein.moiming.model.dto.domain.MyMoimLinkerDto;
import com.peoplein.moiming.model.dto.request.MoimJoinRequestDto;
import com.peoplein.moiming.repository.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
public class MoimMemberIntegrationServiceTest {


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


    @Test
    @DisplayName("requestJoin")
    void requestJoinTestSuccess() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();
        RuleJoin ruleJoin = new RuleJoin(TestUtils.birthMaxForBigRange, TestUtils.birthMinForBigRange, TestUtils.memberGenderAny, TestUtils.moimCountBig, true, true, moim, member.getUid());

        moimRepository.save(moim);
        memberRepository.save(member);
        member.getRoles().forEach(memberRoleLinker -> roleRepository.save(memberRoleLinker.getRole()));
        flushAndClearEM();

        MoimJoinRequestDto requestDto = new MoimJoinRequestDto(moim.getId());

        // when
        MyMoimLinkerDto myMoimLinkerDto = moimMemberService.requestJoin(requestDto, member);

        // then
        assertThat(myMoimLinkerDto.getMoimRoleType()).isEqualTo(MoimRoleType.NORMAL);
        assertThat(myMoimLinkerDto.getMemberState()).isEqualTo(MoimMemberState.ACTIVE);
    }

    @Test
    @DisplayName("성공 @ requestJoin() - 재가입 시도")
    void 재가입_가입요청() {


    }

    @Test
    @DisplayName("실패 @ requestJoin() - 재가입 금지로 인한 실패")
    void 재가입_금지_가입요청() {

    }


    void flushAndClearEM() {
        em.flush();
        em.clear();
    }

}