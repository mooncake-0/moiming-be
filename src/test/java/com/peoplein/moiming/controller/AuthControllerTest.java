package com.peoplein.moiming.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.SmsVerification;
import com.peoplein.moiming.domain.enums.VerificationType;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.enums.PolicyType;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.RoleRepository;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.domain.enums.PolicyType.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.AuthReqDto.*;
import static com.peoplein.moiming.model.dto.request.AuthReqDto.AuthSignUpReqDto.*;
import static com.peoplein.moiming.security.exception.AuthExceptionValue.*;
import static com.peoplein.moiming.support.TestParams.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/*
 - reissueToken - 잘못된 토큰 (이메일 추출은 가능) 일 시 Fail & RT 삭제는 Service 에서 확인
*/
@AutoConfigureMockMvc
@Transactional // Test Data 저장을 위함
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AuthControllerTest extends TestObjectCreator {

    private final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    private String savedToken;


    @BeforeEach
    void dataSu() {

        Role testRole = makeTestRole(RoleType.USER);
        testMember = makeTestMember(memberPhone, nickname, ci, testRole);
        savedToken = createTestJwtToken(testMember, 2000);
        testMember.changeRefreshToken(savedToken); // reissue 단 test 를 위해 Test token 을 주입해둔다

        roleRepository.save(testRole);
        memberRepository.save(testMember);

        em.flush();
        em.clear();

    }

    private List<PolicyAgreeDto> provideNormalPolicyDtos() {
        boolean[] hasAgreeds = {true, true, true, true, false, true};
        PolicyType[] policyTypes = {SERVICE, PRIVACY, AGE, MARKETING_AGREE, MARKETING_SMS, MARKETING_PUSH};
        return makePolicyReqDtoList(hasAgreeds, policyTypes);
    }


    @Test
    void signUp_shouldReturnMemberDtoAnd200_whenSuccessful() throws Exception {

        // given
        SmsVerification smsVerification = makeTestSmsVerification(true, null, memberPhone2, VerificationType.SIGN_UP);
        em.persist(smsVerification);
        em.flush();
        em.clear();

        AuthSignUpReqDto reqDto = new AuthSignUpReqDto(smsVerification.getId(), memberPhone2, password, fcmToken, provideNormalPolicyDtos());
        String requestString = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_SIGN_UP)
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isCreated());
        resultActions.andExpect(jsonPath("$.data.hasMemberInfo").value(false));
        resultActions.andExpect(jsonPath("$.data.memberPhone").value(memberPhone2));
        resultActions.andExpect(jsonPath("$.data.fcmToken").value(fcmToken));
        resultActions.andExpect(jsonPath("$.data.createdAt").exists());
        resultActions.andExpect(jsonPath("$.data.tokenInfo").exists());

        // then - db verify
        Optional<Member> findMember = memberRepository.findByMemberPhone(memberPhone2);
        assertTrue(findMember.isPresent());
        resultActions.andExpect(jsonPath("$.data.memberId").value(findMember.get().getId()));
        resultActions.andExpect(jsonPath("$.data.nickname").value(findMember.get().getNickname()));

    }


    // 중복 핸드폰 유저 (중복된 ID 사용)
    @Test
    void signUp_shouldReturn409_whenPhoneDuplicates_byMoimingApiException() throws Exception {

        // given
        String unavailablePhone = memberPhone;
        SmsVerification smsVerification = makeTestSmsVerification(true, null, unavailablePhone, VerificationType.SIGN_UP);
        em.persist(smsVerification);
        em.flush();
        em.clear();

        AuthSignUpReqDto reqDto = new AuthSignUpReqDto(smsVerification.getId(), unavailablePhone, password, fcmToken, provideNormalPolicyDtos());
        String requestString = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_SIGN_UP).content(requestString).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isConflict());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_SIGN_UP_PHONE_DUPLICATE.getErrCode()));
    }


