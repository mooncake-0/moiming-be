package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimRepository;
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
public class MoimServiceSecondTest {

    @InjectMocks
    private MoimService moimService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private MoimRepository moimRepository;

    @Mock
    private MoimMemberRepository moimMemberRepository;

    // TODO:: 정적 팩토리 패턴에 대한 mocking 은 어떻게 해야하는지 점검 필요
//    @Test
    void createMoim_shouldProcess_whenRightInfoPassed() { // REQUESTDTO 를 가지고 VERIFY 해주는 친구는 없음

        // given
        MoimCreateReqDto mockReqDto = mock(MoimCreateReqDto.class);
        Moim mockMoim = mock(Moim.class);
        Member mockMember = mock(Member.class);
    }


    @Test
    void getMemberMoims_shouldProcess_whenRightInfoPassed() {

        // given
        Member mockMember = mock(Member.class);
        List<MoimMember> mockMembers = mock(ArrayList.class);

        // given - stub
        doReturn(mockMembers).when(moimMemberRepository).findWithMoimAndCategoryByMemberId(any()); // MoimMember 들을 알아서 찾아볼 것

        // when
        moimService.getMemberMoims(mockMember);

        // then
        verify(mockMembers, times(1)).stream(); // 응답 형성 시도를 진행한다
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

}

