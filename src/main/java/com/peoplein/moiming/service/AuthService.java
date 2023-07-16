package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.exception.DuplicateAuthValueException;
import com.peoplein.moiming.model.dto.auth.MemberSigninRequestDto;
import com.peoplein.moiming.model.dto.response.MemberResponseDto;
import com.peoplein.moiming.model.dto.domain.MemberDto;
import com.peoplein.moiming.model.dto.domain.MemberInfoDto;
import com.peoplein.moiming.model.query.QueryDuplicateColumnMemberDto;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.RoleRepository;
import com.peoplein.moiming.repository.jpa.query.MemberJpaQueryRepository;
import com.peoplein.moiming.security.JwtPropertySetting;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.security.exception.AuthErrorEnum;
import com.peoplein.moiming.security.provider.token.MoimingTokenProvider;
import com.peoplein.moiming.security.provider.token.MoimingTokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final MoimingTokenProvider moimingTokenProvider;
    private final MemberJpaQueryRepository memberJpaQueryRepository;
    private final PolicyAgreeService policyAgreeService;

    public boolean checkUidAvailable(String uid) {
        Member memberByUid = memberRepository.findMemberByUid(uid);
        return Objects.isNull(memberByUid);
    }

    /*
     회원가입 로직을 실행한다
     1. Null 및 공백값들에 대한 2차적 확인
     2. Unique Columns 들에 대한 중복값 검증 및 대응
     3. 기본 MemberInfo 생성, User Role 부여

     ------------- TODO :: 회원 약관 동의 항목들에 대한 Policy Agree 객체 push -- 별도의 서비스 분리해서 가져가는게 좋을듯!

     4. 토큰 발급, Response 헤더 세팅
     5. ResponseDto 모델 세팅
     */
    public MemberResponseDto signin(MemberSigninRequestDto memberSigninDto, HttpServletResponse response) {

        DomainChecker.checkRightString("Member Signin Request Dto", true, memberSigninDto.getUid(), memberSigninDto.getPassword(), memberSigninDto.getEmail());
        checkUniqueColumnDuplication(memberSigninDto.getUid(), memberSigninDto.getEmail());

        // TODO : 이거 TEMP로 생성하는게 맞는건가?
        String encodedPassword = passwordEncoder.encode(memberSigninDto.getPassword());
        Role roleUser = roleRepository.findByRoleType(RoleType.USER);

        Member signInMember = Member.createMember(
                memberSigninDto.getUid(),
                encodedPassword,
                memberSigninDto.getEmail(),
                "TEMP",
                memberSigninDto.getFcmToken(),
                MemberGender.N,
                roleUser);

        memberRepository.save(signInMember);
        provideTokenBySignin(signInMember, response);

        // 약관 등록
        policyAgreeService.createUserPolicyAgree(signInMember, memberSigninDto.getPolicyAgreeList());


        // Response 객체 생성
        MemberDto memberDto = MemberDto.createMemberDtoWhenSignIn(signInMember);
        MemberInfoDto memberInfoDto = MemberInfoDto.createMemberInfoDtoWhenSignIn(signInMember.getMemberInfo());

        return new MemberResponseDto(memberDto, memberInfoDto);
    }


    /*
     회원가입 전에 중복 조건들에 대해서 확인
     에러 발생시 회원 가입 중단
     */
    private void checkUniqueColumnDuplication(String uid, String memberEmail) {

        List<QueryDuplicateColumnMemberDto> dupMembers = memberJpaQueryRepository.findDuplicateMemberByUidOrEmail(uid, memberEmail);

        if (!dupMembers.isEmpty()) {
            for (QueryDuplicateColumnMemberDto target : dupMembers) {
                if (target.getUid().equals(uid)) {
                    throw new DuplicateAuthValueException("[" + uid + "] 는  이미 존재하는 회원입니다", AuthErrorEnum.AUTH_SIGNIN_DUPLICATE_UID.getErrorCode());
                }

                if (target.getMemberEmail().equals(memberEmail)) {
                    throw new DuplicateAuthValueException("[" + memberEmail + "] 는  이미 존재하는 회원의 이메일입니다", AuthErrorEnum.AUTH_SIGNIN_DUPLICATE_EMAIL.getErrorCode());
                }

                //...
            }
        }
    }

    /*
     회원가입 성공시 인증 토큰을 발급한다
     */
    private void provideTokenBySignin(Member signinMember, HttpServletResponse response) {

        SecurityMember securityMember = new SecurityMember(signinMember, new ArrayList<>());

        String accessToken = moimingTokenProvider.generateToken(MoimingTokenType.JWT_AT, securityMember);
        String refreshToken = moimingTokenProvider.generateToken(MoimingTokenType.JWT_RT, securityMember);

        signinMember.changeRefreshToken(refreshToken);

        response.addHeader(JwtPropertySetting.HEADER_AT, accessToken);
        response.addHeader(JwtPropertySetting.HEADER_RT, refreshToken);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);

    }
}
