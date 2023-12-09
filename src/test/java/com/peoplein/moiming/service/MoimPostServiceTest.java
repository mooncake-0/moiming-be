package com.peoplein.moiming.service;


import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimPostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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
        verify(moimPostRepository, times(1)).findByCategoryAndLastPostOrderByDateDesc(any(), any(), any(), anyInt(), anyBoolean());

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
        verify(moimPostRepository, times(1)).findByCategoryAndLastPostOrderByDateDesc(any(), any(), any(), anyInt(), anyBoolean());

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
        when(moimPostRepository.findWithMoimAndMemberById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> moimPostService.getMoimPosts(1L, 1234L, null, 0, member)).isInstanceOf(MoimingApiException.class);

    }


}