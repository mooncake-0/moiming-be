//package com.peoplein.moiming.service.integrated;
//
//
//import com.peoplein.moiming.domain.SmsVerification;
//import com.peoplein.moiming.domain.enums.VerificationType;
//import com.peoplein.moiming.domain.member.Member;
//import com.peoplein.moiming.domain.PolicyAgree;
//import com.peoplein.moiming.domain.enums.PolicyType;
//import com.peoplein.moiming.domain.enums.RoleType;
//import com.peoplein.moiming.domain.fixed.Role;
//import com.peoplein.moiming.domain.member.MemberSetting;
//import com.peoplein.moiming.exception.MoimingAuthApiException;
//import com.peoplein.moiming.model.dto.response.TokenRespDto;
//import com.peoplein.moiming.repository.PolicyAgreeRepository;
//import com.peoplein.moiming.service.AuthService;
//import com.peoplein.moiming.support.TestObjectCreator;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.StringUtils;
//
//import javax.persistence.EntityManager;
//import java.util.List;
//
//import static com.peoplein.moiming.domain.enums.PolicyType.*;
//import static com.peoplein.moiming.model.dto.request.AuthReqDto.*;
//import static com.peoplein.moiming.model.dto.request.AuthReqDto.AuthSignUpReqDto.*;
//import static com.peoplein.moiming.model.dto.response.AuthRespDto.*;
//import static com.peoplein.moiming.support.TestParams.*;
//import static org.assertj.core.api.Assertions.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//public class AuthServiceIntegratedTest extends TestObjectCreator {
//
//    @Autowired
//    private AuthService authService;
//
//    @Autowired
//    private EntityManager em;
//
//    @Autowired
//    private PolicyAgreeRepository policyAgreeRepository;
//
//
//    @Test
//    void signUp_shouldSaveMemberAndMemberInfoAndPolicy_whenRightInfoPassed() {
//
//        // given - prep
//        SmsVerification smsVerification = makeTestSmsVerification(true, null, memberPhone, VerificationType.SIGN_UP);
//        em.persist(smsVerification);
//        em.flush();
//        em.clear();
//
//        PolicyType[] policyTypes = {SERVICE, PRIVACY, AGE, MARKETING_AGREE, MARKETING_SMS, MARKETING_PUSH};
//        boolean[] hasAgreeds = {true, true, true, true, false, true};
//        List<PolicyAgreeDto> policies = makePolicyReqDtoList(hasAgreeds, policyTypes);
//
//        // given
//        AuthSignUpReqDto reqDto = new AuthSignUpReqDto(smsVerification.getId()
//                , memberPhone, password, fcmToken, policies
//        );
//
//        // when
//        AuthSignUpRespDto responseDto = authService.signUp(reqDto);
//
//
//        // then - Return Val Confirm
//        assertThat(responseDto.getMemberId()).isNotNull();
//        assertThat(responseDto.isHasMemberInfo()).isEqualTo(false);
//        assertThat(responseDto.getMemberPhone()).isEqualTo(memberPhone);
//        assertThat(responseDto.getFcmToken()).isEqualTo(fcmToken);
//        assertTrue(StringUtils.hasText(responseDto.getNickname()));
//        assertTrue(StringUtils.hasText(responseDto.getTokenInfo().getAccessToken()));
//        assertTrue(StringUtils.hasText(responseDto.getTokenInfo().getRefreshToken()));
//
//        // then - DB Confirm
//        Long id = responseDto.getMemberId();
//        Member savedMember = em.find(Member.class, id);
//        assertThat(savedMember.getFcmToken()).isEqualTo(fcmToken);
//        assertThat(savedMember.getNickname()).isEqualTo(responseDto.getNickname());
//        assertThat(savedMember.getRefreshToken()).isEqualTo(responseDto.getTokenInfo().getRefreshToken());
//
//
//        List<PolicyAgree> policyAgrees = policyAgreeRepository.findByMemberId(id);
//        for (PolicyAgree policyAgree : policyAgrees) {
//            if (policyAgree.getPolicyType().equals(SERVICE)) {
//                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
//            }
//            if (policyAgree.getPolicyType().equals(PRIVACY)) {
//                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
//            }
//            if (policyAgree.getPolicyType().equals(AGE)) {
//                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
//            }
//            if (policyAgree.getPolicyType().equals(MARKETING_AGREE)) {
//                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
//            }
//            if (policyAgree.getPolicyType().equals(MARKETING_SMS)) {
//                assertThat(policyAgree.isHasAgreed()).isEqualTo(false);
//            }
//            if (policyAgree.getPolicyType().equals(MARKETING_SMS)) {
//                assertThat(policyAgree.isHasAgreed()).isEqualTo(true);
//            }
//        }
//
//        // then - member setting confirm
//        MemberSetting memberSetting = em.find(MemberSetting.class, savedMember.getId());
//        assertNotNull(memberSetting);
//        assertThat(memberSetting.isMoimActivityNotifyAllowed()).isEqualTo(false); // 기본값
//        assertThat(memberSetting.isNoticeNotifyAllowed()).isEqualTo(false);
//    }
//
//
//    @Test
//    void reissueToken_shouldChangeMemberRefreshToken_whenRightInfoPassed() {
//
//        // given - su data - 위 signIn 함수를 쓰고 싶지만, 완전한 Test 분리를 위해 사용하지 않는다
//        Role testRole = makeTestRole(RoleType.USER);
//        Member testMember = makeTestMember(memberPhone, nickname, ci, testRole);// 저장할 Member 를 만든다.
//        String preRefreshToken = createTestJwtToken(testMember, 2000);// 해당 멤버에 Refresh Token 을 저장해준다
//        testMember.changeRefreshToken(preRefreshToken);
//        em.persist(testRole);
//        em.persist(testMember);
//        em.flush();
//        em.clear();
//
//        // given
//        AuthTokenReqDto requestDto = new AuthTokenReqDto();
//        requestDto.setGrantType("REFRESH_TOKEN");
//        requestDto.setToken(preRefreshToken);
//
//        // when
//        TokenRespDto tokenRespDto = authService.reissueToken(requestDto);
//        String reIssuedAt = tokenRespDto.getAccessToken();
//        String reIssuedRt = tokenRespDto.getRefreshToken();
//
//        // then
//        assertTrue(StringUtils.hasText(reIssuedAt));
//
//        // then - db verify
//        Member member = em.find(Member.class, testMember.getId());
//        assertThat(member.getRefreshToken()).isEqualTo(reIssuedRt);
//
//    }
//
//
//    @Test
//    void reissueToken_shouldEmptyRefreshTokenData_whenReissueFail_byMoimingAuthApiException() {
//
//        // given - su data - 위 signIn 함수를 쓰고 싶지만, 완전한 Test 분리를 위해 사용하지 않는다
//        Role testRole = makeTestRole(RoleType.USER);
//        Member testMember = makeTestMember(memberPhone, nickname, ci, testRole);// 저장할 Member 를 만든다.
//        String preRefreshToken = createTestJwtToken(testMember, 2000);// 해당 멤버에 Refresh Token 을 저장해준다
//        testMember.changeRefreshToken("WRONG_REFRESH_TOKEN");
//        em.persist(testRole);
//        em.persist(testMember);
//        em.flush();
//        em.clear();
//
//        // given
//        AuthTokenReqDto requestDto = new AuthTokenReqDto();
//        requestDto.setGrantType("REFRESH_TOKEN");
//        requestDto.setToken(preRefreshToken); // 다른 값으로 들어감
//
//        // when
//        // then
//        assertThatThrownBy(() -> authService.reissueToken(requestDto)).isInstanceOf(MoimingAuthApiException.class);
//
//        // then - db verify
//        Member member = em.find(Member.class, testMember.getId());
//        assertThat(member.getRefreshToken()).isEqualTo(""); // 비워냈다
//
//    }
//
//}
//
