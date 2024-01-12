package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;
import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class MoimServiceTest {

    @InjectMocks
    private MoimService moimService;
    @Mock
    private MoimRepository moimRepository;
    @Mock
    private MoimMemberRepository moimMemberRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private MoimPostRepository moimPostRepository;
    @Mock
    private PostCommentRepository postCommentRepository;
    @Mock
    private MoimCategoryLinkerRepository moimCategoryLinkerRepository;


    // 성공
    @Test
    void getMemberMoims_shouldProcess_whenRightInfoWithNotNullMoimIdPassed() {

        // given
        Member member = mock(Member.class);
        Moim lastMoim = mock(Moim.class);
        Long lastMoimId = 1L; // Not Null

        // given - stub
        when(moimRepository.findById(lastMoimId)).thenReturn(Optional.of(lastMoim));

        // when
        moimService.getMemberMoims(lastMoimId, false, false, 0, member); // ANY VALUE

        // then
        verify(moimMemberRepository, times(1)).findMemberMoimsWithRuleAndCategoriesByConditionsPaged(any(), anyBoolean(), anyBoolean(), any(), anyInt());

    }


    // 성공 lastMoimId = Null 이여도
    @Test
    void getMemberMoims_shouldProcess_whenRightInfoWithNullMoimIdPassed() {

        // given
        Member member = mock(Member.class);
        Long lastMoimId = null; // Not Null

        // when
        moimService.getMemberMoims(lastMoimId, false, false, 0, member); // ANY VALUE

        // then
        verify(moimMemberRepository, times(1)).findMemberMoimsWithRuleAndCategoriesByConditionsPaged(any(), anyBoolean(), anyBoolean(), any(), anyInt());

    }


    // 실패 member null
    @Test
    void getMemberMoims_shouldThrowException_whenMemberNull_byMoimingApiException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> moimService.getMemberMoims(null, false, false, 0, null)).isInstanceOf(MoimingApiException.class);

    }


    @Test
    void updateMoim_shouldProcess_whenRightInfoPassed() {

        // given
        Moim mockMoim = mock(Moim.class);
        Member mockMember = mock(Member.class);
        MoimMember mockMoimMember = mock(MoimMember.class);
        MoimUpdateReqDto reqDto = mock(MoimUpdateReqDto.class); // updateMoim 에서 validation 하는 부분이 없음 - 뭐가 들었든 관심 없음

        // given - stub
        doReturn(Optional.ofNullable(mockMoimMember)).when(moimMemberRepository).findByMemberAndMoimId(any(), any()); // 뭘 찾아오든 상관 없음, 동작만 하면 됨
        doReturn(true).when(mockMoimMember).hasPermissionOfManager(); // 이것도 state 자체를 바꾼다면 hasPermissionUpdate 가 뭔지 확인하러 들어가봐야 함 (이 함수의 동작은 아무짝에 관심없음)
        doReturn(new ArrayList<>()).when(categoryService).generateCategoryList(any()); // generateCategoryList 가 잘되는지는 거기서 확인하게된다
        doReturn(mockMoim).when(mockMoimMember).getMoim(); // mockMoim 반환하도록 한다

        // when
        moimService.updateMoim(reqDto, mockMember);

        // then
        verify(mockMoim, times(1)).updateMoim(any(), any(), any()); // 호출을 확인한다

        // return val 은 통합을 통해 확인할 수 있다
    }

    // requestCategory 가 채워있냐 비어있냐 -> Moim.updateMoim() 에서 수행해야함
    // update 가 잘되는지 -> Moim.updateMoim() 에서 수행해야 한다


    // updateMoim 테스트에서는 직접 예외상황이 될 수 있는 부분을 처리해준다
    @Test
    void updateMoim_shouldThrowException_whenMoimMemberNotFound_byMoimingApiException() {

        // given
        Member mockMember = mock(Member.class);
        MoimUpdateReqDto mockDto = mock(MoimUpdateReqDto.class);

        // given - stub
        doReturn(Optional.empty()).when(moimMemberRepository).findByMemberAndMoimId(any(), any()); // 뭘 넣든 상관 없이, 당장 예외상황이 나야 하는 부분을 검증

        // when
        // then
        assertThatThrownBy(() -> moimService.updateMoim(mockDto, mockMember)).isInstanceOf(MoimingApiException.class);

    }


    @Test
    void updateMoim_shouldThrowException_whenMemberDoesNotHavePermission_byMoimingApiException() {

        // given
        Member mockMember = mock(Member.class);
        MoimMember mockMoimMember = mock(MoimMember.class);
        MoimUpdateReqDto reqDto = mock(MoimUpdateReqDto.class);

        // given - stub
        doReturn(Optional.ofNullable(mockMoimMember)).when(moimMemberRepository).findByMemberAndMoimId(any(), any());
        doReturn(false).when(mockMoimMember).hasPermissionOfManager();

        // when
        // then
        assertThatThrownBy(() -> moimService.updateMoim(reqDto, mockMember)).isInstanceOf(MoimingApiException.class);

    }


    // getMoimDetail - 성공
    @Test
    void getMoimDetail_shouldPass_whenRightInfoPassed() {

        // given
        Long moimId = 1L;
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);
        MoimMember creator = mock(MoimMember.class);

        // given - stub
        when(moimRepository.findWithJoinRuleAndCategoryById(any())).thenReturn(Optional.of(moim));
        when(moimMemberRepository.findWithMemberByMemberAndMoimId(any(), any())).thenReturn(Optional.of(creator));

        // when
        moimService.getMoimDetail(moimId, member);

        // then
        verify(moimRepository, times(1)).findWithJoinRuleAndCategoryById(any());

    }


    // getMoimDetail - 실패 : NULL PARAM
    @Test
    void getMoimDetail_shouldThrowException_whenParamNull_byMoimingApiException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> moimService.getMoimDetail(null, null)).isInstanceOf(MoimingApiException.class);

    }


    // getMoimDetail - 실패 : MOIM NOT FOUND
    @Test
    void getMoimDetail_shouldThrowException_whenMoimNotFound_byMoimingApiException() {

        // given
        Long moimId = 1L;
        Member member = mock(Member.class);

        // given - stub
        when(moimRepository.findWithJoinRuleAndCategoryById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> moimService.getMoimDetail(moimId, member)).isInstanceOf(MoimingApiException.class);

    }


    // deleteMoim - 성공 - JoinRule 이 없어도 성공한다. 있으면 있다고 stubbing 필요
    @Test
    void deleteMoim_shouldPass_whenRightInfoPassed() {

        // given - Moim 에 MoimPost 가 두 개있다고 가정한다
        Long moimId = 1L;
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);
        MoimPost moimPost1 = mock(MoimPost.class);
        MoimPost moimPost2 = mock(MoimPost.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasPermissionOfManager()).thenReturn(true);
        when(moimRepository.findWithJoinRuleAndCategoryById(any())).thenReturn(Optional.of(moim));
        when(moimPostRepository.findByMoimId(any())).thenReturn(List.of(moimPost1, moimPost2));

        // when
        moimService.deleteMoim(moimId, member);

        // then
        verify(postCommentRepository, times(2)).removeAllByMoimPostId(any()); // moim 은 Null 아니라 괜찮음
        verify(moimPostRepository, times(1)).removeAllByMoimId(any());
        verify(moimCategoryLinkerRepository, times(1)).removeAllByMoimId(any());
        verify(moimRepository, times(1)).remove(any());

    }


    // deleteMoim - 실패 : NULL PARAM
    @Test
    void deleteMoim_shouldThrowException_whenParamNull_byMoimingApiException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> moimService.deleteMoim(null, null)).isInstanceOf(MoimingApiException.class);

    }


    // deleteMoim - 실패 : MOIM MEMBER NOT FOUND
    @Test
    void deleteMoim_shouldThrowException_whenMoimMemberNotFound_byMoimingApiException() {

        // given
        Long moimId = 1L;
        Member member = mock(Member.class);

        // when
        // then
        assertThatThrownBy(() -> moimService.deleteMoim(moimId, member)).isInstanceOf(MoimingApiException.class);

    }


    // deleteMoim - 실패 : MEMBER NOT AUTHORIZED
    @Test
    void deleteMoim_shouldThrowException_whenMoimMemberNotManager_byMoimingApiException() {

        // given
        Long moimId = 1L;
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimRepository.findWithJoinRuleAndCategoryById(any())).thenReturn(Optional.of(moim));

        // when
        // then
        assertThatThrownBy(() -> moimService.deleteMoim(moimId, member)).isInstanceOf(MoimingApiException.class);

    }


    // deleteMoim - 실패 : MOIM NOT FOUND
    @Test
    void deleteMoim_shouldThrowException_whenMoimNotFound_byMoimingApiException() {

        // given
        Long moimId = 1L;
        Member member = mock(Member.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimRepository.findWithJoinRuleAndCategoryById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> moimService.deleteMoim(moimId, member)).isInstanceOf(MoimingApiException.class);

    }

}

