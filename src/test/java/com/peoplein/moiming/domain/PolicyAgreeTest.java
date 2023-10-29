package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.PolicyType;
import com.peoplein.moiming.exception.MoimingApiException;
import org.junit.jupiter.api.Test;

import static com.peoplein.moiming.domain.enums.PolicyType.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

public class PolicyAgreeTest {

    // createPolicyAgree
    // 서비스 약관 동의 생성
    @Test
    void createPolicyAgree_shouldCreatePolicyAgree_whenServiceAgreePassed() {

        // given
        Member member = mock(Member.class);

        // when
        PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(member, SERVICE, true);

        // then
        assertThat(policyAgree.getPolicyType()).isEqualTo(SERVICE);
        assertThat(policyAgree.isHasAgreed()).isEqualTo(true);

    }

    // 서비스 약관 비동의 생성
    @Test
    void createPolicyAgree_shouldThrowException_whenServiceDisagreePassed_byMoimingApiException() {

        // given
        Member member = mock(Member.class);

        // when
        // then
        assertThatThrownBy(() -> PolicyAgree.createPolicyAgree(member, SERVICE, false));

    }

    // 개인정보 약관 동의
    @Test
    void createPolicyAgree_shouldCreatePolicyAgree_whenPrivacyAgreePassed() {

        // given
        Member member = mock(Member.class);

        // when
        PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(member, PRIVACY, true);

        // then
        assertThat(policyAgree.getPolicyType()).isEqualTo(PRIVACY);
        assertThat(policyAgree.isHasAgreed()).isEqualTo(true);

    }


    // 개인정보 약관 비동의
    @Test
    void createPolicyAgree_shouldThrowException_whenPrivacyDisagreePassed_byMoimingApiException() {

        // given
        Member member = mock(Member.class);

        // when
        // then
        assertThatThrownBy(() -> PolicyAgree.createPolicyAgree(member, PRIVACY, false));

    }


    // 나이 약관 동의
    @Test
    void createPolicyAgree_shouldCreatePolicyAgree_whenAgeAgreePassed() {

        // given
        Member member = mock(Member.class);

        // when
        PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(member, AGE, true);

        // then
        assertThat(policyAgree.getPolicyType()).isEqualTo(AGE);
        assertThat(policyAgree.isHasAgreed()).isEqualTo(true);

    }


    // 나이 약관 비동의
    @Test
    void createPolicyAgree_shouldThrowException_whenAgeDisagreePassed_byMoimingApiException() {

        // given
        Member member = mock(Member.class);

        // when
        // then
        assertThatThrownBy(() -> PolicyAgree.createPolicyAgree(member, AGE, false));

    }


    // 마케팅1 약관 동의 비동의
    @Test
    void createPolicyAgree_shouldCreatePolicyAgree_whenMarketingSmsAgreePassed() {

        // given
        Member member = mock(Member.class);

        // when
        PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(member, MARKETING_SMS, true);

        // then
        assertThat(policyAgree.getPolicyType()).isEqualTo(MARKETING_SMS);
        assertThat(policyAgree.isHasAgreed()).isEqualTo(true);

    }

    @Test
    void createPolicyAgree_shouldCreatePolicyAgree_whenMarketingSmsDisagreePassed() {

        // given
        Member member = mock(Member.class);

        // when
        PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(member, MARKETING_SMS, false);

        // then
        assertThat(policyAgree.getPolicyType()).isEqualTo(MARKETING_SMS);
        assertThat(policyAgree.isHasAgreed()).isEqualTo(false);

    }


    // 마케팅2 약관 동의 비동의
    @Test
    void createPolicyAgree_shouldCreatePolicyAgree_whenMarketingEmailAgreePassed() {

        // given
        Member member = mock(Member.class);

        // when
        PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(member, MARKETING_EMAIL, true);

        // then
        assertThat(policyAgree.getPolicyType()).isEqualTo(MARKETING_EMAIL);
        assertThat(policyAgree.isHasAgreed()).isEqualTo(true);

    }

    @Test
    void createPolicyAgree_shouldCreatePolicyAgree_whenMarketingEmailDisagreePassed() {

        // given
        Member member = mock(Member.class);

        // when
        PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(member, MARKETING_EMAIL, false);

        // then
        assertThat(policyAgree.getPolicyType()).isEqualTo(MARKETING_EMAIL);
        assertThat(policyAgree.isHasAgreed()).isEqualTo(false);

    }


