package com.peoplein.moiming.controller;

import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.request.MemberInfoReqDto;
import com.peoplein.moiming.security.auth.model.SecurityMember;
import com.peoplein.moiming.service.MemberInfoService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.peoplein.moiming.config.AppUrlPath.PATH_MEMBER_CREATE_MEMBER_INFO;
import static com.peoplein.moiming.config.AppUrlPath.PATH_MEMBER_LOGOUT;
import static com.peoplein.moiming.model.dto.request.MemberInfoReqDto.*;

@Api(tags = "회원 개인 정보 관련")
@RestController
@RequiredArgsConstructor
public class MemberInfoController {

    private final MemberInfoService memberInfoService;

    @ApiOperation("개인 정보 등록 - 응답 없음, 성공 여부만 확인")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "개인 정보 등록 성공"),
            @ApiResponse(code = 400, message = "개인 정보 등록 실패, ERR MSG 확인")
    })
    @PostMapping(PATH_MEMBER_CREATE_MEMBER_INFO)
    public ResponseEntity<?> registerMemberInfo(@RequestBody @Valid MemberInfoCreateReqDto requestDto,
                                                BindingResult br,
                                                @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        memberInfoService.registerMemberInfo(requestDto, principal.getMember());
        return new ResponseEntity<>(ResponseBodyDto.createResponse("1", "개인 정보 등록 성공", null), HttpStatus.CREATED);
    }
}
