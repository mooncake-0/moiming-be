package com.peoplein.moiming.service;

import com.peoplein.moiming.BaseTest;
import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.model.dto.domain.MoimPostDto;
import com.peoplein.moiming.model.dto.request_b.MoimPostRequestDto;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.repository.PostCommentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


/*
 해당 도메인 서비스단 재설계 예정
 - Service 는 단위테스트만 진행 예정 (DB 개입 필요 없음)
 - Repo 단위테스트, Controlller 통합 테스트로 진행
 */
@SpringBootTest
@Transactional
public class MoimPostServiceTest {

    @Autowired
    MoimPostService moimPostService;

    @Autowired
    PostCommentRepository commentRepository;
    @Autowired
    EntityManager em;
    @Autowired
    MoimPostRepository moimPostRepository;


//    @Test
    void updateIntegrationSuccessTest() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();
        MoimPost moimPost = TestUtils.initMoimPost(moim, member);
        String changedPostTitle = "fixed " + TestUtils.postTitle;

        persist(member,
                moim,
                moimPost,
                member.getRoles().get(0).getRole(),
                member.getRoles().get(0));
        flushAndClear();

        MoimPostRequestDto moimPostRequestDto = new MoimPostRequestDto(
                moim.getId(),
                moimPost.getId(),
                changedPostTitle,
                TestUtils.postContent,
                TestUtils.isNotice,
                TestUtils.moimPostCategory);

        // when
        MoimPostDto moimPostDto = moimPostService.updatePost(moimPostRequestDto, member);

        // then
        assertThat(moimPostDto.getPostTitle()).isEqualTo(changedPostTitle);
        assertThat(moimPostDto.getUpdatedMemberId()).isEqualTo(member.getId());
    }
//    @Test
    void updateIntegrationFailTest() {
        // 작성자만 수정 가능함.

        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Member updateMember = TestUtils.initOtherMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();
        MoimPost moimPost = TestUtils.initMoimPost(moim, member);
        String changedPostTitle = "fixed " + TestUtils.postTitle;

        persist(member,
                moim,
                moimPost,
                updateMember,
                member.getRoles().get(0).getRole(),
                member.getRoles().get(0),
                updateMember.getRoles().get(0).getRole(),
                updateMember.getRoles().get(0));
        flushAndClear();

        MoimPostRequestDto moimPostRequestDto = new MoimPostRequestDto(
                moim.getId(),
                moimPost.getId(),
                changedPostTitle,
                TestUtils.postContent,
                TestUtils.isNotice,
                TestUtils.moimPostCategory);

        // when + then
        assertThatThrownBy(() -> moimPostService.updatePost(moimPostRequestDto, updateMember))
                .isInstanceOf(IllegalArgumentException.class);
    }


//    @Test
    void deleteIntegrationSuccessTest() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();
        MemberMoimLinker moimLinker = TestUtils.createLeaderMemberMoimLinker(member, moim);
        MoimPost moimPost = TestUtils.initMoimPost(moim, member);
        PostComment comment = PostComment.createPostComment("hello", member, moimPost);
        PostComment comment1 = PostComment.createPostComment("hello", member, moimPost);
        PostComment comment2 = PostComment.createPostComment("hello", member, moimPost);
        PostComment comment3 = PostComment.createPostComment("hello", member, moimPost);

        persist(member,
                moim,
                moimLinker,
                moimPost,
                comment,
                comment1,
                comment2,
                comment3,
                member.getRoles().get(0).getRole(),
                member.getRoles().get(0));
        flushAndClear();

        // when
        moimPostService.deletePost(moimPost.getId(), member);

        // then
        flushAndClear();
        MoimPost findMoimPost = moimPostRepository.findById(moimPost.getId());
        List<PostComment> findPostComments = commentRepository.findWithMoimPostId(moimPost.getId());

        assertThat(findMoimPost).isNull();
        assertThat(findPostComments.size()).isEqualTo(0);
    }


    void flushAndClear() {
        em.flush();
        em.clear();
    }

    void persist(Object ... objects) {
        for (Object object : objects) {

        }
        Arrays.stream(objects).forEach(o -> em.persist(o));
    }
}