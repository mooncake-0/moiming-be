package com.peoplein.moiming.security.service;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.security.auth.model.SecurityMember;
import com.peoplein.moiming.security.exception.AuthExceptionValue;
import com.peoplein.moiming.security.exception.LoginAttemptException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class SecurityMemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /*
     전달받은 UID 를 통해 해당 UID 의 유저가 존재하는지 확인한다
     조회 후 Encrypt 되어 있는 Password 와 함께 반환한다
     */
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String memberPhone) throws LoginAttemptException {

        Member memberPs = memberRepository.findByMemberPhone(memberPhone).orElseThrow(() -> {
                    log.error("[" + memberPhone + "]의 유저를 찾을 수 없습니다");
                    return new LoginAttemptException(AuthExceptionValue.AUTH_LOGIN_PHONE_NOT_FOUND);
                }
        );

        if (memberPs.isHasDeleted()) {
            log.error("["+ memberPs.getId() + ", " + memberPhone + "]의 유저는 삭제된 유저입니다, 재가입 7일 방지 정책 존재");
            throw new LoginAttemptException(AuthExceptionValue.AUTH_LOGIN_MEMBER_DELETED);
        }

        return new SecurityMember(memberPs);
    }

    @Transactional
    public void saveUserFcmToken(String fcmToken) {
    }
}