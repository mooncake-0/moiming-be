package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.repository.PostCommentRepository;
import com.peoplein.moiming.support.TestMockCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.peoplein.moiming.domain.enums.MoimMemberState.*;
import static com.peoplein.moiming.model.dto.request.PostCommentReqDto.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostCommentServiceTest extends TestMockCreator {

    @InjectMocks
    private PostCommentService postCommentService;

    @Mock
    private MoimPostRepository moimPostRepository;

    @Mock
    private PostCommentRepository postCommentRepository;

    @Mock
    private MoimMemberRepository moimMemberRepository;


    // Success - 1차 댓글일 경우 성공한다
    @Test
    void createComment_shouldPass_whenNormalCommentPassed() {

        try (MockedStatic<PostComment> mocker = mockStatic(PostComment.class)) {
            // given
            PostCommentCreateReqDto requestDto = mock(PostCommentCreateReqDto.class);
            Member member = mock(Member.class);
            MoimPost moimPost = mock(MoimPost.class);
            MoimMember moimMember = mock(MoimMember.class);

            // given - stub
            when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));
            when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
            when(moimMember.hasActivePermission()).thenReturn(true);

            // given - stub - 1차 댓글
            when(requestDto.getDepth()).thenReturn(0);
            when(requestDto.getParentId()).thenReturn(null);

            // given - static stub - 생성했다 치자
            mocker.when(() -> PostComment.createPostComment(
                    any(), any(), any(), anyInt(), any()
            )).thenReturn(null); // 어떤 값이든 상관 없음

            // when
            postCommentService.createComment(requestDto, member);

            // then
            verify(postCommentRepository, times(1)).save(any());
        }
    }


    @Test
    void createComment_shouldPass_whenReplyCommentPassed() {

        try (MockedStatic<PostComment> mocker = mockStatic(PostComment.class)) {
            // given
            PostCommentCreateReqDto requestDto = mock(PostCommentCreateReqDto.class);
            Member member = mock(Member.class);
            MoimPost moimPost = mock(MoimPost.class);
            MoimMember moimMember = mock(MoimMember.class);
            PostComment parentComment = mock(PostComment.class);

            // given - stub
            when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));
            when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
            when(moimMember.hasActivePermission()).thenReturn(true);

            // given - stub - 댓글에 대한 답글
            when(requestDto.getDepth()).thenReturn(1);
            when(requestDto.getParentId()).thenReturn(1L); // any
            when(postCommentRepository.findById(any())).thenReturn(Optional.of(parentComment));

            // given - static stub - 생성했다 치자
            mocker.when(() -> PostComment.createPostComment(
                    any(), any(), any(), anyInt(), any()
            )).thenReturn(null); // 어떤 값이든 상관 없음

            // when
            postCommentService.createComment(requestDto, member);

            // then
            verify(postCommentRepository, times(1)).save(any());
        }
    }


    // 1  requestDto null
    @Test
    void createComment_shouldThrowException_whenRequestDtoNull_byMoimingApiException() {

        // given
        PostCommentCreateReqDto requestDto = null;
        Member member = mock(Member.class);

        // when
        // then
        assertThatThrownBy(() -> postCommentService.createComment(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // 2  member null
    @Test
    void createComment_shouldThrowException_whenMemberNull_byMoimingApiException() {

        // given
        PostCommentCreateReqDto requestDto = mock(PostCommentCreateReqDto.class);
        Member member = null;

        // when
        // then
        assertThatThrownBy(() -> postCommentService.createComment(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // 3  moimPost Not Found
    @Test
    void createComment_shouldThrowException_whenMoimPostNotFound_byMoimingApiException() {

        // given
        PostCommentCreateReqDto requestDto = mock(PostCommentCreateReqDto.class);
        Member member = mock(Member.class);

        // given - stub
        when(moimPostRepository.findById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> postCommentService.createComment(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // 4  moimMember Not Null
    @Test
    void createComment_shouldThrowException_whenMoimMemberNotFound_byMoimingApiException() {

        // given
        PostCommentCreateReqDto requestDto = mock(PostCommentCreateReqDto.class);
        Member member = mock(Member.class);
        MoimPost moimPost = mock(MoimPost.class);

        // given - stub
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> postCommentService.createComment(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // 5  moimMember Not Active
    @Test
    void createComment_shouldThrowException_whenMoimMemberNotActive_byMoimingApiException() {

        // given
        PostCommentCreateReqDto requestDto = mock(PostCommentCreateReqDto.class);
        Member member = mock(Member.class);
        MoimPost moimPost = mock(MoimPost.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasActivePermission()).thenReturn(false);

        // when
        // then
        assertThatThrownBy(() -> postCommentService.createComment(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // 6  Parent Post Not Found
    @Test
    void createComment_shouldThrowException_whenParentCommentNotFound_byMoimingApiException() {

        // given
        PostCommentCreateReqDto requestDto = mock(PostCommentCreateReqDto.class);
        Member member = mock(Member.class);
        MoimPost moimPost = mock(MoimPost.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasActivePermission()).thenReturn(true);

        // given - stub - Parent 댓글 정보가 전달됨
        when(requestDto.getDepth()).thenReturn(1);
        when(requestDto.getParentId()).thenReturn(1L);
        when(postCommentRepository.findById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> postCommentService.createComment(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // 7  Parent comment Mapping Error
    @Test
    void createComment_shouldThrowException_whenDepthTrueButNoParentCommentId_byMoimingApiException() {

        // given
        PostCommentCreateReqDto requestDto = mock(PostCommentCreateReqDto.class);
        Member member = mock(Member.class);
        MoimPost moimPost = mock(MoimPost.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasActivePermission()).thenReturn(true);

        // given - stub - 답글인데 부모가 전달이 안됨
        when(requestDto.getDepth()).thenReturn(1);
        when(requestDto.getParentId()).thenReturn(null);

        // when
        // then
        assertThatThrownBy(() -> postCommentService.createComment(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // 7  Parent comment Mapping Error2
    @Test
    void createComment_shouldThrowException_whenDepthZeroButParentIdNotNull_byMoimingApiException() {

        // given
        PostCommentCreateReqDto requestDto = mock(PostCommentCreateReqDto.class);
        Member member = mock(Member.class);
        MoimPost moimPost = mock(MoimPost.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasActivePermission()).thenReturn(true);

        // given - stub - 답글인데 부모가 전달이 안됨
        when(requestDto.getDepth()).thenReturn(0);
        when(requestDto.getParentId()).thenReturn(1L);

        // when
        // then
        assertThatThrownBy(() -> postCommentService.createComment(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }

}