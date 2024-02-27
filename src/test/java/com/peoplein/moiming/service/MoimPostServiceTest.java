package com.peoplein.moiming.service;


import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.inner.PostDetailsInnerDto;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.repository.PostCommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;

import static com.peoplein.moiming.model.dto.inner.PostDetailsInnerDto.*;
import static com.peoplein.moiming.model.dto.request.MoimPostReqDto.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class MoimPostServiceTest {

    @InjectMocks
    private MoimPostService moimPostService;

    @Mock
    private MoimMemberRepository moimMemberRepository;

    @Mock
    private MoimPostRepository moimPostRepository;

    @Mock
    private MoimMemberService moimMemberService;

    @Mock
    private PostCommentService postCommentService;

    @Mock
    private PostCommentRepository postCommentRepository;


    @Test
    void createMoimPost_shouldPass_whenRightInfoPassed() {

        try (MockedStatic<MoimPost> mocker = mockStatic(MoimPost.class)) {
            // given
            Member member = mock(Member.class);
            MoimMember moimMember = mock(MoimMember.class);
            MoimPostCreateReqDto requestDto = mock(MoimPostCreateReqDto.class);

            // given - stub
            when(moimMember.getMemberState()).thenReturn(MoimMemberState.ACTIVE);
            when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
            mocker.when(() -> MoimPost.createMoimPost(any(), any(), any(), anyBoolean(), anyBoolean(), any(), any()))
                    .thenReturn(null); // 해당 결과는 상관 없다는 것을 지칭

            // when
            moimPostService.createMoimPost(requestDto, member);

            // then
            verify(moimPostRepository, times(1)).save(any());

        }
    }


    @Test
    void createMoimPost_shouldThrowException_whenRequestDtoNull_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        MoimPostCreateReqDto requestDto = null;


        // when
        // then
        assertThatThrownBy(() -> moimPostService.createMoimPost(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    @Test
    void createMoimPost_shouldThrowException_whenMemberNull_byMoimingApiException() {

        // given
        Member member = null;
        MoimPostCreateReqDto requestDto = mock(MoimPostCreateReqDto.class);


        // when
        // then
        assertThatThrownBy(() -> moimPostService.createMoimPost(requestDto, member)).isInstanceOf(MoimingApiException.class);
    }


    // 모임원 아닌 상황
    @Test
    void createMoimPost_shouldThrowException_whenNotMoimMember_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        MoimPostCreateReqDto requestDto = mock(MoimPostCreateReqDto.class);

        // given - stub
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> moimPostService.createMoimPost(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // 게시물을 생성할 권한이 없는 상황
    @Test
    void createMoimPost_shouldThrowException_whenMoimMemberNotActive() {

        // given
        Member member = mock(Member.class);
        MoimMember moimMember = mock(MoimMember.class);
        MoimPostCreateReqDto requestDto = mock(MoimPostCreateReqDto.class);

        // given - stub
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.getMemberState()).thenReturn(MoimMemberState.IBF);

        // when
        // then
        assertThatThrownBy(() -> moimPostService.createMoimPost(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // --- getMoimPosts TEST
    // getMoimPosts > MoimPost 정상, moimmember 통과 정상일 경우
    @Test
    void getMoimPosts_shouldPass_whenRightInfoPassed() {

        // given
        Member member = mock(Member.class);
        MoimPost moimPost = mock(MoimPost.class);

        // when
        moimPostService.getMoimPosts(1L, null, null, 0, member); // 검증시에는 Matchers 사용하지 않으므로, null 로 아무 상관 없음을 지칭

        // then
        verify(moimPostRepository, times(1)).findWithMemberByCategoryAndLastPostOrderByDateDesc(any(), any(), any(), anyInt(), anyBoolean());
        verify(moimMemberService, times(1)).getMoimMemberStates(any(), any());

    }


    // 로직이 달라지는 사례 검증
    // moimMemberState 가 INACTIVE 여도 통과한다
    // 사실 이 함수에서는 모임 멤버가 무슨 값이든 상관 없다가 기준이므로 안해도 되는 테스트
    @Test
    void getMoimPosts_shouldPass_whenMoimMemberIsNotActive() {

        // given
        Member member = mock(Member.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.getMemberState()).thenReturn(MoimMemberState.IBF);

        // when
        moimPostService.getMoimPosts(1L, null, null, 0, member); // 검증시에는 Matchers 사용하지 않으므로, null 로 아무 상관 없음을 지칭

        // then
        verify(moimPostRepository, times(1)).findWithMemberByCategoryAndLastPostOrderByDateDesc(any(), any(), any(), anyInt(), anyBoolean());
        verify(moimMemberService, times(1)).getMoimMemberStates(any(), any());

    }


    // moimId == null 일 경우 예외 발생
    @Test
    void getMoimPosts_shouldThrowException_whenMoimIdNull_byMoimingApiException() {

        // given
        Long moimId = null;
        Member member = mock(Member.class);

        // when
        // then // 원함수 실행시에는 Arguments Matcher 사용하는거 아니다
        assertThatThrownBy(() -> moimPostService.getMoimPosts(moimId, null, null, 0, member)).isInstanceOf(MoimingApiException.class);

    }


    // member == null 일 경우 예외 발생
    @Test
    void getMoimPosts_shouldThrowException_whenMemberNull_byMoimingApiException() {

        // given
        Member member = null;

        // when
        // then // 원함수 실행시에는 Arguments Matcher 사용하는거 아니다
        assertThatThrownBy(() -> moimPostService.getMoimPosts(1L, null, null, 0, member)).isInstanceOf(MoimingApiException.class);

    }


    // 마지막 검색 post 를 찾을 수 없음, Exception 발생 // Null 이 아닌 상태로 들어온 상황이여야 한다 -> 상관없는 value 가 아님
    @Test
    void getMoimPosts_shouldThrowException_whenLastPostNotFound_byMoimingApiException() {

        // given
        Member member = mock(Member.class);

        // given - stub
        when(moimPostRepository.findById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> moimPostService.getMoimPosts(1L, 1234L, null, 0, member)).isInstanceOf(MoimingApiException.class);

    }


    // getMoimPostDetail - 성공
    @Test
    void getMoimPostDetail_shouldPass_whenRightInfoPassed() {

        // given
        Long postId = 1L;
        Member member = mock(Member.class);
        Member postCreator = mock(Member.class);
        Moim moim = mock(Moim.class);
        MoimPost post = mock(MoimPost.class);
        PostCommentDetailsDto commentsInnerDto = mock(PostCommentDetailsDto.class);

        // given - stub
        when(moimPostRepository.findWithMemberById(any())).thenReturn(Optional.of(post));
        when(post.getMoim()).thenReturn(moim);
        when(postCommentService.getSortedPostComments(any())).thenReturn(commentsInnerDto);
        when(post.getMember()).thenReturn(postCreator);
        when(commentsInnerDto.getCommentCreatorIds()).thenReturn(new HashSet<>());
        // 사실 이렇게 하면 안될 것 같긴 하다. NULL 일 경우 어떻게 되어야 하는지 처리가 되어야 하지만, 이전 로직에서 NOTNULL 이 보장됨 -> verify 를 통해서 이전 로직이 진행됨을 확인

        // when
        moimPostService.getMoimPostDetail(postId, member);

        // then
        verify(postCommentService, times(1)).getSortedPostComments(any());
        verify(moimMemberService, times(1)).getMoimMemberStates(any(), any());

    }


    // getMoimPostDetail - 실패 :: POST 없음
    @Test
    void getMoimPostDetail_shouldThrowException_whenMoimNotFound_byMoimingApiException() {

        // given
        Long postId = 1L;
        Member member = mock(Member.class);

        // given - stub
        when(moimPostRepository.findWithMemberById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> moimPostService.getMoimPostDetail(postId, member)).isInstanceOf(MoimingApiException.class);

    }


    // updateMoimPost - 성공 :: 작성자의 요청만이 성공
    @Test
    void updateMoimPost_shouldPass_whenPostCreatorReq() {

        // given
        MoimPostUpdateReqDto requestDto = mock(MoimPostUpdateReqDto.class);
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);
        MoimPost post = mock(MoimPost.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimPostRepository.findWithMemberById(any())).thenReturn(Optional.of(post));
        when(post.getMoim()).thenReturn(moim);
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasActivePermission()).thenReturn(true); // 활동중인 모임원이어야 함
        when(post.getMember()).thenReturn(member);
        when(member.getId()).thenReturn(1L); // 아무 동일하게 값이 나오도록 (null == null 은 안됨)

        // when
        moimPostService.updateMoimPost(requestDto, member);

        // then
        verify(post, times(1)).changeMoimPostInfo(any(), any(), any(), any(), any(), any());

    }


    // updateMoimPost - 실패 :: 게시물 없음
    @Test
    void updateMoimPost_shouldThrowException_whenPostNotFound_byMoimingApiException() {

        // given
        MoimPostUpdateReqDto requestDto = mock(MoimPostUpdateReqDto.class);
        Member member = mock(Member.class);

        // given - stub
        when(moimPostRepository.findWithMemberById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> moimPostService.updateMoimPost(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // updateMoimPost - 실패 :: 요청자 없음
    @Test
    void updateMoimPost_shouldThrowException_whenMoimMemberNotFound_byMoimingApiException() {

        // given
        MoimPostUpdateReqDto requestDto = mock(MoimPostUpdateReqDto.class);
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);
        MoimPost post = mock(MoimPost.class);

        // given - stub
        when(moimPostRepository.findWithMemberById(any())).thenReturn(Optional.of(post));
        when(post.getMoim()).thenReturn(moim);

        // when
        // then
        assertThatThrownBy(() -> moimPostService.updateMoimPost(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // updateMoimPost - 실패 :: 권한 부족 (활동중이 아님)
    @Test
    void updateMoimPost_shouldThrowException_whenMoimMemberNotActive_byMoimingApiException() {

        // given
        MoimPostUpdateReqDto requestDto = mock(MoimPostUpdateReqDto.class);
        Member member = mock(Member.class);
        MoimPost post = mock(MoimPost.class);
        Moim moim = mock(Moim.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimPostRepository.findWithMemberById(any())).thenReturn(Optional.of(post));
        when(post.getMoim()).thenReturn(moim);
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasActivePermission()).thenReturn(false);

        // when
        // then
        assertThatThrownBy(() -> moimPostService.updateMoimPost(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // updateMoimPost - 실패 :: 권한 부족 (작성자가 아님)
    @Test
    void updateMoimPost_shouldThrowException_whenMoimMemberNotPostCreator_byMoimingApiException() {

        // given
        MoimPostUpdateReqDto requestDto = mock(MoimPostUpdateReqDto.class);
        Member member = mock(Member.class);
        Member postCreator = mock(Member.class);
        MoimPost post = mock(MoimPost.class);
        Moim moim = mock(Moim.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimPostRepository.findWithMemberById(any())).thenReturn(Optional.of(post));
        when(post.getMoim()).thenReturn(moim);
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasActivePermission()).thenReturn(true);
        when(post.getMember()).thenReturn(postCreator);
        when(member.getId()).thenReturn(1L);
        when(postCreator.getId()).thenReturn(1234L); // 둘은 다른 Member 이다


        // when
        // then
        assertThatThrownBy(() -> moimPostService.updateMoimPost(requestDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // deleteMoimPost - 성공 :: 작성자의 요청
    @Test
    void deleteMoimPost_shouldPass_whenMoimMemberReq() {

        // given
        Long postId = 1L;
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);
        MoimPost moimPost = mock(MoimPost.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));
        when(moimPost.getMoim()).thenReturn(moim);
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasActivePermission()).thenReturn(true);
        when(moimPost.getMember()).thenReturn(member); // 요청자와 작성자가 같음
        when(member.getId()).thenReturn(1L);// 아무 값이나 반환해서 비교가능하게 해준다

        // when
        moimPostService.deleteMoimPost(postId, member);

        // then
        verify(postCommentRepository, times(1)).removeAllByMoimPostId(any());
        verify(moimPostRepository, times(1)).remove(any());

    }


    // deleteMoimPost - 성공 :: 운영자의 요청
    @Test
    void deleteMoimPost_shouldPass_whenReqFromMoimCreator() {

        // given
        Long postId = 1L;
        Member moimCreator = mock(Member.class);
        Member postCreator = mock(Member.class);
        Moim moim = mock(Moim.class);
        MoimPost moimPost = mock(MoimPost.class);
        MoimMember creatorInfo = mock(MoimMember.class); // 요청자

        // given - stub
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));
        when(moimPost.getMoim()).thenReturn(moim);
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(creatorInfo));
        when(creatorInfo.hasActivePermission()).thenReturn(true);
        when(creatorInfo.hasPermissionOfManager()).thenReturn(true); // 요청자는 운영자임 // 운영자면 무조건 Pass 라 작성자 다르다는거 비교 안해줘도 됨 (이하들 생략)
//        when(moimPost.getMember()).thenReturn(postCreator); // 작성자는 다른 멤버임
//        when(moimCreator.getId()).thenReturn(1L);
//        when(postCreator.getId()).thenReturn(1234L); // 둘은 다르다

        // when
        moimPostService.deleteMoimPost(postId, moimCreator);

        // then
        verify(postCommentRepository, times(1)).removeAllByMoimPostId(any());
        verify(moimPostRepository, times(1)).remove(any());

    }


    // deleteMoimPost - 실패 :: 게시물 없음
    @Test
    void deleteMoimPost_shouldThrowException_whenReqPostNoFound_byMoimingApiException() {

        // given
        Long postId = 1L;
        Member member = mock(Member.class);

        // given - stub
        when(moimPostRepository.findById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> moimPostService.deleteMoimPost(postId, member)).isInstanceOf(MoimingApiException.class);

    }


    // deleteMoimPost - 실패 :: 요청자 없음
    @Test
    void deleteMoimPost_shouldThrowException_whenNotMoimMemberReq_byMoimingApiException() {

        // given
        Long postId = 1L;
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);
        MoimPost moimPost = mock(MoimPost.class);

        // given - stub
        when(moimPost.getMoim()).thenReturn(moim);
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));

        // when
        // then
        assertThatThrownBy(() -> moimPostService.deleteMoimPost(postId, member)).isInstanceOf(MoimingApiException.class);

    }


    // deleteMoimPost - 실패 :: 권한 부족 (활동중이 아님)
    @Test
    void deleteMoimPost_shouldThrowException_whenReqFromInactiveMember_byMoimingApiException() {

        // given
        Long postId = 1L;
        Member inactiveMember = mock(Member.class);
//        Member postCreator = mock(Member.class);
        Moim moim = mock(Moim.class);
        MoimPost moimPost = mock(MoimPost.class);
        MoimMember inactiveMoimMember = mock(MoimMember.class); // 요청자

        // given - stub
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));
        when(moimPost.getMoim()).thenReturn(moim);
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(inactiveMoimMember));
        when(inactiveMoimMember.hasActivePermission()).thenReturn(false); // 요청자는 활동중이 아님 > 다 필요 없음
//        when(moimPost.getMember()).thenReturn(postCreator); // 작성자는 다른 멤버임
//        when(otherMember.getId()).thenReturn(1L); // 요청자는 1L 임
//        when(postCreator.getId()).thenReturn(1234L); // 작성자는 1234L 임 (둘이 다름

        // when
        // then
        assertThatThrownBy(() -> moimPostService.deleteMoimPost(postId, inactiveMember)).isInstanceOf(MoimingApiException.class);

    }


    // deleteMoimPost - 실패 :: 권한 부족 (작성자도 아니고 운영자도 아님)
    @Test
    void deleteMoimPost_shouldThrowException_whenReqFromOtherMoimMember_byMoimingApiException() {

        // given
        Long postId = 1L;
        Member otherMember = mock(Member.class);
        Member postCreator = mock(Member.class);
        Moim moim = mock(Moim.class);
        MoimPost moimPost = mock(MoimPost.class);
        MoimMember otherMoimMember = mock(MoimMember.class); // 요청자

        // given - stub
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(moimPost));
        when(moimPost.getMoim()).thenReturn(moim);
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(otherMoimMember));
        when(otherMoimMember.hasActivePermission()).thenReturn(true); // 요청자는 활동중임
        when(moimPost.getMember()).thenReturn(postCreator); // 작성자는 다른 멤버임
        when(otherMember.getId()).thenReturn(1L); // 요청자는 1L 임
        when(postCreator.getId()).thenReturn(1234L); // 작성자는 1234L 임 (둘이 다름

        // when
        // then
        assertThatThrownBy(() -> moimPostService.deleteMoimPost(postId, otherMember)).isInstanceOf(MoimingApiException.class);

    }

}