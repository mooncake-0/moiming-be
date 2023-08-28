package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.model.dto.request.MoimReqDto;
import com.peoplein.moiming.security.domain.SecurityMember;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.peoplein.moiming.NetworkSetting.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;

@Api(tags = "모임 관련")
@RestController
@RequiredArgsConstructor
@RequestMapping(API_SERVER + API_MOIM_VER + API_MOIM)
public class MoimController {


    @ApiOperation("모임 생성")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @PostMapping("/create")
    public String createMoim(@RequestBody @Valid MoimCreateReqDto requestDto
            , BindingResult br
            , @AuthenticationPrincipal SecurityMember principal) {


        return "";
    }


    @ApiOperation("모임 일반 조회 - 특정 유저의 모든 모임 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @GetMapping("")
    public String getUserMoims(@PathVariable("memberId") Long memberId,
                               @AuthenticationPrincipal SecurityMember principal) {

        return "";
    }


    @ApiOperation("모임 세부 조회 - 특정 모임 전체 정보 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @GetMapping("/{moimId}")
    public String getMoim() {
        return "";
    }


    @ApiOperation("모임 정보 수정")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @PatchMapping("/{moimId}")
    public String updateMoim() {
        return "";
    }



    @ApiOperation("모임 삭제")
    @DeleteMapping("/{moimId}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    public String deleteMoim() {
        return "";
    }

}
