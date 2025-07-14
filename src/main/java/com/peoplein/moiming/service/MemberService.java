package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.repository.PostCommentRepository;
import com.peoplein.moiming.security.token.MoimingTokenProvider;
import com.peoplein.moiming.security.token.MoimingTokenType;
import com.peoplein.moiming.security.token.logout.LogoutTokenManager;
import com.peoplein.moiming.service.util.MemberPasswordManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.domain.enums.MoimMemberRoleType.*;
import static com.peoplein.moiming.domain.enums.MoimMemberState.*;
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
    private final MemberPasswordManager pwManager;


    public Member persistMember(Member member, boolean joinInfo) {

        if (member == null) {
            log.info("{}, persistMember ::{}", this.getClass().getName(), "인증 후 Member Null 감지");
            throw new MoimingApiException(COMMON_INVALID_SITUATION);
        }

        if (joinInfo) {
            member = memberRepository.findWithMemberInfoById(member.getId()).orElseThrow(() -> {
                log.info("{}, persistMember with Info:: {}", this.getClass().getName(), " Member 찾을 수 없습니다");
                return new MoimingApiException(MEMBER_NOT_FOUND);
            });
        } else {
            member = memberRepository.findById(member.getId()).orElseThrow(() -> {
                log.info("{}, persistMember :: {}", this.getClass().getName(), " Member 찾을 수 없습니다");
                return new MoimingApiException(MEMBER_NOT_FOUND);
            });
        }

        if (member.isHasDeleted()) {
            log.warn("{}, persistMember ::{}", this.getClass().getName(), "삭제된 유저의 조회 접근");
            throw new MoimingApiException(COMMON_INVALID_SITUATION, "삭제된 유저로 접근할 수 없습니다. This incident will be reported");
        }


        return member;
    }


    @Transactional
    public void logout(String accessToken, Member member) {
        if (accessToken == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        member = persistMember(member, false);
        member.changeRefreshToken(null);
        member.changeFcmToken(null);

        // LOGOUT 처리 토큰으로 등재한다
        Date expireAt = moimingTokenProvider.verifyExpireAt(MoimingTokenType.JWT_AT, accessToken);
        logoutTokenManager.saveLogoutToken(accessToken, expireAt);

        log.info("{}, logout :: {}", this.getClass().getName(), "[" + member.getMemberPhone() + "] 의 유저가 " +
                "[" + LocalDateTime.now() + "]의 시간에 성공적으로 로그아웃 처리 되었습니다");

    }


    private final MemberServiceMediator moimMemberServiceMed;
    @Transactional
    public void delete(Member member) {


        member = persistMember(member, false);

        // 탈퇴 처리된다면 7일까지 재가입 방지를 위해 보관한다 (번복은 불가)
        // 1 - 작성한 댓글, 작성한 게시물들
        //   - 이거만 로직 판단하고 짜자
        //   - 댓글, 게시물은 유지됨. 유저만 "삭제된 유저라고 전달되는거임"
        //   - 생서자도 안바꿔줘도 됨. that user - marked deleted
        //   - 댓글 / 게시물은 아무것도 할 필요가 없음


        // 2 - 가입된 모임 및 관계들
        //   - 모든 모임 관계를 NOTFOUND 로 변경, 모임장인 모임은 delegate 진행
        List<MoimMember> moimMembers = moimMemberRepository.findByMemberId(member.getId());
        for (MoimMember moimMember : moimMembers) {
            // 만약 MANAGER 가 요청한 것이라면, alternative flow 가 실행된다
            if (moimMember.hasPermissionOfManager()) {
                Moim thisMoim = moimMember.getMoim();
                if (thisMoim.getCurMemberCount() == 1) { // Moim 삭제 진행
                    // 후속으로 -1 과 IBW 변환은 진행되므로, moim 삭제 정보만 기록한다
                    thisMoim.markDeletedTime();
                    moimMember.changeMemberState(IBW);
                } else { // 모임장이 나가며, 모임장 승계가 진행된다
                    moimMemberServiceMed.delegateMoimManager(thisMoim);
                }
            }
            moimMember.changeMemberState(NOTFOUND);
        }

        // 3 - 발생한 알림들
        //   - 일괄 삭제 진행하려했는데, 연관관계도 없고 그래서 그냥 냅두자 일단..

        // 4 - 개인 정보 및 회원 상태 처리
        //   - 일단 일괄 보관, hasDeleted = true 처리
        //   - refreshToken, fcmToken 제외 모두 유지
        //   - MemberInfo 테이블은 모두 삭제 (현재 이건 Pending.. ) > 법적으로는 즉시 삭제 필요
        member.deleteMember();

    }


    @Transactional
    public void confirmPw(String password, Member member) {

        if (!StringUtils.hasText(password) || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        // 비밀번호 조회
        member = persistMember(member, false);

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

        member = persistMember(member, false);

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

        member = persistMember(member, false);

        if (!passwordEncoder.matches(prePw, member.getPassword())) {
            log.error("{}, changePw :: {}", this.getClass().getName(), "[" + member.getId() + "] 의 유저의 비밀번호 변경 시도, 이전 비밀번호 불일치 발생");
            throw new MoimingApiException(MEMBER_PW_INCORRECT); // 현재 비밀번호가 맞는지 확인
        }

        String encodedPw = pwManager.encodePwWhenUpdate(newPw, member.getPassword());
        member.changePassword(encodedPw);

    }


}
