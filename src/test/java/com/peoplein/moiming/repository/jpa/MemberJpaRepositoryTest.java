package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.BaseTest;
import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MemberInfo;
import com.peoplein.moiming.domain.MemberRoleLinker;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.MemberRoleLinkerRepository;
import com.peoplein.moiming.repository.RoleRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
public class MemberJpaRepositoryTest extends BaseTest{

    @Autowired
    MemberRepository repository;
    @Autowired
    MemberRoleLinkerRepository memberRoleLinkerRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    EntityManager em;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private Member member;
    private MemberInfo memberInfo;


    @BeforeEach
    void initInstance() {
        TestUtils.truncateAllTable(jdbcTemplate);

        member = TestUtils.initMemberAndMemberInfo();
        memberInfo = member.getMemberInfo();
        MemberRoleLinker memberRoleLinker = member.getRoles().get(0);
        memberRoleLinkerRepository.save(memberRoleLinker);
        roleRepository.save(memberRoleLinker.getRole());
    }


    @Test
    @DisplayName("성공 @ 멤버 저장")
    @Rollback(value = false)
    void testSaveMember() {

        Long memberId = repository.save(member);

        assertEquals(memberId, member.getId());
    }

    @Test
    @DisplayName("")
    void testFindMemberById() {

        repository.save(member);
        em.flush();
        em.clear();

        Member findMember = repository.findMemberById(member.getId());

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getMemberInfo().getId()).isEqualTo(memberInfo.getId());
        assertThat(findMember.getPassword()).isEqualTo(member.getPassword());
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getRoles().get(0).getId()).isEqualTo(member.getRoles().get(0).getId());
    }

    @Test
    @DisplayName("")
    void testFindMemberByUid() {

        Long saveMemberId = repository.save(member);
        em.flush();
        em.clear();

        Member findMember = repository.findMemberByUid(member.getUid());

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getMemberInfo().getId()).isEqualTo(memberInfo.getId());
        assertThat(findMember.getPassword()).isEqualTo(member.getPassword());
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getMemberInfo().getId()).isEqualTo(member.getMemberInfo().getId());
        assertThat(findMember.getRoles().get(0).getId()).isEqualTo(member.getRoles().get(0).getId());
    }
}