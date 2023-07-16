package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.request.PolicyAgreeRequestDto;
import com.peoplein.moiming.model.dto.response.PolicyAgreeResponseDto;
import com.peoplein.moiming.service.PolicyAgreeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "약관 동의 관련")
@RestController
@RequiredArgsConstructor
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_POLICY_VER + NetworkSetting.API_POLICY)
public class PolicyAgreeController {

    private final PolicyAgreeService policyAgreeService;

    // 동의는 회원가입과 동시에 진행된다. Policy Service 에 한함
    // 삭제 또한 회원탈퇴에서 진행되려나? > 여기에서 받을 필요는 없을 듯

    // TODO: 플로우 확인 필요
    // 약관 내용이 수정되는 건은 마케팅 정보 정도일 것
    // 삭제되는 내용은 아마 회원 탈퇴 이외에는 없을 것. 마케팅을 동의했다가 거절하는 것은 삭제가 아니라 변경할 것이기 때문
    // 삭제는 요청이 Controller 를 통해 오지는 않을 것 - 삭제 Flow 에 추가되는 정도일 것

    // 일단 @RequestBody  는 List 로 Data 받는 형태 채택
    // 추후 중간 객체 두는게 더 나을 것 같으면 (유지 보수 측면 데이터 추가 유리) 그 때 수정하되, 크게 추가될 데이터가 있을 것 같지는 않아서 이 방식 유지
    @PatchMapping("/update")
    public ResponseModel<PolicyAgreeResponseDto> updatePolicyAgree(@RequestBody List<PolicyAgreeRequestDto> policyAgreeList) {
        // 수정 요청을 보낸 사람의 약관 내용 중 요청한 내용들을 수정해준다
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PolicyAgreeResponseDto responseModel = policyAgreeService.updatePolicyAgree(curMember, policyAgreeList);
        return ResponseModel.createResponse(responseModel);
    }
}
