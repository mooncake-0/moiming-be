package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.security.OldJwtPropertySetting;
import com.peoplein.moiming.security.provider.token.JwtParams;
import com.peoplein.moiming.security.provider.token.MoimingTokenProvider;
import com.peoplein.moiming.security.provider.token.MoimingTokenType;
import com.peoplein.moiming.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.util.Map;

import static com.peoplein.moiming.model.dto.requesta.MemberReqDto.*;
import static com.peoplein.moiming.model.dto.response_a.MemberRespDto.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_AUTH_VER + NetworkSetting.API_AUTH)
public class AuthController {

    private final AuthService authService;


    @GetMapping("/available/{email}")
    public ResponseEntity<?> checkUidAvailable(@PathVariable String email) {
        authService.checkEmailAvailable(email);
        return ResponseEntity.ok().body(ResponseBodyDto.createResponse(1, "사용 가능", null));
    }


    /*
     회원가입 요청 수신
     */
    @PostMapping("/signin")
    public ResponseEntity<?> signInMember(@RequestBody @Valid MemberSignInReqDto requestDto, BindingResult br
            , HttpServletResponse response) {

        Map<String, Object> transmit = authService.signIn(requestDto);

        // 응답 준비
        String jwtAccessToken = transmit.get(authService.KEY_ACCESS_TOKEN).toString();
        response.addHeader(JwtParams.HEADER, JwtParams.PREFIX + jwtAccessToken);

        return new ResponseEntity<>(ResponseBodyDto.createResponse(1, "회원 생성 성공", transmit.get(authService.KEY_RESPONSE_DATA)), HttpStatus.CREATED);

    }
}