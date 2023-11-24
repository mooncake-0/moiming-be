package com.peoplein.moiming.controller;

import com.peoplein.moiming.domain.MoimCategoryLinker;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.request.MoimReqDto;
import com.peoplein.moiming.model.dto.response.TokenRespDto;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.service.MoimService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;
import static com.peoplein.moiming.model.dto.response.MoimRespDto.*;

@Api(tags = "모임 관련")
@RestController
@RequiredArgsConstructor
public class MoimController {

    private final MoimService moimService;

    @ApiOperation("모임 생성")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "모임 생성 성공", response = MoimCreateRespDto.class),
            @ApiResponse(code = 400, message = "모임 생성 실패, ERR MSG 확인")
    })
    @PostMapping(PATH_MOIM_CREATE)
    public ResponseEntity<?> createMoim(@RequestBody @Valid MoimCreateReqDto requestDto
            , BindingResult br
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        Moim moimOut = moimService.createMoim(requestDto, principal.getMember());
        MoimCreateRespDto respDto = new MoimCreateRespDto(moimOut);
        return new ResponseEntity<>(ResponseBodyDto.createResponse(1, "모임 생성 성공", respDto), HttpStatus.CREATED);
    }




    @ApiOperation("모임 일반 조회 - 특정 유저의 모든 모임 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "유저 모든 모임 조회 성공", response = MoimViewRespDto.class),
            @ApiResponse(code = 400, message = "유저 모든 모임 조회 실패, ERR MSG 확인")
    })
    @GetMapping(PATH_MOIM_GET_VIEW)
    public ResponseEntity<?> getMemberMoims(@AuthenticationPrincipal @ApiIgnore SecurityMember principal) {
        List<MoimViewRespDto> responseData = moimService.getMemberMoims(principal.getMember());
        return ResponseEntity.ok(ResponseBodyDto.createResponse(1, "조회 성공", responseData));
    }




    @ApiOperation("모임 세부 조회 - 특정 모임 전체 정보 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @GetMapping(PATH_MOIM_GET_DETAIL)
    public String getMoim() {
        return "";
    }




    @ApiOperation("모임 정보 수정")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "모임 정보 수정 성공", response = MoimViewRespDto.class),
            @ApiResponse(code = 400, message = "모임 정보 수정 실패, ERR MSG 확인")
    })
    @PatchMapping(PATH_MOIM_UPDATE)
    public ResponseEntity<?> updateMoim(@RequestBody @Valid MoimUpdateReqDto requestDto
            , BindingResult br
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        Moim moimOut = moimService.updateMoim(requestDto, principal.getMember());
        MoimUpdateRespDto respDto = new MoimUpdateRespDto(moimOut);
        return ResponseEntity.ok(ResponseBodyDto.createResponse(1, "모임 정보 수정 성공", respDto));

    }



    @ApiOperation("모임 삭제")
    @DeleteMapping(PATH_MOIM_DELETE)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    public String deleteMoim() {
        return "";
    }

}
