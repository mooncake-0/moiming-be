package com.peoplein.moiming.support;


import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;

import static com.peoplein.moiming.support.TestModelParams.*;
import static com.peoplein.moiming.model.dto.request_b.MemberReqDto.*;

/*
 MOCK - id 를 직접 지정해서 모킹한 모델 (ID Verifying 까지를 위함)
 */
public class TestMockCreator {


    protected MemberSignInReqDto mockSigninRequestDto() { // 모델들 추가되면 그 때 분할
        return new MemberSignInReqDto(memberEmail, password, memberName, memberPhone, memberGender, memberBirth, fcmToken);
    }

    protected Member mockMember() {
        return null;
    }

    protected Role mockRole(Long id, RoleType roleType) {
        Role testRole = new Role();
        testRole.setId(id);
        testRole.setRoleDesc("목업");
        testRole.setRoleType(roleType);
        return testRole;
    }
}