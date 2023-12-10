package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.security.token.MoimingTokenProvider;
import com.peoplein.moiming.security.token.MoimingTokenType;
import com.peoplein.moiming.service.util.LogoutTokenManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.Date;

import static com.peoplein.moiming.exception.ExceptionValue.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MoimingTokenProvider moimingTokenProvider;
    private final LogoutTokenManager logoutTokenManager;

    public void logout(String accessToken, Member member) {
        if (accessToken == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM_NULL);
        }

        // refreshToken 을 영속화하기 위해
        final Long memberId = member.getId();
        member = memberRepository.findById(memberId).orElseThrow(
                () -> {
                    log.error("[" + memberId + "] 의 멤버를 영속화할 수 없습니다");
                    return new MoimingApiException(COMMON_INVALID_SITUATION);
                }
        );

        member.changeRefreshToken(null);

        Date expireAt = moimingTokenProvider.verifyExpireAt(MoimingTokenType.JWT_AT, accessToken);
        logoutTokenManager.saveLogoutToken(accessToken, expireAt);

    }


    public void dormant() {

        // 스케줄링 되는게 아니라, 별도의 input 에 따른 ADMIN 단의 요청 따위일 것(ADMIN 권한으로 처리된다)
        // TODO :: dormant 같은 경우 역시 ADMIN 단 ROLE 이 확인되는게 좋을듯? 추후 협의

        // 휴면으로 전환시 member field dormant = true 로 변경
        // 모든 모임 > Inactive By Dormant 로 탈퇴 처리 (ACTIVE 감소)
        //     게시물, 댓글 > 생존, member_fk 는 유지된다 > 해당 Member 의 현 상태를 정확히 진단하기 위함

    }


    public void delete() {

        // 탈퇴 처리된다면 7일까지 재가입 방지를 위해 보관한다 (번복은 불가)
        // refreshToken, fcmToken 제외 모두 유지
        // MemberInfo 테이블은 모두 삭제 (현재 이건 Pending.. ) > 법적으로는 즉시 삭제 필요

        // 7 일 뒤 모두 삭제
        // PK 값은 유지, hasDeleted = true 로만 유지 한다. nickname, ci, memberEmail 모두 삭제처리
        // 필수값들은 삭제처리가 아닌 dummy 값으로 보관된다

    }

}
