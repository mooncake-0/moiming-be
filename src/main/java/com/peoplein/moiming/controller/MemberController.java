package com.peoplein.moiming.controller;

import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.security.token.JwtParams;
import com.peoplein.moiming.service.MemberService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;

@Api(tags = "회원 관련")
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @ApiOperation("로그아웃")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "로그아웃 성공"),
            @ApiResponse(code = 400, message = "로그아웃 실패, ERR MSG 확인")
    })
    @PatchMapping(PATH_MEMBER_LOGOUT)
    public ResponseEntity<?> logout(HttpServletRequest request,
            @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        String accessToken = getJwtFromRequest(request);
        memberService.logout(accessToken, principal.getMember());
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "로그아웃 성공, Client 저장 토큰 초기화 필수", null));
    }


    @ApiOperation("회원 탈퇴")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "회원 탈퇴 성공"),
            @ApiResponse(code = 400, message = "회원 탈퇴 실패, ERR MSG 확인")
    })
    @PostMapping(PATH_MEMBER_DELETE)
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {
        return null;
    }

    /*
     요청 헤더에 Authorization Bearer Token (ACCESS_TOKEN) 이 존재하는지 확인하고 반환한다
     > 이미 ContextHolder 에 존재하는 Member 와 동기화되었음을 보증한다
     */
    private String getJwtFromRequest(HttpServletRequest request) {

        String authorizationValue = request.getHeader(JwtParams.HEADER);

        if (StringUtils.hasText(authorizationValue) && authorizationValue.startsWith(JwtParams.PREFIX)) {
            return authorizationValue.replace(JwtParams.PREFIX, "");
        }else{ // 발생할리가 없음
            throw new MoimingApiException(MEMBER_LOGOUT_AT_NOT_FOUND);
        }
    }
}
