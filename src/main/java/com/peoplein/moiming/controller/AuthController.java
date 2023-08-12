package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.inner.TokenTransmitter;
import com.peoplein.moiming.security.JwtPropertySetting;
import com.peoplein.moiming.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static com.peoplein.moiming.model.dto.request_b.MemberReqDto.*;
import static com.peoplein.moiming.model.dto.response_a.MemberRespDto.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_AUTH_VER + NetworkSetting.API_AUTH)
public class AuthController {

    private final AuthService authService;


    @GetMapping("/uidAvailable/{email}")
    private ResponseModel<String> checkUidAvailable(@PathVariable String email) {
        if (authService.checkUidAvailable(email)) {
            return ResponseModel.createResponse(HttpStatus.OK, "OK", null);
        } else {
            throw new MoimingApiException("[" + email + "]" + "는 이미 존재하는 EMAIL 입니다");
        }
    }

    /*
     회원가입 요청 수신
     */
    @PostMapping("/signin")
    private ResponseModel<MemberSignInRespDto> signInMember(@RequestBody @Valid MemberSignInReqDto requestDto, BindingResult br
            , HttpServletResponse response) {

        TokenTransmitter<MemberSignInRespDto> data = authService.signIn(requestDto);
        prepareResponseWithToken(data.getAccessToken(), data.getRefreshToken(), response);

        return ResponseModel.createResponse(HttpStatus.CREATED, "Member 생성 성공", data.getData());
    }


    /*
     Http 처리는 모두 Controller 에서 한다 -> Service 단에 Http 누수 X
     */
    private void prepareResponseWithToken(String accessToken, String refreshToken, HttpServletResponse response) {

        response.addHeader(JwtPropertySetting.HEADER_AT, accessToken);
        response.addHeader(JwtPropertySetting.HEADER_RT, refreshToken);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);
    }

}