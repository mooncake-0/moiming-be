package com.peoplein.moiming.controller;

import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.request.MoimMemberReqDto;
import com.peoplein.moiming.model.dto.response.MoimMemberRespDto;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.service.MoimMemberService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.request.MoimMemberReqDto.*;
import static com.peoplein.moiming.model.dto.response.MoimMemberRespDto.*;


@Api(tags = "모임 내 멤버 관리 관련")
@RestController
@RequiredArgsConstructor
@RequestMapping(API_SERVER + API_MOIM_VER + API_MOIM)
public class MoimMemberController {

    private final MoimMemberService moimMemberService;

    // 1. 모임 내 모든 회원 및 상태 조회 (조회의 종류를 파악해보면 좋을 듯) - 제일 간단한 정보로만 조회
    @ApiOperation("모든 모임원 일반 조회하기")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "모든 모임원 일반 조회 성공"),
            @ApiResponse(code = 400, message = "모든 모임원 일반 조회 실패, ERR MSG 확인")
    })
    @GetMapping("/{moimId}" + API_MOIM_MEMBER)
    public ResponseEntity<?> getActiveMoimMembers(@PathVariable(value = "moimId", required = true) Long moimId,
                                            @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        List<MoimMember> moimMembers = moimMemberService.getActiveMoimMembers(moimId, principal.getMember());
        List<ActiveMoimMemberRespDto> responseData = moimMembers.stream().map(ActiveMoimMemberRespDto::new).collect(Collectors.toList());

        // 뭘 보내줘야 할까?
        return ResponseEntity.ok(ResponseBodyDto.createResponse(1, "모든 모임원 일반 조회 성공", responseData));
    }


    @ApiOperation("모임 가입하기")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "모임 가입 성공"),
            @ApiResponse(code = 400, message = "모임 가입 실패, ERR MSG 확인")
    })
    @PostMapping(API_MOIM_MEMBER + "/join")
    public ResponseEntity<?> joinMoim(@RequestBody @Valid MoimMemberJoinReqDto requestDto,
                                      BindingResult br,
                                      @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        moimMemberService.joinMoim(requestDto, principal.getMember());

        // 가입 성공 응답만 보내준다
        return ResponseEntity.ok(ResponseBodyDto.createResponse(1, "가입 성공", null));
    }


    //3. 모임 나가기 - IBW 전환
    @ApiOperation("모임 나가기")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "모임 나가기 성공"),
            @ApiResponse(code = 400, message = "모임 나가기 실패, ERR MSG 확인")
    })
    @PostMapping(API_MOIM_MEMBER + "/leave")
    public ResponseEntity<?> leaveMoim(@RequestBody @Valid MoimMemberLeaveReqDto requestDto,
                                       BindingResult br,
                                       @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        moimMemberService.leaveMoim(requestDto, principal.getMember());
        return ResponseEntity.ok(ResponseBodyDto.createResponse(1, "모임 나가기 성공", null));
    }


    //4. 강퇴하기 (MANAGER 권한) - IBF 전환, inactiveReason 기입
    @ApiOperation("강퇴하기")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "유저 강퇴 성공"),
            @ApiResponse(code = 400, message = "유저 강퇴 실패, ERR MSG 확인")
    })
    @PostMapping(API_MOIM_MEMBER + "/expel")
    public ResponseEntity<?> expelMember(@RequestBody @Valid MoimMemberExpelReqDto requestDto,
                                         BindingResult br,
                                         @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        moimMemberService.expelMember(requestDto, principal.getMember());
        return ResponseEntity.ok(ResponseBodyDto.createResponse(1, "유저 강퇴 성공", null));
    }

}