package com.peoplein.moiming.support;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static com.peoplein.moiming.support.TestModelParams.*;

public class TestObjectCreator {

    protected Member makeTestMember(String email, String phone, String name, Role role) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode(password);
        return Member.createMember(email, encoded, name, phone, memberGender, memberBirth, fcmToken, role);
    }

    protected Role makeTestRole(RoleType roleType) {
        return new Role(1L, "일반유저", roleType);
    }

}