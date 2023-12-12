package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.exception.repository.InvalidQueryParameterException;
import com.peoplein.moiming.repository.PostCommentRepository;
import com.peoplein.moiming.support.RepositoryTestConfiguration;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.support.TestModelParams.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;


@Import({RepositoryTestConfiguration.class, PostCommentJpaRepository.class})
@ActiveProfiles("test")
@DataJpaTest
public class PostCommentJpaRepositoryTest extends TestObjectCreator {

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private EntityManager em;

    private MoimPost post1;
    private MoimPost post2;
    private PostComment leafComment1;
    private PostComment leafComment2;


    // 현재 TEST 대상
    // 1. findById()
    @Test
    void findById_shouldReturnOptionalComment_whenRightId() throws Exception {
        // given
        insertSampleComments();
        // when
        Optional<PostComment> byId = postCommentRepository.findById(leafComment1.getId());
        // then
        assertFalse(byId.isEmpty());
        assertThat(byId.get().getId()).isEqualTo(leafComment1.getId());
    }


    @Test
    void findById_shouldThrowException_whenIdNull_byInvalidQueryParameterException() throws Exception {

        // given
        insertSampleComments();

        // when
        // then
        assertThatThrownBy(() -> postCommentRepository.findById(null)).isInstanceOf(InvalidQueryParameterException.class);

    }


    @Test
    void findById_shouldReturnOptionalEmpty_whenWrongIdPassed() throws Exception {

        // given
        insertSampleComments();

        // when
        Optional<PostComment> byId = postCommentRepository.findById(12537L);

        // then
        assertTrue(byId.isEmpty());

    }


    // 2. findByMoimPostInHierarchyQuery
    // 2-1 정상 Return - Comment1
    // 2-2 정상 Return - Comment2
    // 2-2 Null 값 존재
    // 2-2 0 raw found

    void insertSampleComments() throws InterruptedException {

        // 참여자
        // Member - moimCreator, moimMember
        // Moim - moim1
        // MoimPost - post1, post2
        // Post1 - 1 comment + 4 replyComment + 1 comment
        // Post2 - 2 comment

        Role testRole = makeTestRole(RoleType.USER);
        Member creator = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);
        Member moimMember = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, testRole);
        em.persist(testRole);
        em.persist(creator);
        em.persist(moimMember);

        // Moim Cateogry 저장 // 이것도 사실 Object Creator 에 저장되면 좋은데..
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth1SampleCategory), 2, testCategory1);
        Moim moim = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), creator);
        em.persist(testCategory1);
        em.persist(testCategory1_1);
        em.persist(moim);

        // MoimPost 필요
        post1 = makeMoimPost(moim, creator, MoimPostCategory.NOTICE, false);
        post2 = makeMoimPost(moim, moimMember, MoimPostCategory.GREETING, true);
        em.persist(post1);
        em.persist(post2);

        // Comment 필요
        leafComment1 = makePostComment(moimMember, post1, 0, null);
        em.persist(leafComment1);

        for (int i = 0; i < 4; i++) {
            if (i % 2 == 0) em.persist(makePostComment(creator, post1, 1, leafComment1));
            else em.persist(makePostComment(moimMember, post1, 1, leafComment1));
        }

        leafComment2 = makePostComment(creator, post2, 0, null); // 댓글을 담
        PostComment comment2_2 = makePostComment(moimMember, post2, 1, leafComment2); // 답글을 담
        em.persist(leafComment2);
        em.persist(comment2_2);

    }

}