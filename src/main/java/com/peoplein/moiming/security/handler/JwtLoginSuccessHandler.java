package com.peoplein.moiming.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.domain.MemberDto;
import com.peoplein.moiming.model.dto.domain.MemberInfoDto;
import com.peoplein.moiming.model.dto.response.MemberResponseDto;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.security.JwtPropertySetting;
import com.peoplein.moiming.security.provider.token.MoimingTokenProvider;
import com.peoplein.moiming.security.provider.token.MoimingTokenType;
import com.peoplein.moiming.security.service.SecurityMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MoimingTokenProvider tokenProvider;
    private final SecurityMemberService securityMemberService;
    private ObjectMapper om = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /*
     Filter 에서 Auth 성공 수신시 후 처리
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        SecurityMember securityMember = (SecurityMember) authentication.getPrincipal();

        Map<String, Object> valueMap = securityMemberService.prepareLoginResponseModel(securityMember);

        Member loggedInMember = (Member) valueMap.get("member");

        // TODO :: Security 단 임시 해결
        ResponseBodyDto<MemberResponseDto> loginSuccessfulResponse = ResponseBodyDto.createResponse(
                1, "로그인 성공", new MemberResponseDto(loggedInMember)
        );

        response.getWriter().write(om.writeValueAsString(loginSuccessfulResponse));

        response.addHeader(JwtPropertySetting.HEADER_AT, (String) valueMap.get(MoimingTokenType.JWT_AT.name()));
        response.addHeader(JwtPropertySetting.HEADER_RT, (String) valueMap.get(MoimingTokenType.JWT_RT.name()));

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);

    }

}