package com.peoplein.moiming.model.dto.auth;

import com.peoplein.moiming.model.dto.request.PolicyAgreeRequestDto;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSigninRequestDto {

    private String uid;
    private String password;
    private String email;
    private String fcmToken;

    // 약관 동의 항목들도 같이 요청을 보낸다
    // 회원가입 요청시에는 true, false 인지 정확하게 보내져야 한다
    private List<PolicyAgreeRequestDto> policyAgreeList = new ArrayList<>();

}
