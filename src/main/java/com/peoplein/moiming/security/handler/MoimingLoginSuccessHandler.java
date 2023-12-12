package com.peoplein.moiming.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.inner.TokenDto;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.security.token.JwtParams;
import com.peoplein.moiming.security.token.MoimingTokenProvider;
import com.peoplein.moiming.security.token.MoimingTokenType;
import com.peoplein.moiming.security.service.SecurityMemberService;
import com.peoplein.moiming.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.peoplein.moiming.model.dto.response.MemberRespDto.*;

@RequiredArgsConstructor
public class MoimingLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private ObjectMapper om = new ObjectMapper();

    /*
     로그인 성공 했으면
     Refresh Token 저장 후 발급해줘야 함
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        /*
         Tx 가 끝났어도 Member Info 까지 다 프록시에 로딩된 Member 객체
         */
        SecurityMember securityMember = (SecurityMember) authentication.getPrincipal();

        TokenDto tokenDto = authService.issueJwtToken(false, securityMember.getMember());
        response.addHeader(JwtParams.HEADER, JwtParams.PREFIX + tokenDto.getAccessToken());

        ResponseBodyDto<MemberLoginRespDto> responseBody = ResponseBodyDto.createResponse("1", "로그인 성공", new MemberLoginRespDto(securityMember.getMember()));

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(om.writeValueAsString(responseBody));

        response.setStatus(HttpStatus.OK.value());

    }
}