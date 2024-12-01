package com.peoplein.moiming.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.request.AuthReqDto;
import com.peoplein.moiming.model.dto.response.TokenRespDto;
import com.peoplein.moiming.security.auth.JwtAuthenticationToken;
import com.peoplein.moiming.security.auth.model.SecurityMember;
import com.peoplein.moiming.security.exception.LoginAttemptException;
import com.peoplein.moiming.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;

import static com.peoplein.moiming.model.dto.request.AuthReqDto.*;
import static com.peoplein.moiming.model.dto.response.AuthRespDto.*;
import static com.peoplein.moiming.security.exception.AuthExceptionValue.AUTH_COMMON_INVALID_PARAM_NULL;

@Slf4j
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

        // 응답용 FCM Token 을 전달한다 (의미 없지만 확인용)
        String fcmToken = member.getFcmToken();
        if (authentication instanceof JwtAuthenticationToken) {
            String loginFcmToken = ((JwtAuthenticationToken) authentication).getFcmToken();
            if (StringUtils.hasText(loginFcmToken)) { // fcm token 이 로그인시 전달되었을 경우 전달된다
                fcmToken = loginFcmToken;
            }
        }

        TokenRespDto tokenRespDto = authService.issueTokensAndUpdateColumns(false, member, fcmToken);

        log.info("{}, onAuthenticationSuccess :: {}", this.getClass().getName(), "["+ member.getMemberPhone() + "] 의 유저가 " +
                "[" + LocalDateTime.now() + "]의 시간에 성공적으로 로그인 하였습니다");

        // 위 함수를 통과해도 member 는 update 되지 않는다.
        // 위에 member 가 issueToken 에 전달되어도,
        ResponseBodyDto<AuthLoginRespDto> responseBody = ResponseBodyDto.createResponse("1", "로그인 성공"
                , new AuthLoginRespDto(member, tokenRespDto, fcmToken)
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(om.writeValueAsString(responseBody));
        response.setStatus(HttpStatus.OK.value());

    }
}