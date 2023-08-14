package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.RoleRepository;
import com.peoplein.moiming.support.RepositoryTestConfiguration;
import com.peoplein.moiming.support.TestObjectCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;


import java.util.List;

import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Import({RepositoryTestConfiguration.class, MemberJpaRepository.class, RoleJpaRepository.class})
@ActiveProfiles("test")
@DataJpaTest
public class MemberJpaRepositoryTest extends TestObjectCreator {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EntityManager em;


    @BeforeEach
    void be() {

        // 1번 유저 주입
        Role testRole = makeTestRole(RoleType.USER);
        roleRepository.save(testRole);

        Member member1 = makeTestMember(memberEmail, "01023456789", memberName, testRole);
        memberRepository.save(member1);

        // 2번 유저 주입
        Member member2 = makeTestMember("hello@abc.com", memberPhone, memberName, testRole);
        memberRepository.save(member2);

        // Data Jpa 아님
        em.flush();
        em.clear();

    }

    @Test
    void findByEmailOrPhone_should_return_empty_list_when_not_found() throws Exception {
        //given
        String notRegisteredEmail = "not@registered.com";
        String notRegisteredPhone = "01000000000";

        //when
        List<Member> members = memberRepository.findByEmailOrPhone(notRegisteredEmail, notRegisteredPhone);

        //then
        assertTrue(members.isEmpty());
    }


    @Test
    void findByEmailOrPhone_should_return_list_when_email_found() throws Exception {
        // given
        String notRegisteredPhone = "01000000000";

        // when
        List<Member> members = memberRepository.findByEmailOrPhone(memberEmail, notRegisteredPhone);

        // then
        assertFalse(members.isEmpty());
    }

    @Test
    void findByEmailOrPhone_should_return_list_when_phone_found() throws Exception {
        // given
        String notRegisteredEmail = "not@registered.com";

        // when
        List<Member> members = memberRepository.findByEmailOrPhone(notRegisteredEmail, memberPhone);

        // then
        assertFalse(members.isEmpty());
    }
}