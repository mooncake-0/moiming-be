package com.peoplein.moiming.controller;

import com.peoplein.moiming.domain.MoimCategoryLinker;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.service.MoimService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;
import static com.peoplein.moiming.model.dto.response.MoimRespDto.*;

@Api(tags = "모임 관련")
@RestController
@RequiredArgsConstructor
@RequestMapping(API_SERVER + API_MOIM_VER + API_MOIM)
public class MoimController {

    private final MoimService moimService;

    @ApiOperation("모임 생성")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @PostMapping("/create")
    public ResponseEntity<?> createMoim(@RequestBody @Valid MoimCreateReqDto requestDto
            , BindingResult br
            , @AuthenticationPrincipal SecurityMember principal) {

        Moim moimOut = moimService.createMoim(requestDto, principal.getMember());
        List<String> categoryNameValues = MoimCategoryLinker.convertLinkersToNameValues(moimOut.getMoimCategoryLinkers());

        MoimCreateRespDto respDto = new MoimCreateRespDto(moimOut, categoryNameValues);
        return new ResponseEntity<>(ResponseBodyDto.createResponse(1, "모임 생성 성공", respDto), HttpStatus.CREATED);
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
