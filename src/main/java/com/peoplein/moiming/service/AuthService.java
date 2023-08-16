package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.requesta.MemberReqDto.MemberSignInReqDto;
import com.peoplein.moiming.model.inner.TokenTransmitter;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.RoleRepository;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.security.provider.token.MoimingTokenProvider;
import com.peoplein.moiming.security.provider.token.MoimingTokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.model.dto.response_a.MemberRespDto.*;

@Service
@Transactional
@RequiredArgsConstructor // 중복 Type 이 있는 경우에만 직접 생성자 주입하도록 함
public class AuthService {

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


    public TokenTransmitter<MemberSignInRespDto> signIn(MemberSignInReqDto requestDto) {

        checkUniqueColumnDuplication(requestDto.getMemberEmail(), requestDto.getMemberPhone());

        // TODO :: 인코딩 AOP 로 빼주면 좋을 듯
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        Role roleUser = roleRepository.findByRoleType(RoleType.USER);


        Member signInMember = Member.createMember(requestDto.getMemberEmail(), encodedPassword, requestDto.getMemberName()
                , requestDto.getMemberPhone(), requestDto.getMemberGender(), requestDto.isForeigner()
                , requestDto.getMemberBirth(), requestDto.getFcmToken(), roleUser);


        // member 저장
        memberRepository.save(signInMember);

        // 토큰 발급
        String accessToken = provideTokenByMember(signInMember);

        // Response 객체 생성
        MemberSignInRespDto mrd = new MemberSignInRespDto(signInMember);
        return new TokenTransmitter<>(accessToken, signInMember.getRefreshToken(), mrd);

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
    String provideTokenByMember(Member signInMember) {

        SecurityMember sm = new SecurityMember(signInMember, new ArrayList<>()); // 권한은 토큰 생성에 필요하지 않음
        String accessToken = moimingTokenProvider.generateToken(MoimingTokenType.JWT_AT, sm);
        String refreshToken = moimingTokenProvider.generateToken(MoimingTokenType.JWT_RT, sm);
        signInMember.changeRefreshToken(refreshToken);

        return accessToken;
    }
}
