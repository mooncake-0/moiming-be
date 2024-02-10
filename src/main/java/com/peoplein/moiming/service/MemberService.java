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
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.exception.ExceptionValue.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MoimMemberRepository moimMemberRepository;

    private final PasswordEncoder passwordEncoder;
    private final MoimingTokenProvider moimingTokenProvider;
    private final LogoutTokenManager logoutTokenManager;

    public void logout(String accessToken, Member member) {
        if (accessToken == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        // refreshToken 을 영속화하기 위해
//        final Long memberId = member.getId();
//        member = memberRepository.findById(memberId).orElseThrow(
//                () -> {
//                    log.error("[" + memberId + "] 의 멤버를 영속화할 수 없습니다");
//                    return new MoimingApiException(COMMON_INVALID_SITUATION);
//                }
//        );

        member.changeRefreshToken(null);

        Date expireAt = moimingTokenProvider.verifyExpireAt(MoimingTokenType.JWT_AT, accessToken);
        logoutTokenManager.saveLogoutToken(accessToken, expireAt);

    }


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


    //// 2024
    //// TODO :: 예외 일괄 Refactoring 후 적용 필요!! 다 임시적으로 조치해놓음

    public void confirmPw(String password, Member member) {

        if (!StringUtils.hasText(password) || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new MoimingApiException(MEMBER_PW_INCORRECT);
        }
    }


    public void changeNickname(String nickname, Member member) {

        if (!StringUtils.hasText(nickname) || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        if (member.getNickname().equals(nickname)) {
            throw new MoimingApiException(MEMBER_NICKNAME_UNAVAILABLE); // 현재와 동일한 닉네임 수정 불가
        }

        Optional<Member> memberOp = memberRepository.findByNickname(nickname);
        if (memberOp.isPresent()) {
            throw new MoimingApiException(MEMBER_NICKNAME_UNAVAILABLE); // 중복 닉네임 불가
        }

        member.changeNickname(nickname);

    }


    public void changePw(String prePw, String newPw, Member member) {

        if (!StringUtils.hasText(prePw) || !StringUtils.hasText(newPw) || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        if (!passwordEncoder.matches(prePw, member.getPassword())) {
            throw new MoimingApiException(MEMBER_PW_INCORRECT); // 현재 비밀번호가 맞는지 확인
        }

        String encodedPw = passwordEncoder.encode(newPw);
        member.changePassword(encodedPw);

    }

}
