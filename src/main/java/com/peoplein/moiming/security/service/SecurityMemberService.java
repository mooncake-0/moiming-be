package com.peoplein.moiming.security.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MemberRoleLinker;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.security.provider.token.MoimingTokenProvider;
import com.peoplein.moiming.security.provider.token.MoimingTokenType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.util.*;

@Slf4j
@Transactional
public class SecurityMemberService implements UserDetailsService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MoimingTokenProvider moimingTokenProvider;

    /*
     전달받은 UID 를 통해 해당 UID 의 유저가 존재하는지 확인한다
     조회 후 Encrypt 되어 있는 Password 와 함께 반환한다
     */
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String memberEmail) throws UsernameNotFoundException {

        Member memberPs = memberRepository.findMemberByEmail(memberEmail).orElseThrow(() -> {
                    String msg = "[" + memberEmail + "]의 유저를 찾을 수 없습니다";
                    log.error(msg);
                    throw new UsernameNotFoundException(msg);
                }
        );

        return new SecurityMember(memberPs);
    }

    /*
     로그인한 유저에게 Token 을 발급하고 update 을 해준다
     loadUserByName 으로 보장된 Member 만 해당 쿼리를 타므로 (Member UID 보장)
     바로 update 쿼리를 날린다
     */
    public void issueRefreshTokenToLoggedInMember(Member loggedInMember) {

        if (Objects.isNull(loggedInMember)) {
            throw new MoimingApiException("이상한 일이 일어남");
        }
        String jwtRefreshToken = moimingTokenProvider.generateToken(MoimingTokenType.JWT_RT, loggedInMember);
        memberRepository.updateRefreshTokenByEmail(loggedInMember.getId(), jwtRefreshToken);
    }
}