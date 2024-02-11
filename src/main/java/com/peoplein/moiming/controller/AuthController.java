package com.peoplein.moiming.controller;

import com.peoplein.moiming.model.ResponseBodyDto;
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
import static com.peoplein.moiming.model.dto.request.AuthReqDto.*;
import static com.peoplein.moiming.model.dto.response.AuthRespDto.*;

@Api(tags = "회원 & 회원 인증 관련 (토큰 불필요)")
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;


    @ApiOperation("이메일 중복 확인")
    @GetMapping(PATH_AUTH_EMAIL_AVAILABLE)
    public ResponseEntity<?> checkEmailAvailable(@PathVariable String email) {
        if (authService.checkEmailAvailable(email)) {
            return ResponseEntity.ok().body(ResponseBodyDto.createResponse("1", "사용 가능", null));
        }
        return ResponseEntity.ok().body(ResponseBodyDto.createResponse("-1", "사용 불가", null));
    }


    @ApiOperation("최종 회원 가입")
    @ApiResponses({
            @ApiResponse(code = 201, message = "회원 가입 성공", response = AuthSignInRespDto.class,
                    responseHeaders = {@ResponseHeader(name = "Authorization", description = "Bearer {JWT ACCESS TOKEN}", response = String.class)}),
            @ApiResponse(code = 400, message = "회원 가입 실패, ERR MSG 확인")
    })
    @PostMapping(PATH_AUTH_SIGN_IN)
    public ResponseEntity<?> signInMember(@RequestBody @Valid AuthSignInReqDto requestDto, BindingResult br
            , HttpServletResponse response) {

        AuthSignInRespDto responseDto = authService.signIn(requestDto);
        return new ResponseEntity<>(ResponseBodyDto.createResponse("1", "회원 생성 성공", responseDto), HttpStatus.CREATED);

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
    public ResponseEntity<?> reissueToken(@RequestBody @Valid AuthTokenReqDto requestDto, BindingResult br) {

        TokenRespDto tokenRespDto = authService.reissueToken(requestDto);
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "재발급 성공", tokenRespDto));

    }


    @ApiOperation("이메일 확인 요청 - SMS 인증 후 인증 번호 및 ID 전달 필요")
    @ApiResponses({
            @ApiResponse(code = 200, message = "이메일 확인 성공", response = AuthFindIdRespDto.class),
            @ApiResponse(code = 400, message = "이메일 확인 실패, ERR MSG 확인")
    })
    @PostMapping(PATH_AUTH_FIND_MEMBER_EMAIL)
    public ResponseEntity<?> findMemberEmail(@RequestBody @Valid AuthFindIdReqDto requestDto
            , BindingResult br) {

        String maskedEmail = authService.findMemberEmail(requestDto);
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "이메일 확인 성공", new AuthFindIdRespDto(maskedEmail)));

    }


    @ApiOperation("비밀번호 재설정 인증 요청 - SMS 인증 후 인증 번호 및 ID 전달 필요")
    @ApiResponses({
            @ApiResponse(code = 200, message = "비밀번호 재설정 인증 성공"),
            @ApiResponse(code = 400, message = "비밀번호 재설정 인증 실패, ERR MSG 확인")
    })
    @PostMapping(PATH_AUTH_RESET_PW_CONFIRM)
    public ResponseEntity<?> confirmResetPassword(@RequestBody @Valid AuthResetPwConfirmReqDto requestDto
            , BindingResult br) {

        authService.confirmResetPassword(requestDto);
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "비밀번호 재설정 인증 성공", null));

    }


    @ApiOperation("비밀번호 재설정 요청 - SMS 인증 후 ID 전달 필요")
    @ApiResponses({
            @ApiResponse(code = 200, message = "비밀번호 재설정 성공"),
            @ApiResponse(code = 400, message = "비밀번호 재설정 실패, ERR MSG 확인")
    })
    @PatchMapping(PATH_AUTH_RESET_PW)
    public ResponseEntity<?> resetPassword(@RequestBody @Valid AuthResetPwReqDto requestDto
            , BindingResult br) {

        authService.resetPassword(requestDto);
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "비밀번호 재설정 성공", null));

    }
}