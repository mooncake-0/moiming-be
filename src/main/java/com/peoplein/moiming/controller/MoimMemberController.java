package com.peoplein.moiming.controller;

import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.service.MoimMemberService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.peoplein.moiming.config.AppUrlPath.*;


@Api(tags = "모임 내 멤버 관리 관련")
@RestController
@RequiredArgsConstructor
@RequestMapping(API_SERVER + API_MOIM_VER + API_MOIM_MEMBER)
public class MoimMemberController {

    private final MoimMemberService moimMemberService;

    // 1. 모임 내 모든 회원 및 상태 조회 (조회의 종류를 파악해보면 좋을 듯)


    // 2. 가입 요청 (Rule Join 판별 - Front 에서 걸러줄테지만) // 해당 Id 와 moimId 의 요청이 있다 (해당 대상을 필터링 할 것이므로, Query Parameter)
    @ApiOperation("모임 가입하기")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @GetMapping("/join")
    public String joinMoim(@PathVariable("moimId") Long moimId,
                           @AuthenticationPrincipal SecurityMember principal) {

        moimMemberService.joinMoim(moimId, principal.getMember());

        return "";
    }

    //3. 모임 나가기 - IBW 전환
    @ApiOperation("모임 나가기")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
//            @ApiResponse(code = 200, message = "모임 생성 성공", response = MoimCreateRespDto.class),
//            @ApiResponse(code = 400, message = "모임 생성 실패, ERR MSG 확인")
    })
    @PostMapping("/leave")
    public String leaveMoim() {
        return "";
    }


    //4. 강퇴하기 (MANAGER 권한) - IBF 전환, inactiveReason 기입
    @ApiOperation("강퇴하기")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
//            @ApiResponse(code = 200, message = "모임 생성 성공", response = MoimCreateRespDto.class),
//            @ApiResponse(code = 400, message = "모임 생성 실패, ERR MSG 확인")
    })
    @PostMapping("/expel")
    public String expelMember() {
        return "";
    }

    // 5. 운영진 임명하기 (권한으로 부여)

}
