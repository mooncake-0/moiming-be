package com.peoplein.moiming.model.dto.auth;

import com.peoplein.moiming.model.dto.request.PolicyAgreeRequestDto;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberSigninRequestDto {

    private String uid;
    private String password;
    private String email;
    private String fcmToken;

    // 약관 동의 항목들도 같이 요청을 보낸다
    private List<PolicyAgreeRequestDto> policyAgreeList;

}
