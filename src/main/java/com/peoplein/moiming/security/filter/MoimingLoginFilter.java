package com.peoplein.moiming.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.peoplein.moiming.config.AppUrlPath;
import com.peoplein.moiming.security.exception.AuthExceptionValue;
import com.peoplein.moiming.security.exception.LoginAttemptException;
import com.peoplein.moiming.security.exception.ExtraAuthenticationException;
import com.peoplein.moiming.security.auth.JwtAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.peoplein.moiming.model.dto.request.MemberReqDto.*;
import static com.peoplein.moiming.security.exception.AuthExceptionValue.*;

@Slf4j
public class MoimingLoginFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /*
      일반 Login 시도중임을 Filter
      결론적으로 인증객체와 Access Token, Refresh Token 을 발급해주는 경로
    */
    public MoimingLoginFilter() {

        super(new AntPathRequestMatcher(AppUrlPath.PATH_AUTH_LOGIN));

    }


    /*
      Jwt 요청시 처음으로 인지하고 authentication 을 수행하는 공간
      인증전 Authentication 객체를 형성하여 AuthenticationManager 에게 인증을 위임
      인증후 Authentication 객체를 세션에 저장하고 후속 진행
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        MemberLoginReqDto memberLoginDto = null;

        try {
            memberLoginDto = om.readValue(request.getReader(), MemberLoginReqDto.class);

            if (!StringUtils.hasText(memberLoginDto.getMemberEmail()) || !StringUtils.hasText(memberLoginDto.getPassword())) {

                throw new LoginAttemptException(AUTH_BAD_LOGIN_INPUT);
            }

        } catch (LoginAttemptException exception){ // 얘가 발생하면 그대로 날린다

            throw exception;

        } catch(Exception exception) { // 로그인 시도 중 그 외의 어떤 예외가 발생할 경우

            throw new ExtraAuthenticationException(AUTH_EXTRA, exception);
        }

        // 담긴 정보는 Authenticate 을 위해 Token 에 넣어서 Manager 에게 보내준다
        JwtAuthenticationToken preAuthentication = new JwtAuthenticationToken(memberLoginDto.getMemberEmail(), memberLoginDto.getPassword());
        AuthenticationManager authenticationManager = getAuthenticationManager();

        return authenticationManager.authenticate(preAuthentication);

    }
}