package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "약관 동의 관련")
@RestController
@RequiredArgsConstructor
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_POLICY_VER + NetworkSetting.API_POLICY)
public class PolicyAgreeController {

    // 동의는 회원가입과 동시에 진행된다. Policy Service 에 한함
    // 삭제 또한 회원탈퇴에서 진행되려나? > 여기에서 받을 필요는 없을 듯

    // 수정만 따로 받는다
    @PatchMapping("/update")
    public String updatePolicyAgree() {

        return "";
    }
}