    // changeHasAgreed - 정말 Attribute 이 change 되는지 확인 필요 - 실객체로 진행
    // 서비스 약관 동의 변경
    @Test
    void changeHasAgreed_shouldThrowException_whenServiceChangeToDisagree_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(member, SERVICE, true);

        // when
        // then
        assertThatThrownBy(() -> policyAgree.changeHasAgreed(eq(false), any())).isInstanceOf(MoimingApiException.class);

    }


    // 개인정보 약관 동의 변경
    @Test
    void changeHasAgreed_shouldThrowException_whenPrivacyChangeToDisagree_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(member, PRIVACY, true);

        // when
        // then
        assertThatThrownBy(() -> policyAgree.changeHasAgreed(eq(false), any())).isInstanceOf(MoimingApiException.class);

    }


    // 나이 약관 동의 변경
    @Test
    void changeHasAgreed_shouldThrowException_whenAgeChangeToDisagree_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(member, AGE, true);

        // when
        // then
        assertThatThrownBy(() -> policyAgree.changeHasAgreed(eq(false), any())).isInstanceOf(MoimingApiException.class);

    }


    // 마케팅 약관 비동의 -> 동의
    @Test
    void changeHasAgreed_shouldChangePolicyAgree_whenMarketingSmsChangeToAgree() {

        // given
        Member member = mock(Member.class);
        PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(member, MARKETING_SMS, false);

        // given - stub
        when(member.getId()).thenReturn(1L);

        // when
        policyAgree.changeHasAgreed(true, 1L);

        // then
        assertThat(policyAgree.isHasAgreed()).isEqualTo(true);

    }


    // 다른 사람이 자신의 약관을 변경하려 하는 경우
    @Test
    void changeHasAgreed_shouldThrowException_whenTrialOfOtherMember_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(member, MARKETING_SMS, false);

        // given - stub
        when(member.getId()).thenReturn(1L);

        // when
        // then
        assertThatThrownBy(() -> policyAgree.changeHasAgreed(true, 2L)).isInstanceOf(MoimingApiException.class);

    }


    // 마케팅 약관 동의 -> 비동의
    @Test
    void changeHasAgreed_shouldChangePolicyAgree_whenMarketingSmsChangeToDisagree() {

        // given
        Member member = mock(Member.class);
        PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(member, MARKETING_SMS, true);

        // given - stub
        when(member.getId()).thenReturn(1L);

        // when
        policyAgree.changeHasAgreed(false, 1L);

        // then
        assertThat(policyAgree.isHasAgreed()).isEqualTo(false);

    }


    // 마케팅 약관 동의 -> 동의 (같은 상황)
    @Test
    void changeHasAgreed_shouldThrowException_whenMarketingSmsChangeToSameType_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(member, MARKETING_SMS, true);

        // given - stub
        when(member.getId()).thenReturn(1L);

        // when
        // then
        assertThatThrownBy(() -> policyAgree.changeHasAgreed(true, 1L)).isInstanceOf(MoimingApiException.class);


    }


    // 마케팅2 약관 동의 > 비동의 변경 + 반대
    @Test
    void changeHasAgreed_shouldChangePolicyAgree_whenMarketingEmailChangeToAgree() {

        // given
        Member member = mock(Member.class);
        PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(member, MARKETING_EMAIL, false);

        // given - stub
        when(member.getId()).thenReturn(1L);

        // when
        policyAgree.changeHasAgreed(true, 1L);

        // then
        assertThat(policyAgree.isHasAgreed()).isEqualTo(true);

    }

    @Test
    void changeHasAgreed_shouldChangePolicyAgree_whenMarketingEmailChangeToDisagree() {

        // given
        Member member = mock(Member.class);
        PolicyAgree policyAgree = PolicyAgree.createPolicyAgree(member, MARKETING_SMS, true);

        // given - stub
        when(member.getId()).thenReturn(1L);

        // when
        policyAgree.changeHasAgreed(false, 1L);

        // then
        assertThat(policyAgree.isHasAgreed()).isEqualTo(false);

    }


}
