package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.repository.PostCommentRepository;
import com.peoplein.moiming.support.TestMockCreator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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
            Moim moim = mock(Moim.class);

            // given - stub
            when(moimPostRepository.findWithMoimById(any())).thenReturn(Optional.of(moimPost));
            when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
            when(moimMember.hasActivePermission()).thenReturn(true);
            when(moimPost.getMoim()).thenReturn(moim);
            when(moim.getId()).thenReturn(21234L); // 아무거나 상관 없음

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
            Moim moim = mock(Moim.class);

            // given - stub
            when(moimPostRepository.findWithMoimById(any())).thenReturn(Optional.of(moimPost));
            when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
            when(moimMember.hasActivePermission()).thenReturn(true);
            when(moimPost.getMoim()).thenReturn(moim);
            when(moim.getId()).thenReturn(21234L); // 아무거나 상관 없음

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
        when(moimPostRepository.findWithMoimById(any())).thenReturn(Optional.empty());

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
        Moim moim = mock(Moim.class);
        MoimPost moimPost = mock(MoimPost.class);

        // given - stub
        when(moimPostRepository.findWithMoimById(any())).thenReturn(Optional.of(moimPost));

        // given - stub for private method
        when(member.getId()).thenReturn(1L); // 상관없음
        when(moimPost.getMoim()).thenReturn(moim);
        when(moim.getId()).thenReturn(1L); // 상관없음
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
        Moim moim = mock(Moim.class);
        MoimPost moimPost = mock(MoimPost.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimPostRepository.findWithMoimById(any())).thenReturn(Optional.of(moimPost));

        // given - stub for private method
        when(member.getId()).thenReturn(1L); // 상관없음
        when(moimPost.getMoim()).thenReturn(moim);
        when(moim.getId()).thenReturn(1L); // 상관없음
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
        Moim moim = mock(Moim.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimPostRepository.findWithMoimById(any())).thenReturn(Optional.of(moimPost));

        // given - stub for private method
        when(member.getId()).thenReturn(1L); // 상관없음
        when(moimPost.getMoim()).thenReturn(moim);
        when(moim.getId()).thenReturn(1L); // 상관없음
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
        Moim moim = mock(Moim.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimPostRepository.findWithMoimById(any())).thenReturn(Optional.of(moimPost));

        // given - stub for private method
        when(member.getId()).thenReturn(1L); // 상관없음
        when(moimPost.getMoim()).thenReturn(moim);
        when(moim.getId()).thenReturn(1L); // 상관없음
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
        Moim moim = mock(Moim.class);
        MoimPost moimPost = mock(MoimPost.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimPostRepository.findWithMoimById(any())).thenReturn(Optional.of(moimPost));

        // given - stub for private method
        when(member.getId()).thenReturn(1L); // 상관없음
        when(moimPost.getMoim()).thenReturn(moim);
        when(moim.getId()).thenReturn(1L); // 상관없음
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasActivePermission()).thenReturn(true);

        // given - stub - 답글인데 부모가 전달이 안됨
        when(requestDto.getDepth()).thenReturn(0);
        when(requestDto.getParentId()).thenReturn(1L);

        // when
        // then
        assertThatThrownBy(() -> postCommentService.createComment(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // updateComment Test
    // 1. updateComment - success
    @Test
    void updateComment_shouldPass_whenRightInfoPassed() {

        // given
        PostCommentUpdateReqDto requestDto = mock(PostCommentUpdateReqDto.class);
        Member member = mock(Member.class);
        PostComment comment = mock(PostComment.class);
        MoimPost moimPost = mock(MoimPost.class);
        Moim moim = mock(Moim.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(postCommentRepository.findWithMoimPostAndMoimById(any())).thenReturn(Optional.of(comment));

        // given - stub for private method
        when(member.getId()).thenReturn(1L); // 상관없음
        when(moim.getId()).thenReturn(1L); // 상관없음
        when(moimPost.getMoim()).thenReturn(moim);
        when(comment.getMoimPost()).thenReturn(moimPost);
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasActivePermission()).thenReturn(true);

        // when
        postCommentService.updateComment(requestDto, member);

        // then
        verify(comment, times(1)).updateComment(any(), any());

    }


    // 2. null 상황 발생
    @Test
    void updateComment_shouldThrowException_whenParameterNull_byMoimingApiException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> postCommentService.updateComment(null, null)).isInstanceOf(MoimingApiException.class);
    }



    // 3. postComment 못찾음
    @Test
    void updateComment_shouldThrowException_whenPostCommentNotFound_byMoimingApiException() {

        // given
        PostCommentUpdateReqDto requestDto = mock(PostCommentUpdateReqDto.class);
        Member member = mock(Member.class);

        // given - stub
        when(postCommentRepository.findWithMoimPostAndMoimById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> postCommentService.updateComment(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // 4. requestMember Not Found in moim
    @Test
    void updateComment_shouldThrowException_whenMemberNotFound_byMoimingApiException() {

        // given
        PostCommentUpdateReqDto requestDto = mock(PostCommentUpdateReqDto.class);
        Member member = mock(Member.class);
        PostComment comment = mock(PostComment.class);
        MoimPost moimPost = mock(MoimPost.class);
        Moim moim = mock(Moim.class);

        // given - stub
        when(postCommentRepository.findWithMoimPostAndMoimById(any())).thenReturn(Optional.of(comment));

        // given - stub for private method
        when(member.getId()).thenReturn(1L); // 상관없음
        when(moim.getId()).thenReturn(1L); // 상관없음
        when(moimPost.getMoim()).thenReturn(moim);
        when(comment.getMoimPost()).thenReturn(moimPost);
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> postCommentService.updateComment(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // 5. requestMember Not Active in moim
    @Test
    void updateComment_shouldThrowException_whenMemberNotActive_byMoimingApiException() {

        // given
        PostCommentUpdateReqDto requestDto = mock(PostCommentUpdateReqDto.class);
        Member member = mock(Member.class);
        PostComment comment = mock(PostComment.class);
        MoimPost moimPost = mock(MoimPost.class);
        Moim moim = mock(Moim.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(postCommentRepository.findWithMoimPostAndMoimById(any())).thenReturn(Optional.of(comment));

        // given - stub for private method
        when(member.getId()).thenReturn(1L); // 상관없음
        when(moim.getId()).thenReturn(1L); // 상관없음
        when(moimPost.getMoim()).thenReturn(moim);
        when(comment.getMoimPost()).thenReturn(moimPost);
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasActivePermission()).thenReturn(false);

        // when
        // then
        assertThatThrownBy(() -> postCommentService.updateComment(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // (생략) requestMember is MoimCreator - 이 서비스단의 검증 역할이 아니다
    // (생략) requestMember is NotCommentCreator - 이 서비스단의 검증 역할이 아니다


    // deleteComment Test
    // 1. deleteComment - success
    @Test
    void deleteComment_shouldPass_whenRightInfoPassed() {

        // given
        Long postCommentId = 1L;
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);
        MoimPost moimPost = mock(MoimPost.class);
        MoimMember moimMember = mock(MoimMember.class);
        PostComment postComment = mock(PostComment.class);

        // given - stub
        when(postCommentRepository.findWithMoimPostAndMoimById(any())).thenReturn(Optional.of(postComment));

        // given - stub for private method
        when(member.getId()).thenReturn(1L); // 상관 무
        when(postComment.getMoimPost()).thenReturn(moimPost);
        when(moimPost.getMoim()).thenReturn(moim);
        when(moim.getId()).thenReturn(1L);
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasActivePermission()).thenReturn(true);

        // when
        postCommentService.deleteComment(postCommentId, member);

        // then
        verify(postComment, times(1)).deleteComment(any());

    }


    // 2. null 로 인한 Exception 발생
    @Test
    void deleteComment_shouldThrowException_whenParameterNull_byMoimingApiException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> postCommentService.deleteComment(null, null)).isInstanceOf(MoimingApiException.class);

    }


    // 3. moimNotFound 로 인한 Exception 발생
    @Test
    void deleteComment_shouldThrowException_whenPostCommentNotFound_byMoimingApiException() {

        // given
        Long postCommentId = 1L;
        Member member = mock(Member.class);

        // given - stub
        when(postCommentRepository.findWithMoimPostAndMoimById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> postCommentService.deleteComment(postCommentId, member)).isInstanceOf(MoimingApiException.class);

    }


    // 4. requestMember Not Found in moim
    @Test
    void deleteComment_shouldThrowException_whenMemberNotFound_byMoimingApiException() {

        // given
        Long postCommentId = 1L;
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);
        MoimPost moimPost = mock(MoimPost.class);
        PostComment comment = mock(PostComment.class);

        // given - stub
        when(postCommentRepository.findWithMoimPostAndMoimById(any())).thenReturn(Optional.of(comment));

        // given - stub for private method
        when(member.getId()).thenReturn(1L); // 상관없음
        when(moim.getId()).thenReturn(1L); // 상관없음
        when(moimPost.getMoim()).thenReturn(moim);
        when(comment.getMoimPost()).thenReturn(moimPost);
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> postCommentService.deleteComment(postCommentId, member)).isInstanceOf(MoimingApiException.class);

    }


    // 5. requestMember Not Active in moim
    @Test
    void deleteComment_shouldThrowException_whenMemberNotActive_byMoimingApiException() {

        // given
        Long postCommentId = 1L;
        Member member = mock(Member.class);
        MoimPost moimPost = mock(MoimPost.class);
        Moim moim = mock(Moim.class);
        MoimMember moimMember = mock(MoimMember.class);
        PostComment comment = mock(PostComment.class);

        // given - stub
        when(postCommentRepository.findWithMoimPostAndMoimById(any())).thenReturn(Optional.of(comment));

        // given - stub for private method
        when(member.getId()).thenReturn(1L); // 상관없음
        when(moim.getId()).thenReturn(1L); // 상관없음
        when(moimPost.getMoim()).thenReturn(moim);
        when(comment.getMoimPost()).thenReturn(moimPost);
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasActivePermission()).thenReturn(false);

        // when
        // then
        assertThatThrownBy(() -> postCommentService.deleteComment(postCommentId, member)).isInstanceOf(MoimingApiException.class);

    }


    // (생략) requestMember is MoimCreator - 이 서비스단의 검증 역할이 아니다
    // (생략) requestMember is NotCommentCreator - 이 서비스단의 검증 역할이 아니다


}