package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.exception.DuplicateAuthValueException;
import com.peoplein.moiming.model.ErrorResponse;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.auth.MemberSigninRequestDto;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Tag(name = "Auth 관련")
@RequiredArgsConstructor
@RestController
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_AUTH_VER + NetworkSetting.API_AUTH)
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "중복 ID 확인 요청 (적용성 검토 중")
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
    @Operation(description = "회원가입 요청 형식 및 응답", summary = "회원가입 요청")
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


    // ID
    // 이름 / 휴대폰 번호 를 가지고 요청을 보낸다
    // 휴대폰 번호 인증 Table 에서, 이 인증번호가 뭘 위해서인지도 저장해줘야 할듯
    // RANDOM NUMBER 제작 후 찾으려는 대상자, EXPIRED TIME, 인증완료여부 와 함께 DB 에 저장한다
    // 인증번호를 보내는 요청이 오면 다시한번 이름 / 휴대폰번호 > 저장된 인증번호를 조회한다
    // 성공시 인증완료 정보를 업데이트 해준다
    // 매칭시 이메일 정보를 return 한다

    // PW 찾기
    // 이메일 / 휴대폰 번호 를 가지고 요청을 보낸다
    // 위와 동일하게 동작함. Service 단 설계 잘해야할듯
    // 매칭시 임시 비밀번호로 바꿔준다

    // PW 변경
    // 인증된 객체 Member 가 요청하게 된다
    // 이 때 휴대폰 인증을 다시한번 진행하게 되므로 > 이 로직을 다시 진행한다
    // 비밀번호 변경 요청이 날라올 시, 휴대폰 번호 인증 Table 에서 자신과 매핑된 애가 있는지 확인한다
    // 이 때, 매핑된 애중 해다이 있고, 인즈이 완료된 상태면, 입력받은 비밀번호로 변경을 진행해준다 .

    // 휴대폰 번호 인증 Table 관리
    // [인증 완료된 Table] 을 주기적으로 삭제해주는 녀석을 넣으면 좋을듯? 스케줄링?

}
