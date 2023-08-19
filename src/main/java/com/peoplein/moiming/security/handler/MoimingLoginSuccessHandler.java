package com.peoplein.moiming.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.auth.oauth2.JwtProvider;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.response.MemberResponseDto;
import com.peoplein.moiming.model.dto.response_a.MemberRespDto;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.security.provider.token.JwtParams;
import com.peoplein.moiming.security.provider.token.MoimingTokenProvider;
import com.peoplein.moiming.security.provider.token.MoimingTokenType;
import com.peoplein.moiming.security.service.SecurityMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static com.peoplein.moiming.model.dto.response_a.MemberRespDto.*;

@RequiredArgsConstructor
public class MoimingLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MoimingTokenProvider moimingTokenProvider;
    private final SecurityMemberService securityMemberService;
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

        String accessJwtToken = moimingTokenProvider.generateToken(MoimingTokenType.JWT_AT, securityMember.getMember());
        response.addHeader(JwtParams.HEADER, JwtParams.PREFIX + accessJwtToken);

        ResponseBodyDto<MemberLoginRespDto> responseBody = ResponseBodyDto.createResponse(1, "로그인 성공", new MemberLoginRespDto(securityMember.getMember()));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(om.writeValueAsString(responseBody));

        response.setStatus(HttpStatus.OK.value());

    }
}