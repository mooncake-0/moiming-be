package com.peoplein.moiming.controller;

import com.peoplein.moiming.config.AppUrlPath;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.service.PolicyAgreeService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.request.PolicyAgreeReqDto.*;

@Api(tags = "약관 조회 & 변경 관련")
@RestController
@RequiredArgsConstructor
@RequestMapping(API_SERVER + API_POLICY_VER + API_POLICY)
public class PolicyAgreeController {

    private final PolicyAgreeService policyAgreeService;

    // 동의는 회원가입과 동시에 진행된다. Policy Service 에 한함
    // 삭제 또한 회원탈퇴에서 진행되려나? > 여기에서 받을 필요는 없을 듯


    // 약관 내용이 수정되는 건은 마케팅 정보 정도일 것
    @ApiOperation("선택 약관 수정")
    @ApiResponses({
            @ApiResponse(code = 200, message = "약관 수정 성공"),
            @ApiResponse(code = 400, message = "약관 수정 실패, ERR MSG 확인")
    })
    @PatchMapping("/update")
    public ResponseEntity<?> updatePolicyAgree(@RequestBody @Valid PolicyAgreeUpdateReqDto requestDto
            , BindingResult br
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        policyAgreeService.updatePolicyAgree(principal.getMember(), requestDto.getPolicyDtos());
        return ResponseEntity.ok(ResponseBodyDto.createResponse(1, "요청한 약관들의 동의 여부를 수정하였습니다", null));
    }
}
