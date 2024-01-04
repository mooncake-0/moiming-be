package com.peoplein.moiming.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.peoplein.moiming.config.AppUrlPath;
import com.peoplein.moiming.model.dto.request.AuthReqDto;
import com.peoplein.moiming.security.exception.AuthExceptionValue;
import com.peoplein.moiming.security.exception.LoginAttemptException;
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

import java.io.IOException;

import static com.peoplein.moiming.model.dto.request.AuthReqDto.*;
import static com.peoplein.moiming.security.exception.AuthExceptionValue.*;

@Slf4j
public class MoimingLoginFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper om = new ObjectMapper();


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

        AuthLoginReqDto loginParams = null;

        try {
            loginParams = om.readValue(request.getReader(), AuthLoginReqDto.class);
        } catch (IOException e) {
            log.error("{}, {}", "로그인 정보 인식 실패", e.getMessage());
            throw new LoginAttemptException(AUTH_COMMON_INVALID_PARAM_NULL, e);
        }

        if (!StringUtils.hasText(loginParams.getMemberEmail()) || !StringUtils.hasText(loginParams.getPassword())) {
            throw new LoginAttemptException(AuthExceptionValue.AUTH_LOGIN_REQUEST_INVALID);
        }

        JwtAuthenticationToken preAuthentication = new JwtAuthenticationToken(loginParams.getMemberEmail(), loginParams.getPassword());
        AuthenticationManager authenticationManager = getAuthenticationManager();

        return authenticationManager.authenticate(preAuthentication);

    }
}