package com.peoplein.moiming.service.integrated;


import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.MemberInfo;
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

import java.time.LocalDate;
import java.util.List;

import static com.peoplein.moiming.model.dto.request.PostCommentReqDto.*;
import static com.peoplein.moiming.support.TestParams.*;
import static org.assertj.core.api.Assertions.*;

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

    void insertTestMemberInfo(Member member, MemberGender memberGender, LocalDate memberBirth) {
        // given - 개인정보 기입완료
        MemberInfo curMemberInfo = makeMemberInfo(member, memberGender, memberBirth);
        em.persist(curMemberInfo);
        em.flush();
        em.clear();
    }

    @Test
    void createComment_shouldCreateNormalComment_whenMoimMemberRequest() {

        // given - 환경 SU
        su();

        // given - 요청자 개인 정보 입력
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

        // given
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoimPost.getId(), null, 0);

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
    void createComment_shouldCreateReplyComment_whenMoimMemberRequest() {

        // given - 환경 SU
        su();
        PostComment parentComment = makePostComment(moimMember, testMoimPost, 0, null);
        em.persist(parentComment);
        em.flush();
        em.clear();

        // given - 요청자 개인 정보 입력
        moimMember = em.find(Member.class, moimMember.getId());
        insertTestMemberInfo(moimMember, memberGender2, memberBirth2);

        // given
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoimPost.getId(), parentComment.getId(), 1);

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
    void createComment_shouldThrowException_whenNotMoimMemberRequest_byMoimingApiException() {

        // given
        su();
        PostCommentCreateReqDto requestDto = makeCommentCreateReqDto(testMoimPost.getId(), null, 0);

        // when
        assertThatThrownBy(() -> postCommentService.createComment(requestDto, notMoimMember)).isInstanceOf(MoimingApiException.class);

    }


    private void su() {

        // Member moimMember, moimCreator
        Role testRole = makeTestRole(RoleType.USER);
        moimCreator = makeTestMember(memberPhone, nickname, ci, testRole);
        em.persist(testRole);
        em.persist(moimCreator);

        MemberInfo curMemberInfo = makeMemberInfo(moimCreator, memberGender, memberBirth);
        em.persist(curMemberInfo);

        moimMember = makeTestMember(memberPhone2, nickname2, ci2, testRole);
        notMoimMember = makeTestMember(memberPhone3, nickname3, ci3, testRole);
        em.persist(moimMember);
        em.persist(notMoimMember);

        // Moim moim 존재
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth1SampleCategory), 2, testCategory1);
        em.persist(testCategory1);
        em.persist(testCategory1_1);

        testMoim = makeTestMoim(moimName, maxMember, moimArea, List.of(testCategory1, testCategory1_1), moimCreator, -1, -1, MemberGender.N);
        MoimMember.memberJoinMoim(moimMember, testMoim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        em.persist(testMoim);

        // Post post 존재
        testMoimPost = makeMoimPost(testMoim, moimCreator, MoimPostCategory.NOTICE, false);
        em.persist(testMoimPost);

        em.flush();
        em.clear();

    }
}
