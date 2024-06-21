package com.peoplein.moiming.controller;

import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.response.PolicyAgreeRespDto;
import com.peoplein.moiming.security.auth.model.SecurityMember;
import com.peoplein.moiming.service.PolicyAgreeService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.request.PolicyAgreeReqDto.*;
import static com.peoplein.moiming.model.dto.response.PolicyAgreeRespDto.*;

@Api(tags = "약관 조회 & 변경 관련")
@RestController
@RequiredArgsConstructor
public class PolicyAgreeController {

    private final PolicyAgreeService policyAgreeService;

    // 동의는 회원가입과 동시에 진행된다. Policy Service 에 한함
    // 삭제 또한 회원탈퇴에서 진행되려나? > 여기에서 받을 필요는 없을 듯
    @ApiOperation("약관 조회 - 약관들의 동의 현황에 대해서 전달한다 (자동으로 선택에 대해서 조회한다. 만약 모두 조회할 use case 있을 시 optionalOnly=false Query Param 추가전달)")
    @ApiResponses({
            @ApiResponse(code = 200, message = "약관 조회 성공", response = PolicyAgreeViewRespDto.class),
            @ApiResponse(code = 400, message = "약관 조회 실패, ERR MSG 확인")
    })
    @GetMapping(PATH_MEMBER_POLICY_VIEW)
    public ResponseEntity<?> getPolicyAgree(@AuthenticationPrincipal @ApiIgnore SecurityMember principal,
                                                    @RequestParam(required = false, defaultValue = "true") boolean optionalOnly) {

        List<PolicyAgree> optionalPolicyAgree = policyAgreeService.getPolicyAgree(principal.getMember(), optionalOnly);
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "약관 조회 성공", new PolicyAgreeViewRespDto(optionalPolicyAgree)));

    }


    // 약관 내용이 수정되는 건은 마케팅 정보 정도일 것
    @ApiOperation("선택 약관 수정 - Policy Type 이 정해져 있으므로 변경되는 사항들에 대해 하나씩 요청")
    @ApiResponses({
            @ApiResponse(code = 200, message = "약관 수정 성공", response = PolicyAgreeUpdateRespDto.class),
            @ApiResponse(code = 400, message = "약관 수정 실패, ERR MSG 확인")
    })
    @PatchMapping(PATH_MEMBER_POLICY_UPDATE)
    public ResponseEntity<?> updatePolicyAgree(@RequestBody @Valid PolicyAgreeUpdateReqDto requestDto
            , BindingResult br
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        PolicyAgree updatedPolicy = policyAgreeService.updatePolicyAgree(principal.getMember(), requestDto);
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "약관의 수정 성공", new PolicyAgreeUpdateRespDto(updatedPolicy)));
    }

}
