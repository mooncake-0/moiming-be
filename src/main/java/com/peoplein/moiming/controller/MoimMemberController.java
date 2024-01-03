package com.peoplein.moiming.controller;

import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.security.auth.model.SecurityMember;
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
public class MoimMemberController {

    private final MoimMemberService moimMemberService;


    @ApiOperation("모든 모임원 일반 조회하기")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "모든 모임원 일반 조회 성공"),
            @ApiResponse(code = 400, message = "모든 모임원 일반 조회 실패, ERR MSG 확인")
    })
    @GetMapping(PATH_MOIM_MEMBER_GET_VIEW)
    public ResponseEntity<?> getActiveMoimMembers(@PathVariable(value = "moimId", required = true) Long moimId,
                                            @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        List<MoimMember> moimMembers = moimMemberService.getActiveMoimMembers(moimId, principal.getMember());
        List<ActiveMoimMemberRespDto> responseData = moimMembers.stream().map(ActiveMoimMemberRespDto::new).collect(Collectors.toList());

        // 뭘 보내줘야 할까?
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "모든 모임원 일반 조회 성공", responseData));
    }



    @ApiOperation("모임 가입하기")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "모임 가입 성공"),
            @ApiResponse(code = 400, message = "모임 가입 실패, ERR MSG 확인")
    })
    @PostMapping(PATH_MOIM_MEMBER_JOIN)
    public ResponseEntity<?> joinMoim(@RequestBody @Valid MoimMemberJoinReqDto requestDto,
                                      BindingResult br,
                                      @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        moimMemberService.joinMoim(requestDto, principal.getMember());

        // 가입 성공 응답만 보내준다
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "가입 성공", null));
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
    @PostMapping(PATH_MOIM_MEMBER_LEAVE)
    public ResponseEntity<?> leaveMoim(@RequestBody @Valid MoimMemberLeaveReqDto requestDto,
                                       BindingResult br,
                                       @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        moimMemberService.leaveMoim(requestDto, principal.getMember());
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "모임 나가기 성공", null));
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
    @PostMapping(PATH_MOIM_MEMBER_EXPEL)
    public ResponseEntity<?> expelMember(@RequestBody @Valid MoimMemberExpelReqDto requestDto,
                                         BindingResult br,
                                         @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        moimMemberService.expelMember(requestDto, principal.getMember());
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "유저 강퇴 성공", null));
    }

}