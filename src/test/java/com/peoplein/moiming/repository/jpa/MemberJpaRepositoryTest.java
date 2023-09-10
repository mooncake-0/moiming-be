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
import java.util.Optional;

import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Import({RepositoryTestConfiguration.class, MemberJpaRepository.class})
@ActiveProfiles("test")
@DataJpaTest
public class MemberJpaRepositoryTest extends TestObjectCreator {

    @Autowired
    private MemberRepository memberRepository;


    @Autowired
    private EntityManager em;

    @BeforeEach
    void be() {

        // 1번 유저 주입
        Role testRole = makeTestRole(RoleType.USER);
        em.persist(testRole);

        Member member1 = makeTestMember(memberEmail, "01023456789", memberName, testRole);
        member1.changeNickname(nickname);
        memberRepository.save(member1);

        // 2번 유저 주입
        Member member2 = makeTestMember("hello@abc.com", memberPhone, memberName, testRole);
        member2.changeNickname(nickname + "1");
        memberRepository.save(member2);

        // Data Jpa 아님
        em.flush();
        em.clear();

    }

    @Test
    void findMembersByEmailOrPhone_shouldReturnEmptyList_whenNotFound() throws Exception {
        //given
        String notRegisteredEmail = "not@registered.com";
        String notRegisteredPhone = "01000000000";

        //when
        List<Member> members = memberRepository.findMembersByEmailOrPhone(notRegisteredEmail, notRegisteredPhone);

        //then
        assertTrue(members.isEmpty());
    }


    @Test
    void findMembersByEmailOrPhone_shouldReturnList_whenEmailFound() throws Exception {
        // given
        String notRegisteredPhone = "01000000000";

        // when
        List<Member> members = memberRepository.findMembersByEmailOrPhone(memberEmail, notRegisteredPhone);

        // then
        assertFalse(members.isEmpty());
    }

    @Test
    void findMembersByEmailOrPhone_shouldReturnList_whenPhoneFound() throws Exception {
        // given
        String notRegisteredEmail = "not@registered.com";

        // when
        List<Member> members = memberRepository.findMembersByEmailOrPhone(notRegisteredEmail, memberPhone);

        // then
        assertFalse(members.isEmpty());
    }


    @Test
    void findByNickname_shouldReturn_whenFound() {

        // given
        String findingNickname = nickname;

        // when
        Optional<Member> memberOp = memberRepository.findByNickname(findingNickname);

        // then
        assertTrue(memberOp.isPresent());
        assertThat(memberOp.get().getMemberEmail()).isEqualTo(memberEmail);

    }

    @Test
    void findByNickname_shouldReturnEmpty_whenNotFound() {

        // given
        String findingNickname = nickname + "2";

        // when
        Optional<Member> memberOp = memberRepository.findByNickname(findingNickname);

        // then
        assertTrue(memberOp.isEmpty());

    }
}