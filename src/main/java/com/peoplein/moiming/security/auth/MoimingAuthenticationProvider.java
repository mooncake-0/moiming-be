package com.peoplein.moiming.security.auth;

import com.peoplein.moiming.security.exception.LoginAttemptException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.peoplein.moiming.security.exception.AuthExceptionValue.*;

@Slf4j
@RequiredArgsConstructor
public class MoimingAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /*
     SecurityMemberService 를 통해 들고온 OldSecurityMember 객체와 (DB조회)
     Manager 가 넘겨준 미인증 Authentication 객체의 비밀번호를 검증한다
     인증된 Authentication 객체를 반환하여 Manager > Filter 단까지 다시 올려준s다
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        UserDetails userDetails = userDetailsService.loadUserByUsername((String) authentication.getPrincipal());

        if (!passwordEncoder.matches((String) authentication.getCredentials(), userDetails.getPassword())) {
            throw new LoginAttemptException(AUTH_LOGIN_PASSWORD_INCORRECT);
        }

        JwtAuthenticationToken authenticatedToken = new JwtAuthenticationToken(userDetails, null, userDetails.getAuthorities());

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
