package com.peoplein.moiming.controller;

import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.inner.TokenDto;
import com.peoplein.moiming.model.dto.request.TokenReqDto;
import com.peoplein.moiming.model.dto.response.TokenRespDto;
import com.peoplein.moiming.security.token.JwtParams;
import com.peoplein.moiming.service.AuthService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.util.Map;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.request.MemberReqDto.*;
import static com.peoplein.moiming.model.dto.response.MemberRespDto.*;

@Api(tags = "회원 & 회원 인증 관련 (토큰 불필요)")
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;


    @ApiOperation("이메일 중복 확인")
    @GetMapping(PATH_AUTH_EMAIL_AVAILABLE)
    public ResponseEntity<?> checkEmailAvailable(@PathVariable String email) {
        if (authService.checkEmailAvailable(email)) {
            return ResponseEntity.ok().body(ResponseBodyDto.createResponse("1", "사용가능", null));
        }
        return ResponseEntity.ok().body(ResponseBodyDto.createResponse("-1", "사용 불가", null));
    }


    @ApiOperation("최종 회원 가입")
    @ApiResponses({
            @ApiResponse(code = 201, message = "회원 가입 성공", response = MemberSignInRespDto.class,
                    responseHeaders = {@ResponseHeader(name = "Authorization", description = "Bearer {JWT ACCESS TOKEN}", response = String.class)}),
            @ApiResponse(code = 400, message = "회원 가입 실패, ERR MSG 확인")
    })
    @PostMapping(PATH_AUTH_SIGN_IN)
    public ResponseEntity<?> signInMember(@RequestBody @Valid MemberSignInReqDto requestDto, BindingResult br
            , HttpServletResponse response) {

        Map<String, Object> transmit = authService.signIn(requestDto);

        // 응답 준비
        String jwtAccessToken = transmit.get(authService.KEY_ACCESS_TOKEN).toString();
        response.addHeader(JwtParams.HEADER, JwtParams.PREFIX + jwtAccessToken);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        return new ResponseEntity<>(ResponseBodyDto.createResponse("1", "회원 생성 성공", transmit.get(authService.KEY_RESPONSE_DATA)), HttpStatus.CREATED);

    }


    /*
     Refresh Token 재발급 요청
     */
    @ApiOperation("갱신 토큰 - 토큰 재발급 요청")
    @ApiResponses({
            @ApiResponse(code = 200, message = "접근 / 갱신 토큰 재발급 성공", response = TokenRespDto.class,
                    responseHeaders = {@ResponseHeader(name = "Authorization", description = "Bearer {JWT ACCESS TOKEN}", response = String.class)}),
            @ApiResponse(code = 400, message = "회원 가입 실패, ERR MSG 확인")
    })
    @PostMapping(PATH_AUTH_REISSUE_TOKEN)
    public ResponseEntity<?> reissueToken(@RequestBody @Valid TokenReqDto requestDto, BindingResult br, HttpServletResponse response) {

        TokenDto tokenDto = authService.reissueToken(requestDto);
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "재발급 성공", tokenDto));

    }
}