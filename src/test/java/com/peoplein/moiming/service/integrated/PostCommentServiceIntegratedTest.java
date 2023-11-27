package com.peoplein.moiming.service.integrated;


import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.PostCommentRepository;
import com.peoplein.moiming.service.PostCommentService;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static com.peoplein.moiming.model.dto.request.PostCommentReqDto.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

// Service 예외 상황은 단위 테스트에서 다 진행
// 성공시 DB Tx Test
@SpringBootTest
@Transactional
public class PostCommentServiceIntegratedTest extends TestObjectCreator {

    @Autowired
    private PostCommentService postCommentService;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private EntityManager em;

    private Member moimCreator, moimMember, notMoimMember;
    private Moim testMoim;
    private MoimPost testMoimPost;


    @Test
    void createComment_shouldCreateNormalComment_whenMoimMemberRequest(){

        // given
        su();
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoim.getId(), testMoimPost.getId(), null, 0);

        // when
        postCommentService.createComment(requestDto, moimMember);

        // then - prepare
        List<PostComment> postComments = em.createQuery("select c from PostComment c where c.moimPost.id = :moimPostId", PostComment.class)
                .setParameter("moimPostId", testMoimPost.getId())
                .getResultList();

        // then - verify
        assertThat(postComments.size()).isEqualTo(1);
        assertThat(postComments.get(0).getDepth()).isEqualTo(0);
        assertThat(postComments.get(0).getParent()).isEqualTo(null);

    }


    // 답글 생성 Test
    @Test
    void createComment_shouldCreateReplyComment_whenMoimMemberRequest(){

        // given
        su();
        PostComment parentComment = makePostComment(moimMember, testMoimPost, 0, null);
        em.persist(parentComment);
        em.flush();
        em.clear();

        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoim.getId(), testMoimPost.getId(), parentComment.getId(), 1);

        // when
        postCommentService.createComment(requestDto, moimMember);

        // then - prepare
        List<PostComment> postComments = em.createQuery("select c from PostComment c where c.moimPost.id = :moimPostId", PostComment.class)
                .setParameter("moimPostId", testMoimPost.getId())
                .getResultList();

        // then - verify
        assertThat(postComments.size()).isEqualTo(2);
        int each = 0;
        for (PostComment postComment : postComments) {
            if (postComment.getDepth() == 0) {
                each++;
                assertThat(postComment.getParent()).isEqualTo(null);
            }
            if (postComment.getDepth() == 1) {
                each++;
                assertThat(postComment.getParent().getId()).isEqualTo(parentComment.getId());
            }
        }
        assertThat(each).isEqualTo(2);

    }


    @Test
    void createComment_shouldThrowException_whenNotMoimMemberRequest_byMoimingApiException(){

        // given
        su();
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoim.getId(), testMoimPost.getId(), null, 0);

        // when
        assertThatThrownBy(() -> postCommentService.createComment(requestDto, notMoimMember)).isInstanceOf(MoimingApiException.class);

    }




    private void su() {

        // Member moimMember, moimCreator
        Role testRole = makeTestRole(RoleType.USER);
        moimCreator =makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);
        moimMember = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, testRole);

        notMoimMember = makeTestMember(memberEmail3, memberPhone3, memberName3, nickname3, ci3, testRole);
        em.persist(testRole);
        em.persist(moimCreator);
        em.persist(moimMember);
        em.persist(notMoimMember);

        // Moim moim 존재
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth1SampleCategory), 2, testCategory1);
        em.persist(testCategory1);
        em.persist(testCategory1_1);

        testMoim = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), moimCreator);
        MoimMember.memberJoinMoim(moimMember, testMoim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        em.persist(testMoim);

        // Post post 존재
        testMoimPost = makeMoimPost(testMoim, moimCreator, MoimPostCategory.NOTICE, false);
        em.persist(testMoimPost);

        em.flush();
        em.clear();

    }
}
