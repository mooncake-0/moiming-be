package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.SmsVerification;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.enums.VerificationType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.exception.MoimingAuthApiException;
import com.peoplein.moiming.exception.MoimingAuthNoRollbackException;
import com.peoplein.moiming.model.dto.response.TokenRespDto;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.RoleRepository;
import com.peoplein.moiming.security.token.MoimingTokenProvider;
import com.peoplein.moiming.security.token.MoimingTokenType;
import com.peoplein.moiming.security.token.TokenDto;
import com.peoplein.moiming.security.token.logout.LogoutTokenManager;
import com.peoplein.moiming.service.util.MemberNicknameCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import org.springframework.util.StringUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static com.peoplein.moiming.domain.enums.VerificationType.FIND_PW;
import static com.peoplein.moiming.domain.enums.VerificationType.SIGN_UP;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.security.exception.AuthExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.AuthReqDto.*;
import static com.peoplein.moiming.model.dto.response.AuthRespDto.*;
import static com.peoplein.moiming.security.token.MoimingTokenType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PolicyAgreeService policyAgreeService;
    private final SmsVerificationService smsVerificationService;

    private final PasswordEncoder passwordEncoder;
    private final MoimingTokenProvider tokenProvider;
    private final LogoutTokenManager logoutTokenManager;


    @Transactional(readOnly = true)
    public boolean checkEmailAvailable(String email) {
        Optional<Member> memberOp = memberRepository.findByEmail(email);
        return memberOp.isEmpty();
    }


    @Transactional
    public AuthSignInRespDto signUp(AuthSignInReqDto requestDto) {

        checkUniqueColumnDuplication(requestDto.getMemberEmail(), requestDto.getMemberPhone());

        SmsVerification smsVerification = smsVerificationService.confirmAndGetValidSmsVerification(SIGN_UP, requestDto.getSmsVerificationId());

        // TODO :: 인코딩 AOP 로 빼주면 좋을 듯
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        Role roleUser = roleRepository.findByRoleType(RoleType.USER);


        if (!smsVerification.getMemberPhoneNumber().equals(requestDto.getMemberPhone())) {
            log.error("{}, signIn :: {}", this.getClass().getName(), "SMS Verification 사용된 번호와 회원 가입 번호가 불일치합니다");
            throw new MoimingAuthApiException(AUTH_SMS_REQUEST_INFO_NOT_MATCH_REQUESTING_INFO);
        }


        // MEMO :: MVP 에선 CI 는 ALL NULL 이다
        Member signInMember = Member.createMember(requestDto.getMemberEmail(), encodedPassword, requestDto.getMemberName()
                , requestDto.getMemberPhone(), requestDto.getMemberGender()
                , requestDto.getMemberBirth(), requestDto.getFcmToken(), null, roleUser);


        String createdNickname = tryCreateNicknameForUser(); // 실패시 일괄 rollback
        signInMember.changeNickname(createdNickname);

        // member 저장
        memberRepository.save(signInMember);

        // Policy 저장 분리
        policyAgreeService.createPolicyAgree(signInMember, requestDto.getPolicyDtos());

        // Refresh 토큰 발급 & Response Data 생성
        TokenRespDto tokenRespDto = issueTokensAndUpdateColumns(true, signInMember);
        return new AuthSignInRespDto(signInMember, tokenRespDto);
    }


    @Transactional
    public AuthSignInRespDto devSignUp(DevAuthSignInReqDto requestDto) {

        checkUniqueColumnDuplication(requestDto.getMemberEmail(), requestDto.getMemberPhone());

        // TODO :: 인코딩 AOP 로 빼주면 좋을 듯
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        Role roleUser = roleRepository.findByRoleType(RoleType.USER);

        // MEMO :: MVP 에선 CI 는 ALL NULL 이다
        Member signInMember = Member.createMember(requestDto.getMemberEmail(), encodedPassword, requestDto.getMemberName()
                , requestDto.getMemberPhone(), requestDto.getMemberGender()
                , requestDto.getMemberBirth(), requestDto.getFcmToken(), null, roleUser);


        String createdNickname = tryCreateNicknameForUser(); // 실패시 일괄 rollback
        signInMember.changeNickname(createdNickname);

        // member 저장
        memberRepository.save(signInMember);

        // Policy 저장 분리
        policyAgreeService.createPolicyAgree(signInMember, requestDto.getPolicyDtos());

        // Refresh 토큰 발급 & Response Data 생성
        TokenRespDto tokenRespDto = issueTokensAndUpdateColumns(true, signInMember);

        return new AuthSignInRespDto(signInMember, tokenRespDto);

    }



    /*
    Reissue Token 은 토큰 재발급 요청
    > 토큰을 발급하는 로직 자체와는 다름
    > TODO :: Resp Dto 재 Setting 필요. Inner Dto 전달 금지
    */
    // noRollbackFor 는 Token Not Match Exception 이 발생하여도 정상적으로 RefreshToken 이 삭제되게 하기 위함
    @Transactional(noRollbackFor = {MoimingAuthNoRollbackException.class})
    public TokenRespDto reissueToken(AuthTokenReqDto requestDto) {

        if (requestDto == null) {
            log.error("Class {} : {}", getClass().getName(), COMMON_INVALID_PARAM.getErrMsg());
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        String curRefreshToken = requestDto.getToken();
        String rtEmail = verifyMemberEmail(JWT_RT, curRefreshToken);

        Member memberPs = memberRepository.findByEmail(rtEmail).orElseThrow(() -> {
            log.error("Class {} : {}", getClass().getName(), MEMBER_NOT_FOUND.getErrMsg());
            return new MoimingApiException(MEMBER_NOT_FOUND);
        });


        if (!StringUtils.hasText(memberPs.getRefreshToken()) || !memberPs.getRefreshToken().equals(curRefreshToken)) {
            log.error("Class {} : {}", getClass().getName(), AUTH_REFRESH_TOKEN_NOT_MATCH.getErrMsg());
            memberPs.changeRefreshToken(""); // RefreshToken 을 삭제한다
            throw new MoimingAuthNoRollbackException(AUTH_REFRESH_TOKEN_NOT_MATCH);
        }

        return issueTokensAndUpdateColumns(true, memberPs);
    }


    /*
     회원가입 전에 중복 조건들에 대해서 확인
     에러 발생시 회원 가입 중단
     // TODO :: 이거 DB 에서 컷되는데 굳이 해줘야함? 세 개 가지고 조회하는거라 Full-scan 꽤 오버헤드 존재해보임
     //         당연히 당장은 큰 문제 X
     */
    void checkUniqueColumnDuplication(String memberEmail, String memberPhone) {

        List<Member> duplicateMembers = memberRepository.findMembersByEmailOrPhone(memberEmail, memberPhone);

        if (!duplicateMembers.isEmpty()) {
            log.info("{}, checkUniqueColumnDuplication :: {}", this.getClass().getName(), "[" + memberEmail + ", " + memberPhone + "] 중복되는 이메일 혹은 핸드폰 번호의 가입입니다");
            throw new MoimingAuthApiException(AUTH_SIGN_IN_DUPLICATE_COLUMN);
        }
    }


    @Transactional(readOnly = true)
    public String tryCreateNicknameForUser() {

        MemberNicknameCreator nicknameCreator = new MemberNicknameCreator();

        int trial = 1;
        while (trial < 10) {
            String createdNickname = nicknameCreator.createNickname();
            Optional<Member> memberOp = memberRepository.findByNickname(createdNickname);
            if (memberOp.isEmpty()) { // 중복이 없어야 한다
                return createdNickname;
            }
            trial += 1; // 중복이면 다시 시도
        }

        throw new MoimingAuthApiException(AUTH_SIGN_IN_NICKNAME_FAIL);

    }


    /*
     이메일 확인 요청 확정 및 이메일 마스킹 전달
     */
    @Transactional(readOnly = true)
    public String findMemberEmail(AuthFindIdReqDto requestDto) {

        if (requestDto == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        // 검증된 verification 이 반환된다
        SmsVerification verifiedSms = smsVerificationService.getVerifiedSmsVerification(requestDto.getSmsVerificationId(), VerificationType.FIND_ID, requestDto.getVerificationNumber());

        if (!verifiedSms.getMemberPhoneNumber().equals(requestDto.getMemberPhone())) { // 발생할 일 없음
            log.error("{}, findMemberEmail :: {}", this.getClass().getName(), "전달받은 전화번호와 조회된 Verification 객체의 전화번호가 일치하지 않음 - 발생할 일 없는 상황 발생");
            throw new MoimingAuthApiException(AUTH_SMS_REQUEST_INFO_NOT_MATCH_REQUESTING_INFO);
        }

        Member member = memberRepository.findById(verifiedSms.getMemberId()).orElseThrow(() -> {
                    log.error("{}, findMemberEmail :: {}", this.getClass().getName(), "인증 객체 내 member Id 의 멤버를 찾을 수 없음");
                    return new MoimingApiException(MEMBER_NOT_FOUND);
                }
        );

        return member.getMaskedEmail();
    }


    // 비밀번호 재설정 인증 확인을 진행한다 - 인증 번호를 통해 확인
    // 통과 여부를 확인한다
    @Transactional
    public void verifySmsVerification(Long smsVerificationId, VerificationType type, String memberPhone, String verificationNumber) {

        if (smsVerificationId == null || type == null || !StringUtils.hasText(memberPhone) || !StringUtils.hasText(verificationNumber)) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        // 검증된 verification 이 반환된다
        SmsVerification verifiedSms = smsVerificationService.getVerifiedSmsVerification(smsVerificationId, type, verificationNumber);

        if (!verifiedSms.getMemberPhoneNumber().equals(memberPhone)) { // 발생할 일 없음
            throw new MoimingAuthApiException(AUTH_SMS_REQUEST_INFO_NOT_MATCH_REQUESTING_INFO);
        }

    }


    // 인증된 인증 객체를 확인하고, 비밀번호를 변경한다
    @Transactional
    public void resetPassword(AuthResetPwReqDto requestDto) {

        if (requestDto == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        SmsVerification smsVerification = smsVerificationService.confirmAndGetValidSmsVerification(FIND_PW, requestDto.getSmsVerificationId());

        // member 를 가져옴
        Member member = memberRepository.findById(smsVerification.getMemberId()).orElseThrow(() -> {
                    log.error("{}, resetPassword :: {}", this.getClass().getName(), "비밀번호 변경 인증 객체 내 member Id 의 멤버를 찾을 수 없음");
                    return new MoimingApiException(MEMBER_NOT_FOUND);
                }
        );

        // member 비밀번호를 변경한다
        String encodedPw = passwordEncoder.encode(requestDto.getChangePassword());
        member.changePassword(encodedPw);

    }


    /*
     토큰 발급 로직
     사용 로직 : 로그인 / 회원가입 / (위에 있음) 토큰 재발급 로직
               유저에 대한 인증, 갱신 토큰을 발급한다
     */
    @Transactional
    public TokenRespDto issueTokensAndUpdateColumns(boolean persisted, Member member) {

        if (!persisted) {
            member = memberRepository.findById(member.getId()).orElseThrow(
                    () -> new MoimingApiException(ExceptionValue.MEMBER_NOT_FOUND)
            );
        }

        TokenDto accessTokenDto = tokenProvider.generateToken(JWT_AT, member);
        TokenDto refreshTokenDto = tokenProvider.generateToken(JWT_RT, member);

        member.changeRefreshToken(refreshTokenDto.getJwtToken());
        member.changeLastLoginAt();

        return new TokenRespDto(accessTokenDto.getJwtToken(), accessTokenDto.getExp(), refreshTokenDto.getJwtToken(), refreshTokenDto.getExp());
    }


    /*
     사용 로직 : 모든 요청 인증
     모든 요청은 인증될 시, logoutTokenMap 에서 관리중인 토큰인지 확인한다
     */
    public boolean isLogoutToken(String accessToken) {
        return logoutTokenManager.isUnusableToken(accessToken);
    }


    /*
      사용 로직 : 모든 요청 인증 / 갱신 토큰 인증
      TokenProvider 를 사용해서 Knox Id 를 반환한다
     */
    public String verifyMemberEmail(MoimingTokenType type, String jwtToken) {
        return tokenProvider.verifyMemberEmail(type, jwtToken);
    }


    /*
     사용 로직 : 모든 요청 인증
     모든 요청은 인증될 시, member 를 persist 해서 로그인 날짜를 최신화해준다
    */
    @Transactional
    public void updateLoginAt(boolean persisted, Member member) {
        if (!persisted) { // update 를 위한 persist
            member = memberRepository.findById(member.getId()).orElseThrow(() ->
//                     TODO :: 인 앱 예외 구체화 필요
                            new RuntimeException("해당 인원을 찾을 수 없습니다")
            );
        }

        member.changeLastLoginAt();
    }
}