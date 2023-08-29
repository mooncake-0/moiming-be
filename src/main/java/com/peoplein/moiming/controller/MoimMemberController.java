package com.peoplein.moiming.controller;

import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.service.MoimMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // 3.

    // 4. 모임 나가기 - IBW 전환
    //    강퇴하기 (MANAGER 권한) - IBF 전환, inactiveReason 기입
    //    스스로 강퇴 불가

    // 5. 운영진 임명하기 (권한으로 부여)

}
