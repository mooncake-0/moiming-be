package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimDailyCount;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.*;
import com.peoplein.moiming.repository.jpa.FileJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private MoimPostRepository moimPostRepository;
    @Mock
    private PostCommentRepository postCommentRepository;
    @Mock
    private MoimCategoryLinkerRepository moimCategoryLinkerRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private MoimCountService moimCountService;
    @Mock
    private MoimCountRepository moimCountRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private FileJpaRepository fileJpaRepository;


    // updateImg 성공 - 모임 생성이라 moimId 없음
    @Test
    void updateImg_shouldPass_whenMoimIdNotPassedForNewMoim() {

        // given
        Member member = mock(Member.class);

        // when
        moimService.updateImg(null, null, member);

        // then
        verify(storageService, times(1)).uploadMoimImg(any());

    }


    // updateImg 성공 - 모임 생성이라 moimId 없음
    @Test
    void updateImg_shouldPass_whenMoimIdPassedForUpdate() {

        // given
        Long moimId = 1L;
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);

        // given - stub
        when(moimRepository.findById(any())).thenReturn(Optional.of(moim));
        when(member.getId()).thenReturn(2L);
        when(moim.getCreatorId()).thenReturn(2L);

        // when
        moimService.updateImg(null, moimId, member);

        // then
        verify(storageService, times(1)).uploadMoimImg(any());
        verify(storageService, times(1)).deleteMoimImg(any());
        verify(moim, times(1)).deleteImg();
        verify(moim, times(1)).changeImg(any());

    }


    // updateImg - 실패 - Moim Not Found
    @Test
    void updateImg_shouldThrowException_whenUpdateMoimNotFound_byMoimingApiException() {

        // given
        Long moimId = 1L;
        Member member = mock(Member.class);

        // given - stub
        when(moimRepository.findById(any())).thenReturn(Optional.empty());

        // when
        assertThatThrownBy(() -> moimService.updateImg(null, moimId, member)).isInstanceOf(MoimingApiException.class);

        // then
        verify(storageService, times(1)).uploadMoimImg(any());
        verify(storageService, times(0)).deleteMoimImg(any());

    }


    // updateImg - 실패 - Request Not Moim Creator
    @Test
    void updateImg_shouldThrowException_whenReqByNotMoimCreator_byMoimingApiException() {

        // given
        Long moimId = 1L;
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);

        // given - stub
        when(moimRepository.findById(any())).thenReturn(Optional.of(moim));
        when(member.getId()).thenReturn(2L);
        when(moim.getCreatorId()).thenReturn(3L); // 둘이 다름 적용

        // when
        assertThatThrownBy(() -> moimService.updateImg(null, moimId, member)).isInstanceOf(MoimingApiException.class);

        // then
        verify(storageService, times(1)).uploadMoimImg(any());
        verify(storageService, times(0)).deleteMoimImg(any());

    }


    // delete Img - 성공
    @Test
    void deleteImg_shouldPass_whenRightInfoPassed() {

        // given
        Long moimId = 1L;
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);

        // given - stub
        when(moimRepository.findById(any())).thenReturn(Optional.of(moim));
        when(member.getId()).thenReturn(1L);
        when(moim.getCreatorId()).thenReturn(1L); // 모임 운영자의 요청임
        when(moim.getImgFileId()).thenReturn(1L); // 모임에는 사진이 있음

        // when
        moimService.deleteImg(moimId, member);

        // then
        verify(storageService, times(1)).deleteMoimImg(any());
        verify(moim, times(1)).deleteImg();

    }


    // delete Img - 실패 : Moim 없음
    @Test
    void deleteImg_shouldThrowException_whenMoimNotFound_byMoimingApiException() {

        // given
        Long moimId = 1L;
        Member member = mock(Member.class);

        // given - stub
        when(moimRepository.findById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> moimService.deleteImg(moimId, member)).isInstanceOf(MoimingApiException.class);
        verify(storageService, times(0)).deleteMoimImg(any());
    }


    // delete Img - 실패 : 운영자 아님
    @Test
    void deleteImg_shouldThrowException_whenReqByNotMoimCreator_byMoimingApiException() {

        // given
        Long moimId = 1L;
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);

        // given - stub
        when(moimRepository.findById(any())).thenReturn(Optional.of(moim));
        when(member.getId()).thenReturn(1L);
        when(moim.getCreatorId()).thenReturn(2L); // 모임 운영자의 요청임

        // when
        // then
        assertThatThrownBy(() -> moimService.deleteImg(moimId, member)).isInstanceOf(MoimingApiException.class);
        verify(storageService, times(0)).deleteMoimImg(any());
        verify(moim, times(0)).deleteImg();

    }


    // delete Img - 실패 : 이미 사진 없는 모임
    @Test
    void deleteImg_shouldThrowException_whenMoimImgEmpty_byMoimingApiException() {

        // given
        Long moimId = 1L;
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);

        // given - stub
        when(moimRepository.findById(any())).thenReturn(Optional.of(moim));
        when(member.getId()).thenReturn(1L);
        when(moim.getCreatorId()).thenReturn(1L); // 모임 운영자의 요청임
        when(moim.getImgFileId()).thenReturn(null); // Mock 객체는 Long 은 get 했을 때 0 이 반환되는듯? null 이라고 명시해야함

        // when
        // then
        assertThatThrownBy(() -> moimService.deleteImg(moimId, member)).isInstanceOf(MoimingApiException.class);
        verify(storageService, times(0)).deleteMoimImg(any());
        verify(moim, times(0)).deleteImg();

    }


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
        moimService.getMemberMoims(lastMoimId, false, 0, member); // ANY VALUE

        // then
        verify(moimMemberRepository, times(1)).findMemberMoimsWithCursorConditions(any(), anyBoolean(), anyBoolean(), any(), anyInt());

    }


    // 성공 lastMoimId = Null 이여도
    @Test
    void getMemberMoims_shouldProcess_whenRightInfoWithNullMoimIdPassed() {

        // given
        Member member = mock(Member.class);
        Long lastMoimId = null; // Not Null

        // when
        moimService.getMemberMoims(lastMoimId, false, 0, member); // ANY VALUE

        // then
        verify(moimMemberRepository, times(1)).findMemberMoimsWithCursorConditions(any(), anyBoolean(), anyBoolean(), any(), anyInt());

    }


    // 실패 member null
    @Test
    void getMemberMoims_shouldThrowException_whenMemberNull_byMoimingApiException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> moimService.getMemberMoims(null, false, 0, null)).isInstanceOf(MoimingApiException.class);

    }


    @Test
    void updateMoim_shouldProcess_whenRightInfoPassed() {

        // given
        Moim moim = mock(Moim.class);
        Member member = mock(Member.class);
        MoimMember moimMember = mock(MoimMember.class);
        MoimUpdateReqDto reqDto = mock(MoimUpdateReqDto.class); // updateMoim 에서 validation 하는 부분이 없음 - 뭐가 들었든 관심 없음

        // given - stub
        when(moimMemberRepository.findWithMoimAndCategoriesByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasPermissionOfManager()).thenReturn(true);
        when(moimMember.getMoim()).thenReturn(moim);

        // when
        moimService.updateMoim(reqDto, member);

        // then
        verify(moim, times(1)).updateMoim(any(), any(), any()); // 호출을 확인한다

    }

    // requestCategory 가 채워있냐 비어있냐 -> Moim.updateMoim() 에서 수행해야함
    // update 가 잘되는지 -> Moim.updateMoim() 에서 수행해야 한다


    // updateMoim 테스트에서는 직접 예외상황이 될 수 있는 부분을 처리해준다
    @Test
    void updateMoim_shouldThrowException_whenMoimMemberNotFound_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        MoimUpdateReqDto reqDto = mock(MoimUpdateReqDto.class);

        // when
        // then
        assertThatThrownBy(() -> moimService.updateMoim(reqDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // updateMoim - 실패 : NOT MANAGER
    @Test
    void updateMoim_shouldThrowException_whenMemberDoesNotHavePermission_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        MoimMember moimMember = mock(MoimMember.class);
        MoimUpdateReqDto reqDto = mock(MoimUpdateReqDto.class);

        // given - stub
        when(moimMemberRepository.findWithMoimAndCategoriesByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));

        // when
        // then
        assertThatThrownBy(() -> moimService.updateMoim(reqDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // 가입 조건 수정
    // 성공 : 가입 조건이 이미 있을 때
    @Test
    void updateMoimJoinRule_shouldPass_whenMoimWithJoinRulePassed() {

        // given
        MoimJoinRuleUpdateReqDto reqDto = mock(MoimJoinRuleUpdateReqDto.class);
        Member member = mock(Member.class);
        MoimMember moimMember = mock(MoimMember.class);
        Moim moim = mock(Moim.class);
        MoimJoinRule joinRule = mock(MoimJoinRule.class);

        // given - stub
        when(moimRepository.findWithJoinRuleById(any())).thenReturn(Optional.of(moim));
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
        when(moimMember.hasPermissionOfManager()).thenReturn(true);
        when(moim.getMoimJoinRule()).thenReturn(joinRule);

        // when
        moimService.updateMoimJoinRule(reqDto, member);

        // then
        verify(joinRule, times(1)).changeJoinRule(anyBoolean(), anyInt(), anyInt(), any());

    }


    // 가입 조건 수정
    // 성공 : 가입 조건이 이미 있을 때
    @Test
    void updateMoimJoinRule_shouldPass_whenMoimWithNoJoinRulePassed() {

        try (MockedStatic<MoimJoinRule> mocker = mockStatic(MoimJoinRule.class)) {
            // given
            MoimJoinRuleUpdateReqDto reqDto = mock(MoimJoinRuleUpdateReqDto.class);
            Member member = mock(Member.class);
            MoimMember moimMember = mock(MoimMember.class);
            Moim moim = mock(Moim.class);

            // given - stub
            when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));
            when(moimMember.hasPermissionOfManager()).thenReturn(true);
            when(moimRepository.findWithJoinRuleById(any())).thenReturn(Optional.of(moim));
            mocker.when(() -> MoimJoinRule.createMoimJoinRule(anyBoolean(), anyInt(), anyInt(), any()))
                    .thenReturn(null); // 해당 함수는 호출만 하고, 결과는 상관 없다

            // when
            moimService.updateMoimJoinRule(reqDto, member);

            // then
            verify(moim, times(1)).setMoimJoinRule(any());

        }

    }


    // updateMoimJoinRule - 실패 : INVALID PARAM
    @Test
    void updateMoimJoinRule_shouldThrowException_whenParamNull_byMoimingApiException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> moimService.updateMoimJoinRule(null, null)).isInstanceOf(MoimingApiException.class);

    }


    // updateMoimJoinRule - 실패 : 비모임원의 요청
    @Test
    void updateMoimJoinRule_shouldThrowException_whenReqMoimMemberNotFound_byMoimingApiException() {

        // given
        MoimJoinRuleUpdateReqDto reqDto = mock(MoimJoinRuleUpdateReqDto.class);
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);

        // given - stub
        when(moimRepository.findWithJoinRuleById(any())).thenReturn(Optional.of(moim));

        // when
        // then
        assertThatThrownBy(() -> moimService.updateMoimJoinRule(reqDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // updateMoimJoinRule - 실패 : 비운영자의 요청
    @Test
    void updateMoimJoinRule_shouldThrowException_whenMoimMemberNotManager_byMoimingApiException() {

        // given
        MoimJoinRuleUpdateReqDto reqDto = mock(MoimJoinRuleUpdateReqDto.class);
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);
        MoimMember moimMember = mock(MoimMember.class);

        // given - stub
        when(moimRepository.findWithJoinRuleById(any())).thenReturn(Optional.of(moim));
        when(moimMemberRepository.findByMemberAndMoimId(any(), any())).thenReturn(Optional.of(moimMember));

        // when
        // then
        assertThatThrownBy(() -> moimService.updateMoimJoinRule(reqDto, member)).isInstanceOf(MoimingApiException.class);


    }


    // updateMoimJoinRule - 실패 : 모임을 찾을 수 없음
    @Test
    void updateMoimJoinRule_shouldThrowException_whenMoimNotFound_byMoimingApiException() {

        // given
        MoimJoinRuleUpdateReqDto reqDto = mock(MoimJoinRuleUpdateReqDto.class);
        Member member = mock(Member.class);

        // when
        // then
        assertThatThrownBy(() -> moimService.updateMoimJoinRule(reqDto, member)).isInstanceOf(MoimingApiException.class);

    }


    // getMoimDetail - 성공
    @Test
    void getMoimDetail_shouldPass_whenMemberFirstVisitTodayWithRightInfoPassed() {

        // given
        Long moimId = 1L;
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);
        MoimMember creator = mock(MoimMember.class);

        // given - stub
        when(moimRepository.findWithJoinRuleAndCategoriesById(any())).thenReturn(Optional.of(moim));
        when(moimMemberRepository.findWithMemberAndInfoByMemberAndMoimId(any(), any())).thenReturn(Optional.of(creator));


        // when
        moimService.getMoimDetail(moimId, member);

        // then
        verify(moimRepository, times(1)).findWithJoinRuleAndCategoriesById(any());
        verify(moimCountService, times(1)).processMoimCounting(any(), any());

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
        when(moimRepository.findWithJoinRuleAndCategoriesById(any())).thenReturn(Optional.empty());

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
        when(moimRepository.findWithJoinRuleAndCategoriesById(any())).thenReturn(Optional.of(moim));
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
        when(moimRepository.findWithJoinRuleAndCategoriesById(any())).thenReturn(Optional.of(moim));

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
        when(moimRepository.findWithJoinRuleAndCategoriesById(any())).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> moimService.deleteMoim(moimId, member)).isInstanceOf(MoimingApiException.class);

    }


    // getSuggestedMoim - 성공 - 무슨 값들이 오가는지 아무 상관 안한다
    @Test
    void getSuggestedMoim_shouldPass_whenRightInfoPassed() {

        // given
        // given - stub
        // when
        moimService.getSuggestedMoim(null, null, 0, 0);

        // then
        verify(moimCountRepository, times(1)).findMonthlyBySuggestedCondition(any(), any(), any(), anyInt(), anyInt());
        verify(moimCategoryLinkerRepository, times(0)).findWithCategoryByMoimIds(any());
    }


}

