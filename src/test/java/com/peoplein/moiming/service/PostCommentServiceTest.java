package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.member.Member;
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

import java.util.ArrayList;
import java.util.List;
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

    @Mock
    private NotificationService notificationService;


    // Success - 1차 댓글일 경우 성공한다
    @Test
    void createComment_shouldPass_whenNormalCommentPassed() {

        // given
        PostCommentCreateReqDto requestDto = mock(PostCommentCreateReqDto.class);
        Member member = mock(Member.class); // 요청자
        when(member.getId()).thenReturn(11L);
        MoimMember moimMember = mock(MoimMember.class); // 요청자 권한
        MoimPost moimPost = mock(MoimPost.class); // 생성 게시물
        Moim moim = mock(Moim.class); // 게시물이 속한 모임
        Member postCreator = mock(Member.class);
        when(postCreator.getId()).thenReturn(22L); // 위와 다름을 명시하기 위해 지정
        MoimMember postCreatorStatus = mock(MoimMember.class);

        // given - stub
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));
        // 두개니까 지칭 필요
        when(moimMemberRepository.findByMemberAndMoimId(eq(11L), any())).thenReturn(Optional.of(moimMember)); // 요청하는자 확인
        when(moimMemberRepository.findByMemberAndMoimId(eq(22L), any())).thenReturn(Optional.of(postCreatorStatus)); // 댓글을 남기려는 자 확인
        when(moimMember.hasActivePermission()).thenReturn(true);
        when(postCreatorStatus.hasActivePermission()).thenReturn(true);
        when(moimPost.getMoim()).thenReturn(moim);
        when(moimPost.getMember()).thenReturn(postCreator);
        when(moim.getId()).thenReturn(33L); // 아무거나 상관 없음

        // given - stub - 1차 댓글이 전달된다
        when(requestDto.getDepth()).thenReturn(0);
        when(requestDto.getParentId()).thenReturn(null);

        // when
        postCommentService.createComment(requestDto, member);

        // then
        verify(postCommentRepository, times(1)).save(any());
        verify(notificationService, times(1)).createNotification(any(), any(), any(), any(), any(), any(), any(), any());

    }


    @Test
    void createComment_shouldPassAndNotCreateNotification_whenNormalCommentPassedButCreatorNotActive() {

        // given
        PostCommentCreateReqDto requestDto = mock(PostCommentCreateReqDto.class);
        Member member = mock(Member.class); // 요청자
        when(member.getId()).thenReturn(11L);
        MoimMember moimMember = mock(MoimMember.class); // 요청자 권한
        MoimPost moimPost = mock(MoimPost.class); // 생성 게시물
        Moim moim = mock(Moim.class); // 게시물이 속한 모임
        Member postCreator = mock(Member.class);
        when(postCreator.getId()).thenReturn(22L); // 위와 다름을 명시하기 위해 지정
        MoimMember postCreatorStatus = mock(MoimMember.class);

        // given - stub
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));
        // 두개니까 지칭 필요
        when(moimMemberRepository.findByMemberAndMoimId(eq(11L), any())).thenReturn(Optional.of(moimMember)); // 요청하는자 확인
        when(moimMemberRepository.findByMemberAndMoimId(eq(22L), any())).thenReturn(Optional.of(postCreatorStatus)); // 댓글을 남기려는 자 확인
        when(moimMember.hasActivePermission()).thenReturn(true);
        when(postCreatorStatus.hasActivePermission()).thenReturn(false);
        when(moimPost.getMoim()).thenReturn(moim);
        when(moimPost.getMember()).thenReturn(postCreator);
        when(moim.getId()).thenReturn(33L); // 아무거나 상관 없음

        // given - stub - 1차 댓글이 전달된다
        when(requestDto.getDepth()).thenReturn(0);
        when(requestDto.getParentId()).thenReturn(null);

        // when
        postCommentService.createComment(requestDto, member);

        // then
        verify(postCommentRepository, times(1)).save(any());
        verify(notificationService, times(0)).createNotification(any(), any(), any(), any(), any(), any(), any(), any());

    }


    @Test
    void createComment_shouldPass_whenReplyCommentPassed() {

        // given
        PostCommentCreateReqDto requestDto = mock(PostCommentCreateReqDto.class);
        Member member = mock(Member.class); // 요청자
        MoimPost moimPost = mock(MoimPost.class);
        MoimMember moimMember = mock(MoimMember.class);
        PostComment parentComment = mock(PostComment.class);
        Member parentCommentCreator = mock(Member.class); // 댓글 생성자
        MoimMember parentCommentCreatorStatus = mock(MoimMember.class);
        Moim moim = mock(Moim.class);

        when(parentComment.getMember()).thenReturn(parentCommentCreator);
        when(member.getId()).thenReturn(11L); // 요청자
        when(parentCommentCreator.getId()).thenReturn(22L);

        // given - stub
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));
        when(moimMemberRepository.findByMemberAndMoimId(eq(11L), any())).thenReturn(Optional.of(moimMember));// 요청자 권한
        when(moimMemberRepository.findByMemberAndMoimId(eq(22L), any())).thenReturn(Optional.of(parentCommentCreatorStatus));// 수신자 권한

        when(moimMember.hasActivePermission()).thenReturn(true);
        when(parentCommentCreatorStatus.hasActivePermission()).thenReturn(true); // 알림이 발생할 것임
        when(moimPost.getMoim()).thenReturn(moim);
        when(moim.getId()).thenReturn(33L); // 아무거나 상관 없음

        // given - stub - 댓글에 대한 답글임이 전달
        when(requestDto.getDepth()).thenReturn(1);
        when(requestDto.getParentId()).thenReturn(1L); // any
        when(postCommentRepository.findById(any())).thenReturn(Optional.of(parentComment));

        // when
        postCommentService.createComment(requestDto, member);

        // then
        verify(postCommentRepository, times(1)).save(any());
        verify(notificationService, times(1)).createNotification(any(), any(), any(), any(), any(), any(), any(), any());

    }


    @Test
    void createComment_shouldThrowException_whenTrialToReplyOnChildComment_byMoimingApiException() {

        // given
        PostCommentCreateReqDto requestDto = mock(PostCommentCreateReqDto.class);
        Member member = mock(Member.class);
        MoimPost moimPost = mock(MoimPost.class);
        MoimMember moimMember = mock(MoimMember.class);
        PostComment parentComment = mock(PostComment.class);
        Moim moim = mock(Moim.class);

        // given - stub
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasActivePermission()).thenReturn(true);
        when(moimPost.getMoim()).thenReturn(moim);
        when(moim.getId()).thenReturn(21234L); // 아무거나 상관 없음

        // given - stub - 댓글에 대한 답글임이 전달
        when(requestDto.getDepth()).thenReturn(1); /// 답글 생성 요청이긴 한데
        when(requestDto.getParentId()).thenReturn(1L); // any
        when(parentComment.getDepth()).thenReturn(1); // 조회된 부모가 댓글이 아닌 답글이였을 경우
        when(postCommentRepository.findById(any())).thenReturn(Optional.of(parentComment));


        // when
        // then
        assertThatThrownBy(() -> postCommentService.createComment(requestDto, member)).isInstanceOf(MoimingApiException.class);
        verify(postCommentRepository, times(0)).save(any());
        verify(notificationService, times(0)).createNotification(any(), any(), any(), any(), any(), any(), any(), any());


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
        verify(postCommentRepository, times(0)).save(any());
        verify(notificationService, times(0)).createNotification(any(), any(), any(), any(), any(), any(), any(), any());

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
        verify(postCommentRepository, times(0)).save(any());
        verify(notificationService, times(0)).createNotification(any(), any(), any(), any(), any(), any(), any(), any());

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
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));

        // given - stub for private method
        when(member.getId()).thenReturn(1L); // 상관없음
        when(moimPost.getMoim()).thenReturn(moim);
        when(moim.getId()).thenReturn(1L); // 상관없음
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> postCommentService.createComment(requestDto, member)).isInstanceOf(MoimingApiException.class);
        verify(postCommentRepository, times(0)).save(any());
        verify(notificationService, times(0)).createNotification(any(), any(), any(), any(), any(), any(), any(), any());
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
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));

        // given - stub for private method
        when(member.getId()).thenReturn(1L); // 상관없음
        when(moimPost.getMoim()).thenReturn(moim);
        when(moim.getId()).thenReturn(1L); // 상관없음
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasActivePermission()).thenReturn(false);

        // when
        // then
        assertThatThrownBy(() -> postCommentService.createComment(requestDto, member)).isInstanceOf(MoimingApiException.class);
        verify(postCommentRepository, times(0)).save(any());
        verify(notificationService, times(0)).createNotification(any(), any(), any(), any(), any(), any(), any(), any());
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
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));

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
        verify(postCommentRepository, times(0)).save(any());
        verify(notificationService, times(0)).createNotification(any(), any(), any(), any(), any(), any(), any(), any());
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
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));

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
        verify(postCommentRepository, times(0)).save(any());
        verify(notificationService, times(0)).createNotification(any(), any(), any(), any(), any(), any(), any(), any());
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
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));

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
        verify(postCommentRepository, times(0)).save(any());
        verify(notificationService, times(0)).createNotification(any(), any(), any(), any(), any(), any(), any(), any());
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
        when(postCommentRepository.findWithMemberAndMoimPostById(any())).thenReturn(Optional.of(comment));

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
        when(postCommentRepository.findWithMemberAndMoimPostById(any())).thenReturn(Optional.empty());

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
        when(postCommentRepository.findWithMemberAndMoimPostById(any())).thenReturn(Optional.of(comment));

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
        when(postCommentRepository.findWithMemberAndMoimPostById(any())).thenReturn(Optional.of(comment));

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
        when(postCommentRepository.findWithMemberAndMoimPostById(any())).thenReturn(Optional.of(postComment));

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
        when(postCommentRepository.findWithMemberAndMoimPostById(any())).thenReturn(Optional.empty());

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
        when(postCommentRepository.findWithMemberAndMoimPostById(any())).thenReturn(Optional.of(comment));

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
        when(postCommentRepository.findWithMemberAndMoimPostById(any())).thenReturn(Optional.of(comment));

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


    // sortedPostComments
    // 성공
    @Test
    void getSortedPostComments_shouldPass_whenRightInfoPased() {

        // given
        Long postId = 1L;
        List<PostComment> postComments = new ArrayList<>(); // NOT_NULL

        // given - stub
        // ANY 와 같은 빈 List
        when(postCommentRepository.findWithMemberAndInfoByMoimPostInDepthAndCreatedOrder(postId)).thenReturn(postComments);

        // when
        postCommentService.getSortedPostComments(postId);

        // then
        verify(postCommentRepository, times(1)).findWithMemberAndInfoByMoimPostInDepthAndCreatedOrder(any());

    }

}