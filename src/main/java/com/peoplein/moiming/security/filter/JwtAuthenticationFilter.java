package com.peoplein.moiming.security.filter;

import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.security.provider.token.JwtParams;
import com.peoplein.moiming.security.provider.token.MoimingTokenType;
import com.peoplein.moiming.security.provider.token.MoimingTokenProvider;
import com.peoplein.moiming.security.token.JwtAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
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


    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, MoimingTokenProvider moimingTokenProvider) {
        super(authenticationManager);
        this.userDetailsService = userDetailsService;
        this.moimingTokenProvider = moimingTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        String jwtToken = getJwtFromRequest(request);

        if (StringUtils.hasText(jwtToken)) { // ACCESS TOKEN 이 있다 // Verfiy 해서 Member 를 꺼내준다

            String tokenEmailValue = moimingTokenProvider.verifyMemberEmail(MoimingTokenType.JWT_AT, jwtToken);
            SecurityMember securityMember = (SecurityMember) userDetailsService.loadUserByUsername(tokenEmailValue);
            JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(securityMember.getMember(), null, securityMember.getAuthorities());

            // Access Token Verify 성공시 AuthenticationToken 이 저장된다
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // 실패시 아무것도 저장되지 않은채로 넘긴다
        }

        doFilter(request, response, chain);
    }


    /*
     요청 헤더에 Authorization Bearer Token (ACCESS_TOKEN) 이 존재하는지 확인하고 반환한다
     */
    private String getJwtFromRequest(HttpServletRequest request) {

        String authorizationValue = request.getHeader(JwtParams.HEADER);

        if (StringUtils.hasText(authorizationValue) && authorizationValue.startsWith("Bearer ")) {
            return authorizationValue.replace(JwtParams.PREFIX, "");
        }

        return null;
    }


}
