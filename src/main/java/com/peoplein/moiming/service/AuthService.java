package com.peoplein.moiming.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.exception.MoimingInvalidTokenException;
import com.peoplein.moiming.model.dto.inner.TokenDto;
import com.peoplein.moiming.model.dto.request.MemberReqDto.MemberSignInReqDto;
import com.peoplein.moiming.model.dto.request.TokenReqDto;
import com.peoplein.moiming.model.dto.response.TokenRespDto;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.RoleRepository;
import com.peoplein.moiming.security.token.MoimingTokenProvider;
import com.peoplein.moiming.security.token.MoimingTokenType;
import com.peoplein.moiming.service.util.MemberNicknameCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.util.StringUtils;

import static com.peoplein.moiming.model.dto.response.MemberRespDto.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor // 중복 Type 이 있는 경우에만 직접 생성자 주입하도록 함
public class AuthService {

    public final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    public final String KEY_RESPONSE_DATA = "RESPONSE_DATA";

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final MoimingTokenProvider tokenProvider;
    private final PolicyAgreeService policyAgreeService;


    public boolean checkEmailAvailable(String email) {
        Optional<Member> memberOp = memberRepository.findByEmail(email);
        //            throw new MoimingApiException("[" + email + "]" + "는 이미 존재하는 EMAIL 입니다");
        return memberOp.isEmpty();
    }


    public Map<String, Object> signIn(MemberSignInReqDto requestDto) {

        checkUniqueColumnDuplication(requestDto.getMemberEmail(), requestDto.getMemberPhone(), requestDto.getCi());

        // TODO :: 인코딩 AOP 로 빼주면 좋을 듯
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        Role roleUser = roleRepository.findByRoleType(RoleType.USER);


        Member signInMember = Member.createMember(requestDto.getMemberEmail(), encodedPassword, requestDto.getMemberName()
                , requestDto.getMemberPhone(), requestDto.getMemberGender(), requestDto.getForeigner()
                , requestDto.getMemberBirth(), requestDto.getFcmToken(), requestDto.getCi(), roleUser);


        String createdNickname = tryCreateNicknameForUser(); // 실패시 일괄 rollback
        signInMember.changeNickname(createdNickname);

        // member 저장
        memberRepository.save(signInMember);

        // Policy 저장 분리
        policyAgreeService.createPolicyAgree(signInMember, requestDto.getPolicyDtos());


        // Refresh 토큰 발급 & Response Data 생성
        TokenDto tokenDto = issueJwtToken(true, signInMember);
        MemberSignInRespDto responseData = new MemberSignInRespDto(signInMember);

        // 두 객체 응답을 위한 HashMap
        Map<String, Object> transmit = new HashMap<>();
        transmit.put(KEY_ACCESS_TOKEN, tokenDto.getAccessToken());
        transmit.put(KEY_RESPONSE_DATA, responseData);

        return transmit;
    }


    /*
    재발급은 우성 REFRESH TOKEN 으로만 진행한다
    */
    public TokenDto reissueToken(TokenReqDto requestDto) {

        String curRefreshToken = requestDto.getToken();
        String rtEmail = "";

        try {
            rtEmail = verifyAndClaimEmail(MoimingTokenType.JWT_RT, curRefreshToken);
        } catch (JWTVerificationException exception) { // Verify 시 최상위 Exception
            log.info("Verify 도중 알 수 없는 예외가 발생 : {}", exception.getMessage());
            throw exception;
        }

        Member memberPs = memberRepository.findByEmail(rtEmail).orElseThrow(() ->
                new MoimingApiException("해당 토큰에 저장된 Email 로 가입된 유저가 없습니다")
        );

        // Refresh Token 저장값이 없다. 두 Refresh Token 이 일치하지 않는 경우 REFRESH TOKEN 삭제 및 재로그인 유도
        if (!StringUtils.hasText(memberPs.getRefreshToken()) || !memberPs.getRefreshToken().equals(curRefreshToken)) {
            String errMsg = "Refresh Token 검증에 실패하였습니다. 다시 로그인 해주세요";
            log.info(errMsg + ": {}", rtEmail); // 이메일 로깅

            memberPs.changeRefreshToken(""); // RefreshToken 을 삭제한다
            throw new MoimingInvalidTokenException(errMsg);
        }

        return issueJwtToken(true, memberPs);
    }


    /*
     회원가입 전에 중복 조건들에 대해서 확인
     에러 발생시 회원 가입 중단
     // TODO :: 이거 DB 에서 컷되는데 굳이 해줘야함? 세 개 가지고 조회하는거라 Fullscan 꽤 오버헤드 존재해보임
     //         당연히 당장은 큰 문제 X
     */
    // Test 에서 보이게 하기 위한 package-private 으로 변경
    void checkUniqueColumnDuplication(String memberEmail, String memberPhone, String ci) {

        List<Member> duplicateMembers = memberRepository.findMembersByEmailOrPhoneOrCi(memberEmail, memberPhone, ci);

        if (!duplicateMembers.isEmpty()) {
            for (Member member : duplicateMembers) {

                if (member.getMemberEmail().equals(memberEmail)) {
                    throw new MoimingApiException("[" + memberEmail + "] 는  이미 존재하는 회원입니다");
                }

                if (member.getMemberInfo().getMemberPhone().equals(memberPhone)) {
                    throw new MoimingApiException("[" + memberPhone + "] 는  이미 존재하는 회원의 전화번호 입니다");
                }

                if (member.getCi().equals(ci)) {
                    throw new MoimingApiException("이미 존재하는 회원의 CI 입니다");
                }

                //...
            }
        }
    }

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

        throw new MoimingApiException("");

    }


    // 3 가지 - 로그인 / 액토만료로 인한 재발급 / 회원가입
    public TokenDto issueJwtToken(boolean persisted, Member member) {
        if (!persisted) {
            member = memberRepository.findById(member.getId()).orElseThrow(
                    () -> new MoimingApiException(ExceptionValue.MEMBER_NOT_FOUND)
            );
        }

        String jwtAccessToken = tokenProvider.generateToken(MoimingTokenType.JWT_AT, member);
        String jwtRefreshToken = tokenProvider.generateToken(MoimingTokenType.JWT_RT, member);

        member.changeRefreshToken(jwtRefreshToken);
        member.changeLastLoginAt();

        return new TokenDto(jwtAccessToken, jwtRefreshToken);
    }


    // 토큰을 인증해준다
    public String verifyAndClaimEmail(MoimingTokenType type, String token) {

        return tokenProvider.verifyMemberEmail(type, token);
    }

}