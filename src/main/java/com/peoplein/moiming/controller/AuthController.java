package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.requesta.TokenReqDto;
import com.peoplein.moiming.security.token.JwtParams;
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
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        return new ResponseEntity<>(ResponseBodyDto.createResponse(1, "회원 생성 성공", transmit.get(authService.KEY_RESPONSE_DATA)), HttpStatus.CREATED);

    }

    /*
     Refresh Token 재발급 요청
     */
    @PostMapping("/token")
    public ResponseEntity<?> reissueToken(@RequestBody @Valid TokenReqDto requestDto, BindingResult br, HttpServletResponse response) {

        Map<String, String> responseData = authService.reissueToken(requestDto);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        return ResponseEntity.ok(ResponseBodyDto.createResponse(1, "재발급 성공", responseData));

    }
}