package com.peoplein.moiming.service.integrated;


import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.domain.enums.PolicyType;
import com.peoplein.moiming.model.dto.request.MemberReqDto;
import com.peoplein.moiming.model.dto.response.MemberRespDto;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.PolicyAgreeRepository;
import com.peoplein.moiming.service.AuthService;
import com.peoplein.moiming.support.TestObjectCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;

import static com.peoplein.moiming.domain.enums.PolicyType.*;
import static com.peoplein.moiming.model.dto.request.MemberReqDto.*;
import static com.peoplein.moiming.model.dto.request.MemberReqDto.MemberSignInReqDto.*;
import static com.peoplein.moiming.model.dto.response.MemberRespDto.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AuthServiceIntegratedTest extends TestObjectCreator {

    @Autowired
    private AuthService authService;

    @Autowired
    private EntityManager em;

    @Autowired
    private PolicyAgreeRepository policyAgreeRepository;


    private MemberSignInReqDto makeSignInMemberReqDto() {
        PolicyType[] policyTypes = {SERVICE, PRIVACY, AGE, MARKETING_SMS, MARKETING_EMAIL};
        boolean[] hasAgreeds = {true, true, true, true, false};
        List<PolicyAgreeDto> policies = makePolicyReqDtoList(hasAgreeds, policyTypes);
        return new MemberSignInReqDto(
                memberEmail, password, memberName, memberPhone, memberGender
                , notForeigner, memberBirth, fcmToken, ci, policies
        );
    }

    @Test
    void signIn_shouldSaveMemberAndMemberInfoAndPolicy_whenRightInfoPassed() {

        // given
        MemberSignInReqDto requestDto = makeSignInMemberReqDto();

        // when
        Map<String, Object> returnValue = authService.signIn(requestDto);
        MemberSignInRespDto responseDto = (MemberSignInRespDto) returnValue.get(authService.KEY_RESPONSE_DATA);


        // then - Return Val Confirm
        assertThat(responseDto.getMemberEmail()).isEqualTo(memberEmail);
        assertThat(responseDto.getFcmToken()).isEqualTo(fcmToken);
        assertThat(responseDto.getMemberInfo().getMemberName()).isEqualTo(memberName);
        assertThat(responseDto.getMemberInfo().getMemberGender()).isEqualTo(memberGender.toString());
        assertThat(responseDto.getMemberInfo().getMemberPhone()).isEqualTo(memberPhone);
        assertThat(responseDto.getMemberInfo().getMemberBirth()).isEqualTo(memberBirth + "");
        assertTrue(StringUtils.hasText(responseDto.getNickname()));
        assertTrue(StringUtils.hasText(responseDto.getRefreshToken()));

        // then - DB Confirm
        Long id = responseDto.getId();
        Member savedMember = em.find(Member.class, id);
        assertThat(savedMember.getFcmToken()).isEqualTo(fcmToken);
        assertThat(savedMember.getCi()).isEqualTo(ci);
        assertThat(savedMember.getNickname()).isEqualTo(responseDto.getNickname());
        assertThat(savedMember.getRefreshToken()).isEqualTo(responseDto.getRefreshToken());
        assertThat(savedMember.getMemberInfo().getMemberPhone()).isEqualTo(memberPhone);


        List<PolicyAgree> policyAgrees = policyAgreeRepository.findByMemberId(id);
        for (PolicyAgree policyAgree : policyAgrees) {
            if (policyAgree.getPolicyType().equals(SERVICE)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
            if (policyAgree.getPolicyType().equals(PRIVACY)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
            if (policyAgree.getPolicyType().equals(AGE)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
            if (policyAgree.getPolicyType().equals(MARKETING_SMS)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
            }
            if (policyAgree.getPolicyType().equals(MARKETING_EMAIL)) {
                assertThat(policyAgree.isHasAgreed()).isEqualTo(false);
            }
        }
    }


    @Test
    void reissueToken_shouldChangeMemberRefreshToken_whenRightInfoPassed() {

    }
}

