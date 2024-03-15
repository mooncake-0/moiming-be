package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.security.token.MoimingTokenProvider;
import com.peoplein.moiming.security.token.MoimingTokenType;
import com.peoplein.moiming.security.token.logout.LogoutTokenManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.exception.ExceptionValue.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MoimMemberRepository moimMemberRepository;

    private final PasswordEncoder passwordEncoder;
    private final MoimingTokenProvider moimingTokenProvider;
    private final LogoutTokenManager logoutTokenManager;


    @Transactional(readOnly = true)
    public Member getCurrentMember(Member member) {

        if (member == null) {
            log.info("{}, getCurrentMember ::{}", this.getClass().getName(), "인증 후 Member Null 감지");
            throw new MoimingApiException(COMMON_INVALID_SITUATION);
        }
        member = memberRepository.findWithMemberInfoById(member.getId()).orElseThrow(()->{
            log.info("{}, getCurrentMember :: {}", this.getClass().getName(), " Member 찾을 수 없습니다");
            return new MoimingApiException(MEMBER_NOT_FOUND);
        });
        return member;
    }


    @Transactional
    public void logout(String accessToken, Member member) {
        if (accessToken == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        member = persistSecurityMember(member.getId());
        member.changeRefreshToken(null);

        // LOGOUT 처리 토큰으로 등재한다
        Date expireAt = moimingTokenProvider.verifyExpireAt(MoimingTokenType.JWT_AT, accessToken);
        logoutTokenManager.saveLogoutToken(accessToken, expireAt);

    }


    @Transactional
    public void dormant(Long targetMemberId) {

        // 스케줄링 되는게 아니라, 별도의 input 에 따른 ADMIN 단의 요청 따위일 것(ADMIN 권한으로 처리된다)
        // TODO :: dormant 같은 경우 역시 ADMIN 단 ROLE 이 확인되는게 좋을듯? 추후 협의

        // 휴면으로 전환시 member field dormant = true 로 변경
        Member member = memberRepository.findById(targetMemberId).orElseThrow(() -> new MoimingApiException(
                MEMBER_NOT_FOUND
        ));

        // 모임 내 활동 처리
        // 모든 모임 > Inactive By Dormant 로 탈퇴 처리 (ACTIVE 감소)
        List<MoimMember> memberMoims = moimMemberRepository.findWithMoimByMemberId(targetMemberId);
        for (MoimMember memberMoim : memberMoims) {
            memberMoim.changeMemberState(MoimMemberState.IBD);
        }

        member.makeDormant();
    }


    public void delete() {

        // 탈퇴 처리된다면 7일까지 재가입 방지를 위해 보관한다 (번복은 불가)
        // refreshToken, fcmToken 제외 모두 유지
        // MemberInfo 테이블은 모두 삭제 (현재 이건 Pending.. ) > 법적으로는 즉시 삭제 필요
        // 7 일 뒤 모두 삭제
        // PK 값은 유지, hasDeleted = true 로만 유지 한다. nickname, ci, memberEmail 모두 삭제처리
        // 필수값들은 삭제처리가 아닌 dummy 값으로 보관된다

    }


    @Transactional
    public void confirmPw(String password, Member member) {

        if (!StringUtils.hasText(password) || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        if (!passwordEncoder.matches(password, member.getPassword())) {
            log.error("{}, confirmPw :: {}", this.getClass().getName(), "[" + member.getId() + "] 의 비밀번호 오류");
            throw new MoimingApiException(MEMBER_PW_INCORRECT);
        }
    }


    @Transactional
    public void changeNickname(String nickname, Member member) {

        if (!StringUtils.hasText(nickname) || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        member = persistSecurityMember(member.getId());

        if (member.getNickname().equals(nickname)) {
            throw new MoimingApiException(MEMBER_NICKNAME_UNAVAILABLE); // 현재와 동일한 닉네임 수정 불가
        }

        Optional<Member> memberOp = memberRepository.findByNickname(nickname);
        if (memberOp.isPresent()) {
            throw new MoimingApiException(MEMBER_NICKNAME_UNAVAILABLE); // 중복 닉네임 불가
        }

        member.changeNickname(nickname);

    }


    @Transactional
    public void changePw(String prePw, String newPw, Member member) {

        if (!StringUtils.hasText(prePw) || !StringUtils.hasText(newPw) || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        member = persistSecurityMember(member.getId());

        if (!passwordEncoder.matches(prePw, member.getPassword())) {
            log.error("{}, changePw :: {}", this.getClass().getName(), "[" + member.getId() + "] 의 유저의 비밀번호 변경 시도, 이전 비밀번호 불일치 발생");
            throw new MoimingApiException(MEMBER_PW_INCORRECT); // 현재 비밀번호가 맞는지 확인
        }

        String encodedPw = passwordEncoder.encode(newPw);
        member.changePassword(encodedPw);

    }


    private Member persistSecurityMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> {
            log.error("{}, persistSecurityMember :: {}", this.getClass().getName(), "Member 영속화 중 Id [" + memberId + "] 조회 불가 예외 발생");
            return new MoimingApiException(MEMBER_NOT_FOUND);
        });
    }
}
