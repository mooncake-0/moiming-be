package com.peoplein.moiming.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.response.TokenRespDto;
import com.peoplein.moiming.security.auth.model.SecurityMember;
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

import static com.peoplein.moiming.model.dto.response.AuthRespDto.*;

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

        Member member = ((SecurityMember) authentication.getPrincipal()).getMember();
        TokenRespDto tokenRespDto = authService.issueTokensAndUpdateColumns(false, member);

        ResponseBodyDto<AuthLoginRespDto> responseBody = ResponseBodyDto.createResponse("1", "로그인 성공"
                , new AuthLoginRespDto(member, tokenRespDto)
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(om.writeValueAsString(responseBody));
        response.setStatus(HttpStatus.OK.value());

    }
}