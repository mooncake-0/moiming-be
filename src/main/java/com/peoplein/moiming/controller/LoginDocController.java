package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.model.ErrorResponse;
import com.peoplein.moiming.model.dto.auth.MemberLoginDto;
import com.peoplein.moiming.model.dto.response.MemberResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/*
 SWAGGER 명시를 위해서 등재되어 있는 Controller (실제로 동작하지 않는다)
 Login 관련은 Security Filter 단에서 모두 처리가 된다
 */
@RestController
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_AUTH_VER + NetworkSetting.API_AUTH)
@Tag(name = "로그인")
public class LoginDocController {


    @Operation(description = "로그인 요청 및 응답")
    @RequestBody(description = "로그인 요청 방식입니다",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MemberLoginDto.class))
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공",
                            headers = {
                                    @Header(name = "ACCESS_TOKEN", description = "로그인 성공시 발급되는 액세스 토큰이며, 유효기간 30분입니다"),
                                    @Header(name = "REFRESH_TOKEN", description = "로그인 성공시 발급되는 리프레스 토큰이며, 유효기간 2주입니다")
                            },
                            content = {
                                    @Content(mediaType = "application/json", schema = @Schema(implementation = MemberResponseDto.class))
                            }),
                    @ApiResponse(responseCode = "400", description = "UID 혹은 PW 공란",
                            content = {
                                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
                            }),
                    @ApiResponse(responseCode = "401", description = "비밀번호를 잘못 입력 혹은 휴면 계정 (errorCode 로 구분)",
                            content = {
                                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
                            }),
                    @ApiResponse(responseCode = "500", description = "알 수 없는 에러 발생",
                            content = {
                                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
                            })
            }
    )
    @PostMapping("/login")
    public void processLogin() {

    }
}
