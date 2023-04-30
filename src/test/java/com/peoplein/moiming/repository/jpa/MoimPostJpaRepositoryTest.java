package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.BaseTest;
import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
public class MoimPostJpaRepositoryTest extends BaseTest {

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

    Moim moim;

    Member member;
    MoimPost moimPost;

    @BeforeEach
    void initInstance() {
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

    @Test
    void findNoticesLatest3ByMoimIdSuccess1Test() {
        // Given :
        Moim moim1 = TestUtils.createMoimOnly("other1");
        Moim moim2 = TestUtils.createMoimOnly("other2");
        Moim moim3 = TestUtils.createMoimOnly("other3");

        MemberMoimLinker linker1 = TestUtils.createLeaderMemberMoimLinker(member, moim1);
        MemberMoimLinker linker2 = TestUtils.createLeaderMemberMoimLinker(member, moim2);
        MemberMoimLinker linker3 = TestUtils.createLeaderMemberMoimLinker(member, moim2);

        saveEntities(member, moim1, moim2, moim3, linker1, linker2, linker3);

        MoimPost moimPost1 = TestUtils.initNoticeMoimPost(moim1, member);
        MoimPost moimPost2 = TestUtils.initNoticeMoimPost(moim1, member);
        MoimPost moimPost3 = TestUtils.initNoticeMoimPost(moim1, member);

        saveEntities(moimPost1, moimPost2, moimPost3);

        MoimPost moimPost4 = TestUtils.initNoticeMoimPost(moim2, member);

        saveEntities(moimPost4);

        MoimPost moimPost7 = TestUtils.initNoticeMoimPost(moim3, member);
        MoimPost moimPost8 = TestUtils.initNoticeMoimPost(moim3, member);
        MoimPost moimPost9 = TestUtils.initNoticeMoimPost(moim3, member);

        saveEntities(moimPost7, moimPost8, moimPost9);


        List<Long> moimIds = List.of(moim1.getId(), moim2.getId(), moim3.getId());

        System.out.println("HERE======================================");
        // When :
        List<MoimPost> result = moimPostRepository.findNoticesLatest3ByMoimIds(moimIds);

        // Then :
        assertThat(result.size()).isEqualTo(3);
        assertThat(result)
                .extracting(MoimPost::getId)
                .contains(moimPost7.getId(), moimPost8.getId(), moimPost9.getId());
    }


    @DisplayName("findNoticesLatest3ByMoimId : moimIds에 포함되지 않은 값이 나오지는 않는지 확인")
    @Test
    void findNoticesLatest3ByMoimIdSuccess2Test() {
        // Given :
        TestUtils.initMemberAndMemberInfo("other-member", "other-member@moimimgn.net");

        Moim moim1 = TestUtils.createMoimOnly("other1");
        Moim moim2 = TestUtils.createMoimOnly("other2");
        Moim moim3 = TestUtils.createMoimOnly("other3");

        MemberMoimLinker linker1 = TestUtils.createLeaderMemberMoimLinker(member, moim1);
        MemberMoimLinker linker2 = TestUtils.createLeaderMemberMoimLinker(member, moim2);
        MemberMoimLinker linker3 = TestUtils.createLeaderMemberMoimLinker(member, moim2);

        saveEntities(member, moim1, moim2, moim3, linker1, linker2, linker3);

        MoimPost moimPost1 = TestUtils.initNoticeMoimPost(moim1, member);
        MoimPost moimPost2 = TestUtils.initNoticeMoimPost(moim1, member);
        MoimPost moimPost3 = TestUtils.initNoticeMoimPost(moim1, member);

        saveEntities(moimPost1, moimPost2, moimPost3);

        MoimPost moimPost4 = TestUtils.initNoticeMoimPost(moim2, member);

        saveEntities(moimPost4);

        MoimPost moimPost7 = TestUtils.initNoticeMoimPost(moim3, member);
        MoimPost moimPost8 = TestUtils.initNoticeMoimPost(moim3, member);
        MoimPost moimPost9 = TestUtils.initNoticeMoimPost(moim3, member);

        saveEntities(moimPost7, moimPost8, moimPost9);


        List<Long> moimIds = List.of(moim1.getId());

        // When :
        List<MoimPost> result = moimPostRepository.findNoticesLatest3ByMoimIds(moimIds);

        // Then :
        assertThat(result.size()).isEqualTo(3);
        assertThat(result)
                .extracting(MoimPost::getId)
                .contains(moimPost1.getId(), moimPost2.getId(), moimPost3.getId());
    }

    @DisplayName("findNoticesLatest3ByMoimId : 순서대로 나오는지 확인")
    @Test
    void findNoticesLatest3ByMoimIdSuccess3Test() {
        // Given :
        TestUtils.initMemberAndMemberInfo("other-member", "other-member@moimimgn.net");

        Moim moim1 = TestUtils.createMoimOnly("other1");
        Moim moim2 = TestUtils.createMoimOnly("other2");
        Moim moim3 = TestUtils.createMoimOnly("other3");

        MemberMoimLinker linker1 = TestUtils.createLeaderMemberMoimLinker(member, moim1);
        MemberMoimLinker linker2 = TestUtils.createLeaderMemberMoimLinker(member, moim2);
        MemberMoimLinker linker3 = TestUtils.createLeaderMemberMoimLinker(member, moim2);

        saveEntities(member, moim1, moim2, moim3, linker1, linker2, linker3);

        MoimPost moimPost1 = TestUtils.initNoticeMoimPost(moim1, member);
        MoimPost moimPost2 = TestUtils.initNoticeMoimPost(moim1, member);
        MoimPost moimPost3 = TestUtils.initNoticeMoimPost(moim1, member);

        saveEntities(moimPost1, moimPost2, moimPost3);

        MoimPost moimPost4 = TestUtils.initNoticeMoimPost(moim2, member);

        saveEntities(moimPost4);

        MoimPost moimPost7 = TestUtils.initNoticeMoimPost(moim3, member);
        MoimPost moimPost8 = TestUtils.initNoticeMoimPost(moim3, member);
        MoimPost moimPost9 = TestUtils.initNoticeMoimPost(moim3, member);

        saveEntities(moimPost7, moimPost8, moimPost9);


        List<Long> moimIds = List.of(moim1.getId(), moim2.getId());

        // When :
        List<MoimPost> result = moimPostRepository.findNoticesLatest3ByMoimIds(moimIds);

        // Then :
        assertThat(result.size()).isEqualTo(3);
        assertThat(result)
                .extracting(MoimPost::getId)
                .contains(moimPost4.getId(), moimPost3.getId(), moimPost2.getId());
        assertThat(result)
                .extracting(MoimPost::getId)
                .doesNotContain(moimPost1.getId());
    }

    private void saveEntities(Object... objects) {
        for (Object object : objects) {
            em.persist(object);
        }
        em.flush();
        em.clear();
    }
}