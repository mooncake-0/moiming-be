package com.peoplein.moiming.domain;

import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;


public class MoimPostTest {

    public static String postTitle = "게시물 제목";
    public static String postContent = "게시물 본문 예시";
    public static MoimPostCategory moimPostCategory = MoimPostCategory.NOTICE;
    public static boolean isNotice = false;
    public static boolean hasFiles = false;

    // 연관관계 모킹
    @Mock
    private Moim moim;
    @Mock
    private Member member;

    @BeforeEach
    void be() {

        moim = mock(Moim.class);
        member = mock(Member.class);

        // MOIM
        when(moim.getMoimName()).thenReturn("예제 모임");

        // MEMBER
        when(member.getUid()).thenReturn("wrock.kang");

    }


    @Test
    @DisplayName("성공 @ 생성자")
    void 생성자_성공() {

        // given
        // when
        MoimPost moimPost = MoimPost.createMoimPost(
                postTitle, postContent, moimPostCategory, isNotice, hasFiles, moim, member
        );

        // then
        assertEquals(postTitle, moimPost.getPostTitle());
        assertEquals(postContent, moimPost.getPostContent());
        assertEquals(moimPostCategory, moimPost.getMoimPostCategory());
        assertEquals(isNotice, moimPost.isNotice());
        assertEquals(hasFiles, moimPost.isHasFiles());
        assertNotNull(moimPost.getPostComments());

        // 메모리 할당 받은 객체들 그대로 반환
        assertEquals(moim, moimPost.getMoim());
        assertEquals(member, moimPost.getMember());
    }

    @Test
    @DisplayName("실패 @ 생성자 - 잘못된 값으로 생성시도")
    void 생성자_실패() {

        //given
        //when
        //then
        assertThatThrownBy(() -> MoimPost.createMoimPost("", postContent, moimPostCategory, isNotice, hasFiles, moim, member)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MoimPost.createMoimPost(null, postContent, moimPostCategory, isNotice, hasFiles, moim, member)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MoimPost.createMoimPost(postTitle, "", moimPostCategory, isNotice, hasFiles, moim, member)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MoimPost.createMoimPost(postTitle, null, moimPostCategory, isNotice, hasFiles, moim, member)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MoimPost.createMoimPost(postTitle, postContent, null, isNotice, hasFiles, moim, member)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> MoimPost.createMoimPost(postTitle, postContent, moimPostCategory, isNotice, hasFiles, null, member)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> MoimPost.createMoimPost(postTitle, postContent, moimPostCategory, isNotice, hasFiles, moim, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("성공 @ 게시물에 댓글 달기")
    void 게시물_댓글_달기_성공() {
        //given
        MoimPost moimPost = MoimPost.createMoimPost(
                postTitle, postContent, moimPostCategory, isNotice, hasFiles, moim, member
        );

        //when
        PostComment postComment = PostComment.createPostComment(
                "예시댓글입니다", member, moimPost
        );

        //then
        assertEquals(1, moimPost.getPostComments().size());
        assertEquals(postComment, moimPost.getPostComments().get(0));
        assertEquals("예시댓글입니다", moimPost.getPostComments().get(0).getCommentContent());
        assertEquals(member, moimPost.getPostComments().get(0).getMember());
        assertEquals(moimPost, postComment.getMoimPost());
    }

    @Test
    @DisplayName("실패 @ 게시물에 댓글 달기 - Post Comment 생성 실패")
    void 게시물_댓글_달기_실패_댓글_생성_실패() {
        //given
        MoimPost moimPost = MoimPost.createMoimPost(
                postTitle, postContent, moimPostCategory, isNotice, hasFiles, moim, member
        );

        //when
        PostComment postComment = PostComment.createPostComment(
                "예시댓글입니다", member, moimPost
        );

        //then
        assertEquals(1, moimPost.getPostComments().size());
        assertEquals(postComment, moimPost.getPostComments().get(0));
        assertEquals("예시댓글입니다", moimPost.getPostComments().get(0).getCommentContent());
        assertEquals(member, moimPost.getPostComments().get(0).getMember());
        assertEquals(moimPost, postComment.getMoimPost());
    }

    @Test
    @DisplayName("실패 @ 게시물에 댓글 달기 - addPostComment() 실패")
    void 게시물_댓글_달기_실패_함수_실패() {

        //given
        MoimPost moimPost = MoimPost.createMoimPost(
                postTitle, postContent, moimPostCategory, isNotice, hasFiles, moim, member
        );

        //when
        PostComment postComment = null;

        //then
        assertThatThrownBy(() -> moimPost.addPostComment(postComment)).isInstanceOf(RuntimeException.class);

    }

    @Test
    void updateSuccessTestCase1() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();
        MoimPost moimPost = TestUtils.initMoimPost(moim, member);
        Member updateMember = TestUtils.initOtherMemberAndMemberInfo();
        String changedTitle = TestUtils.postTitle + "fixed";

        // when
        boolean update = moimPost.update(changedTitle,
                TestUtils.postContent,
                TestUtils.isNotice,
                TestUtils.moimPostCategory,
                updateMember.getUid());

        // then
        assertThat(update).isTrue();
        assertThat(moimPost.getPostTitle()).isEqualTo(changedTitle);
        assertThat(moimPost.getUpdatedUid()).isEqualTo(updateMember.getUid());
    }

    @Test
    void updateSuccessTestCase2() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();
        MoimPost moimPost = TestUtils.initMoimPost(moim, member);
        Member updateMember = TestUtils.initOtherMemberAndMemberInfo();

        // when
        boolean update = moimPost.update(TestUtils.postTitle,
                TestUtils.postContent,
                TestUtils.isNotice,
                TestUtils.moimPostCategory,
                updateMember.getUid());

        // then
        assertThat(update).isFalse();
        assertThat(moimPost.getPostTitle()).isEqualTo(TestUtils.postTitle);
        assertThat(moimPost.getUpdatedUid()).isEqualTo(member.getUid());
    }

    @Test
    void updateFailTestCase1() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();
        MoimPost moimPost = TestUtils.initMoimPost(moim, member);

        // when + then
        assertThatThrownBy(() -> moimPost.update(null, null, false, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> moimPost.update(TestUtils.postTitle, null, false, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> moimPost.update(null, TestUtils.postContent, false, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> moimPost.update(null, null, false, TestUtils.moimPostCategory, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> moimPost.update(null, null, false, null, TestUtils.uid + "updated"))
                .isInstanceOf(IllegalArgumentException.class);
    }


}