package com.peoplein.moiming.security.provider;

import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.security.service.SecurityMemberService;
import com.peoplein.moiming.security.token.JwtAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /*
     SecurityMemberService 를 통해 들고온 OldSecurityMember 객체와 (DB조회)
     Manager 가 넘겨준 미인증 Authentication 객체의 비밀번호를 검증한다
     인증된 Authentication 객체를 반환하여 Manager > Filter 단까지 다시 올려준s다
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        SecurityMember securityMember = (SecurityMember) userDetailsService.loadUserByUsername((String) authentication.getPrincipal());

        if (!passwordEncoder.matches((String) authentication.getCredentials(), securityMember.getPassword())) {
            String msg = "비밀번호가 일치하지 않습니다";
            log.error(msg);
            throw new BadCredentialsException(msg);
        }

        JwtAuthenticationToken authenticatedToken = new JwtAuthenticationToken(securityMember, null, securityMember.getAuthorities());
        ((SecurityMemberService) userDetailsService).issueRefreshTokenToLoggedInMember(securityMember.getMember()); // 흠..


        return authenticatedToken;
    }


    /*
     AuthenticationManager 에서 실행되는 함수로, Manager 가 가지고 있는 Provider 중
     JwtAuthenticationToken 을 처리하는 Provider 임을 알려준다
    */
    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(JwtAuthenticationToken.class);
    }


}
