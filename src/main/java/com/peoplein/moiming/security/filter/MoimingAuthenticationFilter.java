package com.peoplein.moiming.security.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.security.auth.JwtAuthenticationToken;
import com.peoplein.moiming.security.auth.model.SecurityMember;
import com.peoplein.moiming.security.exception.AuthExceptionValue;
import com.peoplein.moiming.security.exception.LogoutTokenTrialException;
import com.peoplein.moiming.security.token.JwtParams;
import com.peoplein.moiming.security.token.MoimingTokenProvider;
import com.peoplein.moiming.security.token.MoimingTokenType;
import com.peoplein.moiming.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.peoplein.moiming.security.exception.AuthExceptionValue.*;

@Slf4j

public class MoimingAuthenticationFilter extends BasicAuthenticationFilter {


    private final UserDetailsService userDetailsService;
    private final AuthService authService;
    private final MoimingTokenProvider tokenProvider;

    private ObjectMapper om = new ObjectMapper();

    public MoimingAuthenticationFilter(AuthenticationManager authenticationManager, UserDetailsService userDetailsService
            , AuthService authService, MoimingTokenProvider tokenProvider) {

        super(authenticationManager);
        this.userDetailsService = userDetailsService;
        this.authService = authService;
        this.tokenProvider = tokenProvider;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        String accessToken = getJwtFromRequest(request);

        if (StringUtils.hasText(accessToken)) {
            try {

                if (authService.isLogoutToken(accessToken)) {
                    log.warn("{}, doFilterInternal :: {}", this.getClass().getName(), "로그아웃 관리 대상인 토큰으로 접근 시도");
                    throw new LogoutTokenTrialException(""); // 예외를 발생시킨다
                }

                UserDetails userDetails = tokenProvider.verifyAndCreateAuthUser(MoimingTokenType.JWT_AT, accessToken);

                // 이 객체의 FCM 토큰은 오직 로그인시 전달용으로만 사용되고, 후속에 사용되지 않는다
                SecurityContextHolder.getContext().setAuthentication(
                        new JwtAuthenticationToken(userDetails, null, null, userDetails.getAuthorities())
                );

            } catch (JWTVerificationException exception) {
                processAuthAttemptException(request, exception);
            }
        }

        doFilter(request, response, chain);

    }


    /*
     Token 처리 중 발생한 예외를 Moiming Exception 으로 변환하여 내보낸다
     */
    private void processAuthAttemptException(HttpServletRequest request, JWTVerificationException exception) {

        AuthExceptionValue errValues = AUTH_TOKEN_EXTRA;

        if (exception instanceof SignatureVerificationException) {
            errValues = AUTH_TOKEN_VERIFICATION_FAIL;
        } else if (exception instanceof TokenExpiredException) {
            errValues = AUTH_TOKEN_EXPIRED;
        } else if (exception instanceof LogoutTokenTrialException) {
            errValues = AUTH_LOGOUT_TOKEN_TRIAL;
        }

        request.setAttribute("EXCEPTION_VALUE", errValues);
    }


    /*
     요청 헤더에 Authorization Bearer Token (ACCESS_TOKEN) 이 존재하는지 확인하고 반환한다
     */
    private String getJwtFromRequest(HttpServletRequest request) {

        String authorizationValue = request.getHeader(JwtParams.HEADER);

        if (StringUtils.hasText(authorizationValue) && authorizationValue.startsWith(JwtParams.PREFIX)) {
            return authorizationValue.replace(JwtParams.PREFIX, "");
        }

        return null;
    }

}
