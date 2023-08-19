package com.peoplein.moiming.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.requesta.MemberReqDto.MemberSignInReqDto;
import com.peoplein.moiming.model.dto.requesta.TokenReqDto;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.RoleRepository;
import com.peoplein.moiming.security.provider.token.MoimingTokenProvider;
import com.peoplein.moiming.security.provider.token.MoimingTokenType;
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

import static com.peoplein.moiming.model.dto.response_a.MemberRespDto.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor // 중복 Type 이 있는 경우에만 직접 생성자 주입하도록 함
public class AuthService {

    public final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    public final String KEY_REFRESH_TOKEN = "REFRESH_TOKEN";
    public final String KEY_RESPONSE_DATA = "RESPONSE_DATA";

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final MoimingTokenProvider moimingTokenProvider;


    public void checkEmailAvailable(String email) {
        Optional<Member> memberOp = memberRepository.findMemberByEmail(email);
        if (memberOp.isPresent()) {
            throw new MoimingApiException("[" + email + "]" + "는 이미 존재하는 EMAIL 입니다");
        }
    }


    public Map<String, Object> signIn(MemberSignInReqDto requestDto) {

        checkUniqueColumnDuplication(requestDto.getMemberEmail(), requestDto.getMemberPhone());

        // TODO :: 인코딩 AOP 로 빼주면 좋을 듯
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        Role roleUser = roleRepository.findByRoleType(RoleType.USER);


        Member signInMember = Member.createMember(requestDto.getMemberEmail(), encodedPassword, requestDto.getMemberName()
                , requestDto.getMemberPhone(), requestDto.getMemberGender(), requestDto.isForeigner()
                , requestDto.getMemberBirth(), requestDto.getFcmToken(), roleUser);


        // member 저장
        memberRepository.save(signInMember);

        // Refresh 토큰 발급 & Response Data 생성
        String jwtAccessToken = issueJwtTokens(signInMember);
        MemberSignInRespDto responseData = new MemberSignInRespDto(signInMember);

        // 두 객체 응답을 위한 HashMap
        Map<String, Object> transmit = new HashMap<>();
        transmit.put(KEY_ACCESS_TOKEN, jwtAccessToken);
        transmit.put(KEY_RESPONSE_DATA, responseData);

        return transmit;
    }


    /*
     만료된 AT 와 RT 를 활용해서 재인증을 성공하고
     AT 와 RT 를 모두 재발급해준다
     */
    public Map<String, String> reissueToken(TokenReqDto requestDto) {

        String expiredAccessToken = requestDto.getExpiredAccessToken();
        String curRefreshToken = requestDto.getCurRefreshToken();

        try {

            String atEmail = moimingTokenProvider.verifyMemberEmail(MoimingTokenType.JWT_AT, expiredAccessToken);
            String rtEmail = moimingTokenProvider.verifyMemberEmail(MoimingTokenType.JWT_RT, curRefreshToken);

            if (!atEmail.equals(rtEmail)) {
                throw new RuntimeException("잠깐 기다려봐");
            }

            Member memberPs = memberRepository.findMemberByEmail(rtEmail).orElseThrow(() -> new RuntimeException("잠깐 기다려봐"));


            if (!StringUtils.hasText(memberPs.getRefreshToken()) || !memberPs.getRefreshToken().equals(curRefreshToken)) { // Refresh Token 에 문제가 있을 경우
                throw new RuntimeException("잠깐 기다려봐");
            }

            String jwtAccessToken = issueJwtTokens(memberPs);

            Map<String, String> transmit = new HashMap<>();
            transmit.put(KEY_ACCESS_TOKEN, jwtAccessToken);
            transmit.put(KEY_REFRESH_TOKEN, memberPs.getRefreshToken());

            return transmit;

        } catch (SignatureVerificationException exception) {

            log.error("SIGNATURE 에러, 올바르지 않은 Signature 로 접근하였습니다 : {}", exception.getMessage());
//            processVerificationExceptionResponse(exception, response);

        } catch (TokenExpiredException exception) { // REFRESH TOKEN 만료시

            log.info("Access Token 이 만료되었습니다");
//            processVerificationExceptionResponse(exception, response);

        } catch (JWTVerificationException exception) { // Verify 시 최상위 Exception
            log.info("Verify 도중 알 수 없는 예외가 발생 : {}", exception.getMessage());
//            processVerificationExceptionResponse(exception, response);

        }
    }

    /*
     회원가입 전에 중복 조건들에 대해서 확인
     에러 발생시 회원 가입 중단
     */
    // Test 에서 보이게 하기 위한 package-private 으로 변경
    void checkUniqueColumnDuplication(String memberEmail, String memberPhone) {

        List<Member> duplicateMembers = memberRepository.findByEmailOrPhone(memberEmail, memberPhone);

        if (!duplicateMembers.isEmpty()) {
            for (Member member : duplicateMembers) {

                if (member.getMemberEmail().equals(memberEmail)) {
                    throw new MoimingApiException("[" + memberEmail + "] 는  이미 존재하는 회원입니다");
                }

                if (member.getMemberInfo().getMemberPhone().equals(memberPhone)) {
                    throw new MoimingApiException("[" + memberPhone + "] 는  이미 존재하는 회원의 전화번호 입니다");
                }

                //...
            }
        }
    }


    // Test 에서 보이게 하기 위한 package-private 으로 변경
    // RT 는 저장하고, AT 는 반환해준다
    String issueJwtTokens(Member signInMember) {

        String jwtAccessToken = moimingTokenProvider.generateToken(MoimingTokenType.JWT_AT, signInMember);
        String jwtRefreshToken = moimingTokenProvider.generateToken(MoimingTokenType.JWT_RT, signInMember);

        signInMember.changeRefreshToken(jwtRefreshToken);

        return jwtAccessToken;
    }
}
