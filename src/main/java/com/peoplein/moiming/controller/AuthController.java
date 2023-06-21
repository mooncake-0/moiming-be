package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.exception.DuplicateAuthValueException;
import com.peoplein.moiming.model.ErrorResponse;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.auth.*;
import com.peoplein.moiming.model.dto.response.MemberResponseDto;
import com.peoplein.moiming.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Tag(name = "Auth 관련")
@RequiredArgsConstructor
@RestController
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_AUTH_VER + NetworkSetting.API_AUTH)
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "중복 ID 확인 요청 (적용성 검토 중)")
    @GetMapping("/uidAvailable/{uid}")
    private ResponseModel<String> checkUidAvailable(@PathVariable String uid) {

        if (authService.checkUidAvailable(uid)) {
            return ResponseModel.createResponse("OK");
        } else {
            throw new DuplicateAuthValueException("[" + uid + "]" + "는 이미 존재하는 ID 입니다", "AS002");
        }
    }

    /*
     회원가입 요청 수신
     */
    @Operation(description = "회원가입 요청 형식 및 응답 (약관 동의 항목들도 BODY에 한번에 전달)", summary = "회원가입 요청")
    @PostMapping("/signin")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "회원 가입 성공",
                            headers = {
                                    @Header(name = "ACCESS_TOKEN", description = "로그인 성공시 발급되는 액세스 토큰이며, 유효기간 30분입니다"),
                                    @Header(name = "REFRESH_TOKEN", description = "로그인 성공시 발급되는 리프레스 토큰이며, 유효기간 2주입니다")
                            },
                            content = {
                                    @Content(mediaType = "application/json", schema = @Schema(implementation = MemberResponseDto.class))
                            }),
                    @ApiResponse(responseCode = "400", description = "ID / PW / Email 공란",
                            content = {
                                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
                            }),
                    @ApiResponse(responseCode = "500", description = "존재하는 ID or Email 중복, 혹은 그 외(errorCode 로 구분)",
                            content = {
                                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
                            }),
            }
    )
    private ResponseModel<MemberResponseDto> signinMember(@RequestBody MemberSigninRequestDto requestDto, HttpServletResponse response) {
        MemberResponseDto dto = authService.signin(requestDto, response);
        return ResponseModel.createResponse(dto);
    }


    // TODO:: SMS Verifiy 관련된건 해당 Domain 으로 요청을 보낸다
    //        실제 Id 찾기, Pw 찾기, Pw 변경 자체에 대한 요청을 Auth Domain 에서 진행한다

}
