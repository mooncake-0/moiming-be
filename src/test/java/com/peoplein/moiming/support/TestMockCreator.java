package com.peoplein.moiming.support;


import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static com.peoplein.moiming.support.TestModelParams.*;
import static com.peoplein.moiming.model.dto.request_b.MemberReqDto.*;

/*
 MOCK - id 를 직접 지정해서 모킹한 모델 (ID Verifying 까지를 위함)
 */
public class TestMockCreator {


    protected MemberSignInReqDto mockSigninRequestDto() { // 모델들 추가되면 그 때 분할
        return new MemberSignInReqDto(memberEmail, password, memberName, memberPhone, memberGender, memberBirth, fcmToken);
    }

    protected Member mockMember(Long id, String email, String name, String phone, Role role) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode(password);
        Member mockMember = Member.createMember(
                email, encoded, name, phone, memberGender, memberBirth, fcmToken, role
        );
        mockMember.changeMockObjectIdForTest(id, this.getClass().getSimpleName());
        return mockMember;
    }

    protected Role mockRole(Long id, RoleType roleType) {
        Role testRole = new Role();
        testRole.setId(id);
        testRole.setRoleDesc("목업");
        testRole.setRoleType(roleType);
        return testRole;
    }
}