//    // 중복 CI 값 유저 - 불가능
//    @Test
//    void signIn_shouldReturn409_whenCiDuplicates_byMoimingApiException() throws Exception {
//
//        // given
//        String unavailableCi = ci;
//        TestMemberRequestDto reqDto = makeMemberReqDto(memberEmail2, memberName2, memberPhone2, unavailableCi, provideNormalPolicyDtos());
//        String requestString = om.writeValueAsString(reqDto);
//
//        // when
//        ResultActions resultActions = mvc.perform(post(PATH_AUTH_SIGN_UP).content(requestString).contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        resultActions.andExpect(status().isConflict());
//        resultActions.andExpect(jsonPath("$.code").value(AUTH_SIGN_IN_DUPLICATE_COLUMN.getErrCode()));
//    }


    // VALIDATION 에서 걸릴 때 (없는 값 하나 대표적으로)
    @Test
    void signUp_shouldReturn400_whenRequestDtoValidationFails_byMoimingValidationException() throws Exception {

        // given
        AuthSignUpReqDto reqDto = new AuthSignUpReqDto(null, null, null, null, null);
        reqDto.setFcmToken(""); // 빈값 치환
        String requestString = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_SIGN_UP).content(requestString).contentType(MediaType.APPLICATION_JSON));
        String errResponse = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(5))); // SMS 없음, FCM 없음

    }


    // TODO :: 비밀번호 형식 DTO 에 제대로 명시 후 Test
    // 비밀번호 형식 오류 TODO : 비밀번호 조건 제대로 설정 후 재 Test 필요
    @Test
    void signUp_shouldReturn400_whenPasswordConditionFails_byMoimingValidationException() throws Exception {

        // given
        SmsVerification smsVerification = makeTestSmsVerification(true, null, memberPhone2, VerificationType.SIGN_UP);
        em.persist(smsVerification);
        em.flush();
        em.clear();

        AuthSignUpReqDto reqDto = new AuthSignUpReqDto(smsVerification.getId(), memberPhone2, password, fcmToken, provideNormalPolicyDtos());
        reqDto.setPassword("123");
        String requestString = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_SIGN_UP).content(requestString).contentType(MediaType.APPLICATION_JSON));
        String errResponse = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("errResponse = " + errResponse);

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1))); // 비밀번호 형식 오류

    }


    // Policy 필드가 없음
    @Test
    void signUp_shouldReturn400_whenPolicyListNull_byMoimingValidationException() throws Exception {

        // given
        SmsVerification smsVerification = makeTestSmsVerification(true, null, memberPhone2, VerificationType.SIGN_UP);
        em.persist(smsVerification);
        em.flush();
        em.clear();

        AuthSignUpReqDto reqDto = new AuthSignUpReqDto(smsVerification.getId(), memberPhone2, password, fcmToken, null);
        String requestString = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_SIGN_UP)
                .content(requestString).contentType(MediaType.APPLICATION_JSON));
        String errResponse = resultActions.andReturn().getResponse().getContentAsString();

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1))); // Policy 필드가 없음


    }


    // Policy 정보가 없음
    @Test
    void signUp_shouldReturn400_whenPolicyListEmpty_byMoimingValidationException() throws Exception {

        // given
        SmsVerification smsVerification = makeTestSmsVerification(true, null, memberPhone2, VerificationType.SIGN_UP);
        em.persist(smsVerification);
        em.flush();
        em.clear();

        AuthSignUpReqDto reqDto = new AuthSignUpReqDto(smsVerification.getId(), memberPhone2, password, fcmToken, new ArrayList<>());
        String requestString = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_SIGN_UP)
                .content(requestString).contentType(MediaType.APPLICATION_JSON));
        String errResponse = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("errResponse = " + errResponse);

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1))); // Policy 는 5개의 필드가 필요하다

    }


    // 필수 Policy Agreement 가 false 로 들어옴
    @Test
    void signUp_shouldReturn400_whenPolicyListInvalid_byMoimingApiException() throws Exception {

        // given
        SmsVerification smsVerification = makeTestSmsVerification(true, null, memberPhone2, VerificationType.SIGN_UP);
        em.persist(smsVerification);
        em.flush();
        em.clear();

        boolean[] isAgreeds = {true, true, false, true, false, true};
        PolicyType[] policyTypes = {SERVICE, PRIVACY, AGE, MARKETING_AGREE, MARKETING_SMS, MARKETING_PUSH};
        AuthSignUpReqDto reqDto = new AuthSignUpReqDto(smsVerification.getId(), memberPhone2, password, fcmToken, makePolicyReqDtoList(isAgreeds, policyTypes));
        String requestString = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_SIGN_UP)
                .content(requestString).contentType(MediaType.APPLICATION_JSON));
        String errResponse = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("errResponse = " + errResponse);

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(MEMBER_POLICY_ESSENTIAL.getErrCode()));

    }


    // Policy 정보가 모자람
    @Test
    void signUp_shouldReturn400_whenPolicyListLack_byMoimingValidationException() throws Exception {

        // given
        SmsVerification smsVerification = makeTestSmsVerification(true, null, memberPhone2, VerificationType.SIGN_UP);
        em.persist(smsVerification);
        em.flush();
        em.clear();

        boolean[] isAgreeds = {true, true, true, true, true, false};
        PolicyType[] policyTypes = {SERVICE, PRIVACY, AGE, MARKETING_AGREE, MARKETING_SMS};
        AuthSignUpReqDto reqDto = new AuthSignUpReqDto(smsVerification.getId(), memberPhone2, password, fcmToken, makePolicyReqDtoList(isAgreeds, policyTypes));
        String requestString = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_SIGN_UP)
                .content(requestString).contentType(MediaType.APPLICATION_JSON));
        String errResponse = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("errResponse = " + errResponse);

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(1))); // SMS 없음, Policy 는 5개의 필드가 필요하다 (4개만 들어옴)

    }


    // SMS Verification 폰번호 불일치
    @Test
    void signUp_shouldReturn422_whenSmsVerificationPhoneNumbAndRequestNumNotMatch_byMoimingAuthApiException() throws Exception {

        // given
        SmsVerification smsVerification = makeTestSmsVerification(true, null, memberPhone2, VerificationType.SIGN_UP);
        em.persist(smsVerification);
        em.flush();
        em.clear();

        AuthSignUpReqDto reqDto = new AuthSignUpReqDto(smsVerification.getId(), memberPhone3, password, fcmToken, provideNormalPolicyDtos());
        String requestString = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_SIGN_UP)
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isUnprocessableEntity());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_SMS_REQUEST_INFO_NOT_MATCH_REQUESTING_INFO.getErrCode()));

    }


    // SMS Verification 인증 안됨
    @Test
    void signUp_shouldReturn401_whenSmsVerificationNotVerified_byMoimingAuthApiException() throws Exception {

        // given
        SmsVerification smsVerification = makeTestSmsVerification(false, null, memberPhone2, VerificationType.SIGN_UP);
        em.persist(smsVerification);
        em.flush();
        em.clear();

        AuthSignUpReqDto reqDto = new AuthSignUpReqDto(smsVerification.getId(), memberPhone2, password, fcmToken, provideNormalPolicyDtos());
        String requestString = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_SIGN_UP)
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isUnauthorized());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_SMS_NOT_VERIFIED.getErrCode()));

    }


    // SMS Verification NOT FOUND
    @Test
    void signUp_shouldReturn404_whenSmsVerificationNotFound_byMoimingAuthApiException() throws Exception {

        // given
        AuthSignUpReqDto reqDto = new AuthSignUpReqDto(1L, memberPhone2, password, fcmToken, provideNormalPolicyDtos());
        String requestString = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_SIGN_UP)
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_SMS_VERIFICATION_NOT_FOUND.getErrCode()));

    }


    // SMS Verification FIND_PW
    @Test
    void signUp_shouldReturn401_whenSmsVerificationDiffType_byMoimingAuthApiException() throws Exception {

        // given
        SmsVerification smsVerification = makeTestSmsVerification(true, null, memberPhone2, VerificationType.FIND_PW);
        em.persist(smsVerification);
        em.flush();
        em.clear();

        AuthSignUpReqDto reqDto = new AuthSignUpReqDto(smsVerification.getId(), memberPhone2, password, fcmToken, provideNormalPolicyDtos());
        String requestString = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_SIGN_UP)
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isConflict());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_SMS_VERIFICATION_TYPE_NOT_MATCH.getErrCode()));

    }


    // 토큰 재발급
    @Test
    void reissueToken_shouldReturnNewTokenAnd200_whenSuccessful() throws Exception {

        // given
        AuthTokenReqDto reqDto = new AuthTokenReqDto();
        reqDto.setGrantType("REFRESH_TOKEN");
        reqDto.setToken(savedToken); // 기존 토큰을 가지고 간다
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_REISSUE_TOKEN)
                .content(requestBody).contentType(MediaType.APPLICATION_JSON));


        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data.accessToken").exists());
        resultActions.andExpect(jsonPath("$.data.refreshToken").exists()); // Refresh Token 재발급

    }


    /*
     통합테스트 이므로, 추후 검증까지 진행해본다
     */
    @Test
    void reissueToken_shouldSaveNewTokenToRequestingMemberAndReturn200_whenSuccessful() throws Exception {

        // given
        AuthTokenReqDto reqDto = new AuthTokenReqDto();
        reqDto.setGrantType("REFRESH_TOKEN");
        reqDto.setToken(savedToken); // 기존 토큰을 가지고 간다
        String requestBody = om.writeValueAsString(reqDto);


        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_REISSUE_TOKEN).content(requestBody).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());

        // then - member 조회 후 발급받은 Refresh Token 이 전달 받은것과 같은지 확인한다
        Member findMemberPs = memberRepository.findById(testMember.getId()).orElseThrow(Exception::new);
        resultActions.andExpect(jsonPath("$.data.refreshToken").value(findMemberPs.getRefreshToken()));

    }


    // RT Expire 일 경우
    @Test
    void reissueToken_shouldReturn401_whenRefreshTokenExpired_byTokenExpiredException() throws Exception {

        // given
        AuthTokenReqDto reqDto = new AuthTokenReqDto();
        reqDto.setGrantType("REFRESH_TOKEN");
        reqDto.setToken(savedToken); // 기존 토큰을 가지고 간다

        String requestData = om.writeValueAsString(reqDto);
        Thread.sleep(3000); // 토큰을 만료시키자 (가끔 만료 안돼서 오류나서, 충분한 시간을 줌)


        // when
        ResultActions resultActions = mvc.perform(post(PATH_AUTH_REISSUE_TOKEN)
                .content(requestData).contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isUnauthorized());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_REFRESH_TOKEN_EXPIRED.getErrCode()));
    }


    // Test 가 어려워서 직접 API 날려봐야 할 것들
    // 일단 성공 Case 들 날려봐야 함
    // SMS Controller 로 문자 날라가서 생성되는 것 - 테스트 필요
    // 3분 지나서 만료되는거 확인 - 테스트 필요


    // 비밀번호 재설정 인증 요청 - SMS 생성후 테스트
    // 성공
    @Test
    void confirmResetPassword_shouldReturn200_whenRightInfoPassed() throws Exception {

        // given
        SmsVerification smsVerification = SmsVerification.createSmsVerification(testMember.getId(), memberPhone, VerificationType.FIND_PW);
        String verificationNumber = smsVerification.getVerificationNumber();
        em.persist(smsVerification);
        em.clear();
        em.flush();

        AuthResetPwConfirmReqDto reqDto = new AuthResetPwConfirmReqDto(smsVerification.getId(), memberPhone, verificationNumber);
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_AUTH_RESET_PW_CONFIRM)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.data").isEmpty());

    }


    // 실패 - Validation 오류
    @Test
    void confirmResetPassword_shouldReturn400_whenValidiationFails_byMoimingValidationException() throws Exception {

        // given
        AuthResetPwConfirmReqDto reqDto = new AuthResetPwConfirmReqDto(null, "", "");
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_AUTH_RESET_PW_CONFIRM)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(3)));

    }


    // 실패 - SMS 못찾음 오류
    @Test
    void confirmResetPassword_shouldReturn404_whenSmsNotFound_byMoimingAuthApiException() throws Exception {

        // given
        SmsVerification smsVerification = SmsVerification.createSmsVerification(testMember.getId(), memberPhone, VerificationType.FIND_PW);
        String verificationNumber = smsVerification.getVerificationNumber();
        em.persist(smsVerification);
        em.clear();
        em.flush();

        AuthResetPwConfirmReqDto reqDto = new AuthResetPwConfirmReqDto(1234L, memberPhone, verificationNumber);
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_AUTH_RESET_PW_CONFIRM)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_SMS_VERIFICATION_NOT_FOUND.getErrCode()));

    }


    // 실패 - Verification 숫자 오류
    @Test
    void confirmResetPassword_shouldReturn422_whenSmsNumberNotMatch_byMoimingAuthApiException() throws Exception {

        // given
        SmsVerification smsVerification = SmsVerification.createSmsVerification(testMember.getId(), memberPhone, VerificationType.FIND_PW);
        String verificationNumber = smsVerification.getVerificationNumber();
        em.persist(smsVerification);
        em.clear();
        em.flush();

        AuthResetPwConfirmReqDto reqDto = new AuthResetPwConfirmReqDto(smsVerification.getId(), memberPhone, "000000");
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_AUTH_RESET_PW_CONFIRM)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isUnprocessableEntity());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_SMS_VERIFICATION_NUMBER_NOT_MATCH.getErrCode()));

    }


    // 실패 - Phone 번호가 다른 오류
    @Test
    void confirmResetPassword_shouldReturn422_whenSmsPhoneNumberNotMatchRequestNumber_byMoimingAuthApiException() throws Exception {

        // given
        SmsVerification smsVerification = SmsVerification.createSmsVerification(testMember.getId(), memberPhone, VerificationType.FIND_PW);
        String verificationNumber = smsVerification.getVerificationNumber();
        em.persist(smsVerification);
        em.clear();
        em.flush();

        AuthResetPwConfirmReqDto reqDto = new AuthResetPwConfirmReqDto(smsVerification.getId(), memberPhone2, verificationNumber);
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_AUTH_RESET_PW_CONFIRM)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isUnprocessableEntity());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_SMS_REQUEST_INFO_NOT_MATCH_REQUESTING_INFO.getErrCode()));

    }


    // 비밀번호 재설정 요청 - SMS 생성후 테스트
    // 성공 - 비밀번호 확인
    @Test
    void resetPassword_shouldReturn200AndChangeMemberPw_whenRightInfoPassed() throws Exception {

        // given
        SmsVerification smsVerification = SmsVerification.createSmsVerification(testMember.getId(), memberPhone, VerificationType.FIND_PW);
        String verificationNumber = smsVerification.getVerificationNumber();
        em.persist(smsVerification);
        String newPassword = "NEW_PASSWORD";
        AuthResetPwReqDto reqDto = new AuthResetPwReqDto(smsVerification.getId(), newPassword);
        String requestBody = om.writeValueAsString(reqDto);

        // given - SMS 가 인증되어야 한다
        smsVerification.confirmVerification(VerificationType.FIND_PW, verificationNumber);
        em.flush();
        em.clear();

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_AUTH_RESET_PW)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));


        // then
        resultActions.andExpect(status().isOk());

        // then - db verify
        em.flush();
        em.clear();

        Member member = em.find(Member.class, testMember.getId());
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean matches = encoder.matches(newPassword, member.getPassword());
        assertTrue(matches);

    }


    // 실패 - SMS 못찾음 오류
    @Test
    void resetPassword_shouldReturn404_whenSmsVerificationNotFound_byMoimingAuthApiException() throws Exception {

        // given
        SmsVerification smsVerification = SmsVerification.createSmsVerification(testMember.getId(), memberPhone, VerificationType.FIND_PW);
        String verificationNumber = smsVerification.getVerificationNumber();
        em.persist(smsVerification);
        String newPassword = "NEW_PASSWORD";
        AuthResetPwReqDto reqDto = new AuthResetPwReqDto(1234L, newPassword);
        String requestBody = om.writeValueAsString(reqDto);

        // given - SMS 가 인증되어야 한다
        smsVerification.confirmVerification(VerificationType.FIND_PW, verificationNumber);
        em.flush();
        em.clear();

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_AUTH_RESET_PW)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));


        // then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_SMS_VERIFICATION_NOT_FOUND.getErrCode()));

    }


    // 실패 - Validation 오류
    @Test
    void resetPassword_shouldReturn400_whenValidationFails_byMoimingValidationException() throws Exception {

        // given
        SmsVerification smsVerification = SmsVerification.createSmsVerification(testMember.getId(), memberPhone, VerificationType.FIND_PW);
        String verificationNumber = smsVerification.getVerificationNumber();
        em.persist(smsVerification);
        String newPassword = "NEW_PASSWORD";
        AuthResetPwReqDto reqDto = new AuthResetPwReqDto(null, "");
        String requestBody = om.writeValueAsString(reqDto);

        // given - SMS 가 인증되어야 한다
        smsVerification.confirmVerification(VerificationType.FIND_PW, verificationNumber);
        em.flush();
        em.clear();

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_AUTH_RESET_PW)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));


        // then
        resultActions.andExpect(status().isBadRequest());
        resultActions.andExpect(jsonPath("$.code").value(COMMON_REQUEST_VALIDATION.getErrCode()));
        resultActions.andExpect(jsonPath("$.data", aMapWithSize(2)));

    }


    // 실패 - 인증되지 않은 SMS 활용 오류
    @Test
    void resetPassword_shouldReturn401_whenSmsVerificationNotVerified_byMoimingAuthApiException() throws Exception {

        // given
        SmsVerification smsVerification = SmsVerification.createSmsVerification(testMember.getId(), memberPhone, VerificationType.FIND_PW);
        String verificationNumber = smsVerification.getVerificationNumber();
        em.persist(smsVerification);
        em.flush();
        em.clear();

        String newPassword = "NEW_PASSWORD";
        AuthResetPwReqDto reqDto = new AuthResetPwReqDto(smsVerification.getId(), newPassword);
        String requestBody = om.writeValueAsString(reqDto);

        // when
        ResultActions resultActions = mvc.perform(patch(PATH_AUTH_RESET_PW)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON));


        // then
        resultActions.andExpect(status().isUnauthorized());
        resultActions.andExpect(jsonPath("$.code").value(AUTH_SMS_NOT_VERIFIED.getErrCode()));
    }


    // TODO :: 실패 - 비밀번호 형식 오류

}
