package com.peoplein.moiming.security.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.security.token.JwtParams;
import com.peoplein.moiming.security.token.MoimingTokenType;
import com.peoplein.moiming.security.token.MoimingTokenProvider;
import com.peoplein.moiming.security.auth.JwtAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JwtAuthenticationFilter extends BasicAuthenticationFilter {


    private final UserDetailsService userDetailsService;

    private final MoimingTokenProvider moimingTokenProvider;

    private ObjectMapper om = new ObjectMapper();

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, MoimingTokenProvider moimingTokenProvider) {
        super(authenticationManager);
        this.userDetailsService = userDetailsService;
        this.moimingTokenProvider = moimingTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        String jwtToken = getJwtFromRequest(request);

        if (StringUtils.hasText(jwtToken)) { // ACCESS TOKEN 이 있다 // Verfiy 해서 Member 를 꺼내준다

            try {

                String tokenEmailValue = moimingTokenProvider.verifyMemberEmail(MoimingTokenType.JWT_AT, jwtToken);
                SecurityMember securityMember = (SecurityMember) userDetailsService.loadUserByUsername(tokenEmailValue);
                JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(securityMember.getMember(), null, securityMember.getAuthorities());

                // Access Token Verify 성공시 AuthenticationToken 이 저장된다
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                doFilter(request, response, chain);

            /*
             Verification 시 발생한 예외에 대해
             필터가 직접 Response 를 처리한다
            */
            } catch (JWTVerificationException exception) { // Verify 시 최상위 Exception

                processVerificationExceptionResponse(exception, response);
            }

        } else {
            // 실패시 아무것도 저장되지 않은채로 넘긴다
            doFilter(request, response, chain);
        }
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


    /*
     JWT 인증 중 발생한 에러는 직접 처리한다
     */
    private void processVerificationExceptionResponse(JWTVerificationException exception, HttpServletResponse response) throws IOException {

        int statusCode;
        int errCode = -1;
        String data = "";

        if (exception instanceof SignatureVerificationException) {

            log.error("SIGNATURE 에러, 올바르지 않은 Signature 로 접근하였습니다 : {}", exception.getMessage());
            statusCode = HttpStatus.FORBIDDEN.value();

        } else if (exception instanceof TokenExpiredException) {

            log.info("Access Token 이 만료되었습니다");
            errCode = -100;
            data = "ACCESS_TOKEN_EXPIRED";
            statusCode = HttpStatus.UNAUTHORIZED.value();

        } else {

            log.info("Verify 도중 알 수 없는 예외가 발생 : {}", exception.getMessage());
            statusCode = HttpStatus.BAD_REQUEST.value();

        }

        ResponseBodyDto<?> responseBody = ResponseBodyDto.createResponse(errCode, exception.getMessage(), data);
        response.setStatus(statusCode);
        response.getWriter().write(om.writeValueAsString(responseBody));

    }

}
