package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MoimPostJpaRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired
    MoimPostRepository moimPostRepository;

    @Autowired
    MemberRoleLinkerRepository memberRoleLinkerRepository;
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    MoimRepository moimRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;
    Moim moim;

    Member member;
    MoimPost moimPost;

    @BeforeEach
    void initInstance() {
        TestUtils.truncateAllTable(jdbcTemplate);

        moim = TestUtils.initMoimAndRuleJoin();

        member = TestUtils.initMemberAndMemberInfo();
        memberRepository.save(member);

        List<MemberRoleLinker> roles = member.getRoles();
        roles.forEach(memberRoleLinker -> {
            roleRepository.save(memberRoleLinker.getRole());
            memberRoleLinkerRepository.save(memberRoleLinker);
        });

        moimPost = TestUtils.initMoimPost(moim, member);
        moimRepository.save(moim);
    }

    @Test
    void saveTest() {

        moimRepository.save(moim);
        em.flush();
        em.clear();

        Long saveMoim = moimPostRepository.save(moimPost);

        assertThat(saveMoim).isNotNull();
    }

    @Test
    void findByIdTest() {

        moimRepository.save(moim);
        Long postId = moimPostRepository.save(moimPost);
        em.flush();
        em.clear();

        MoimPost findPost = moimPostRepository.findById(postId);

        assertThat(findPost.getId()).isEqualTo(moimPost.getId());
    }

    @Test
    void findWithMemberByIdTest() {

        moimRepository.save(moim);
        Long postId = moimPostRepository.save(moimPost);
        em.flush();
        em.clear();

        MoimPost findPost = moimPostRepository.findWithMemberById(postId);

        assertThat(findPost.getId()).isEqualTo(moimPost.getId());
        assertThat(findPost.getMember().getId()).isEqualTo(member.getId());
    }

    @Test
    void findWithMoimAndMemberInfoByIdTest() {

        moimRepository.save(moim);
        Long postId = moimPostRepository.save(moimPost);
        em.flush();
        em.clear();

        MoimPost findPost = moimPostRepository.findWithMoimAndMemberInfoById(postId);

        assertThat(findPost.getId()).isEqualTo(moimPost.getId());
        assertThat(findPost.getMember().getId()).isEqualTo(member.getId());
        assertThat(findPost.getMember().getMemberInfo().getId()).isEqualTo(member.getMemberInfo().getId());
    }


    @Test
    void findWithMoimAndMemberByIdTest() {

        moimRepository.save(moim);
        Long postId = moimPostRepository.save(moimPost);
        em.flush();
        em.clear();

        MoimPost findPost = moimPostRepository.findWithMoimAndMemberById(postId);

        assertThat(findPost.getId()).isEqualTo(moimPost.getId());
        assertThat(findPost.getMoim().getId()).isEqualTo(moim.getId());
    }

    @Test
    void findWithMemberInfoByMoimIdTest() {

        Long moimId = moimRepository.save(moim);
        Long postId = moimPostRepository.save(moimPost);
        em.flush();
        em.clear();

        List<MoimPost> findMoimPostList = moimPostRepository.findWithMemberInfoByMoimId(moimId);

        assertThat(findMoimPostList.size()).isEqualTo(1);
        assertThat(findMoimPostList.get(0).getMoim().getId()).isEqualTo(moimId);
        assertThat(findMoimPostList.get(0).getId()).isEqualTo(postId);
    }

    @Test
    void removeTest() {

        moimRepository.save(moim);
        Long postId = moimPostRepository.save(moimPost);
        em.flush();
        em.clear();
        MoimPost findMoimPost = moimPostRepository.findById(postId);

        moimPostRepository.remove(findMoimPost);
        em.flush();
        em.clear();

        MoimPost removedMoim = moimPostRepository.findById(moimPost.getId());
        assertThat(removedMoim).isNull();
    }
}