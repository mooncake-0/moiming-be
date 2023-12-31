package com.peoplein.moiming.security.service;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.security.exception.AuthExceptionValue;
import com.peoplein.moiming.security.exception.LoginAttemptException;
import com.peoplein.moiming.security.token.MoimingTokenProvider;
import com.peoplein.moiming.security.token.MoimingTokenType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.peoplein.moiming.security.exception.AuthExceptionValue.*;

@Slf4j
@Transactional
@RequiredArgsConstructor
public class SecurityMemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /*
     전달받은 UID 를 통해 해당 UID 의 유저가 존재하는지 확인한다
     조회 후 Encrypt 되어 있는 Password 와 함께 반환한다
     */
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String memberEmail) throws LoginAttemptException {

        Member memberPs = memberRepository.findByEmail(memberEmail).orElseThrow(() -> {
                    log.error("[" + memberEmail + "]의 유저를 찾을 수 없습니다");
                    return new LoginAttemptException(AUTH_EMAIL_NOT_FOUND);
                }
        );

        memberPs.changeLastLoginAt(); // 모든 인증 수행시
        return new SecurityMember(memberPs);
    }
}