package com.peoplein.moiming.dev_support;


import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.model.dto.auth.MemberSigninRequestDto;
import com.peoplein.moiming.model.dto.request_b.MemberReqDto;
import org.springframework.context.annotation.Profile;

import static com.peoplein.moiming.dev_support.TestModelParams.*;
import static com.peoplein.moiming.model.dto.request_b.MemberReqDto.*;

import java.time.LocalDate;

/*
 MOCK - id 를 직접 지정해서 모킹한 모델 (ID Verifying 까지를 위함)
 */
@Profile("dev")
public class TestModelCreator {

    protected MemberSignInReqDto makeTestSigninRequestDto() { // 모델들 추가되면 그 때 분할
        return new MemberSignInReqDto(memberEmail, password, memberName, memberPhone, memberGender, memberBirth, fcmToken);
    }

    protected Member makeMockMember() {
        return null;
    }

    protected Role makeMockRole(Long id, RoleType roleType) {
        Role testRole = new Role();
        testRole.setId(id);
        testRole.setRoleDesc("목업");
        testRole.setRoleType(roleType);
        return testRole;
    }

}