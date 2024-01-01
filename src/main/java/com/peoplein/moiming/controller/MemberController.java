package com.peoplein.moiming.controller;

import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.request.MemberReqDto;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.security.token.JwtParams;
import com.peoplein.moiming.service.MemberService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.MemberReqDto.*;
import static com.peoplein.moiming.model.dto.response.MemberRespDto.*;

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


    // TODO :: 탈퇴회원 테이블에 저장, 해당 테이블은 7일 뒤에 삭제된다
    @ApiOperation("회원 탈퇴")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "회원 탈퇴 성공"),
            @ApiResponse(code = 400, message = "회원 탈퇴 실패, ERR MSG 확인")
    })
    @PostMapping(PATH_MEMBER_DELETE)
    public ResponseEntity<?> delete(@AuthenticationPrincipal @ApiIgnore SecurityMember principal) {
        return null;
    }


    @ApiOperation("기본 회원 정보 조회 (개인정보 제외)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "회원 기본 정보 조회 성공", response = MemberViewRespDto.class),
            @ApiResponse(code = 400, message = "회원 기본 정보 조회 실패, ERR MSG 확인")
    })
    @GetMapping(PATH_MEMBER_GET_VIEW)
    public ResponseEntity<?> getMember(@PathVariable Long memberId,
                                       @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "멤버 조회 성공", new MemberViewRespDto(principal.getMember())));
    }


    @ApiOperation("회원 비밀번호 확인")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "비밀번호 확인 성공"),
            @ApiResponse(code = 400, message = "비밀번호 확인 실패, ERR MSG 확인")
    })
    @PostMapping(PATH_MEMBER_CONFIRM_PW)
    public ResponseEntity<?> confirmPw(@RequestBody @Valid MemberConfirmPwReqDto requestDto,
                                       @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {
        boolean successful = memberService.confirmPw(requestDto.getPassword(), principal.getMember());
        String code = successful ? "1" : "-1";
        String msg = successful ? "비밀번호 일치" : "비밀번호 불일치";
        return ResponseEntity.ok(ResponseBodyDto.createResponse(code, msg, null));
    }


    @ApiOperation("회원 개인 정보 내역 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "개인 정보 내역 확인 성공", response = MemberDetailViewRespDto.class),
            @ApiResponse(code = 400, message = "개인 정보 내역 확인 실패, ERR MSG 확인")
    })
    @GetMapping(PATH_MEMBER_GET_DETAIL_VIEW)
    public ResponseEntity<?> getMemberDetail(@PathVariable Long memberId,
                                             @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {
        // TODO :: 기본적으로 Security 단에서 MemberInfo 도 Fetch Join 한다 -> 별도 Tx 없이 바로 응답 가능
        //         하지만 바람직한가?
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "개인 정보 내역 확인 성공", new MemberDetailViewRespDto(principal.getMember())));
    }


    @ApiOperation("회원 닉네임 변경")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "닉네임 변경 성공", response = MemberChangeNicknameRespDto.class),
            @ApiResponse(code = 400, message = "닉네임 변경 실패, ERR MSG 확인")
    })
    @PatchMapping(PATH_MEMBER_CHANGE_NICKNAME)
    public ResponseEntity<?> changeNickname(@RequestBody @Valid MemberChangeNicknameReqDto requestDto,
                                            @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        memberService.changeNickname(requestDto.getNickname(), principal.getMember());
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "닉네임 변경 성공", new MemberChangeNicknameRespDto(principal.getMember().getNickname())));
    }


    @ApiOperation("회원 비밀번호 변경")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "비밀번호 변경 성공"),
            @ApiResponse(code = 400, message = "비밀번호 변경 실패, ERR MSG 확인")
    })
    @PatchMapping(PATH_MEMBER_CHANGE_PASSWORD)
    public ResponseEntity<?> changePw(@RequestBody @Valid MemberChangePwReqDto requestDto,
                                      @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {
        memberService.changePw(requestDto.getPrePw(), requestDto.getPostPw(), principal.getMember());
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "비밀번호 변경 성공", null));
    }



    /*
     요청 헤더에 Authorization Bearer Token (ACCESS_TOKEN) 이 존재하는지 확인하고 반환한다
     > 이미 ContextHolder 에 존재하는 Member 와 동기화되었음을 보증한다
     */
    private String getJwtFromRequest(HttpServletRequest request) {

        String authorizationValue = request.getHeader(JwtParams.HEADER);

        if (StringUtils.hasText(authorizationValue) && authorizationValue.startsWith(JwtParams.PREFIX)) {
            return authorizationValue.replace(JwtParams.PREFIX, "");
        } else { // 발생할리가 없음
            throw new MoimingApiException(MEMBER_LOGOUT_AT_NOT_FOUND);
        }
    }
}
