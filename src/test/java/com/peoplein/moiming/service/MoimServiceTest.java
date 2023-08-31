package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.CategoryRepository;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.support.TestMockCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;

import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class MoimServiceTest extends TestMockCreator {

    @Spy
    @InjectMocks
    private MoimService moimService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MoimRepository moimRepository;

    @Mock
    private MoimMemberRepository moimMemberRepository;

    private Member curMember;


    @BeforeEach
    void be() {
        curMember = mockMember(1L, memberEmail, memberName, memberPhone, mockRole(1L, RoleType.USER));
    }


    @Test
    void createMoim_shouldReturnMoimEntity_whenSuccessful() {

        // given
        MoimCreateReqDto requestDto = mockMoimCreateReqDto(moimName, maxMember, true, true, 10, 40
                , MemberGender.N, depth1SampleCategory, depth2SampleCategory);

        // given - return val ready
        List<Category> categories = new ArrayList<>();
        Category depth1 = mockCategory(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        Category depth2 = mockCategory(2L, CategoryName.fromValue(depth2SampleCategory), 2, depth1);
        categories.add(depth1);
        categories.add(depth2);

        // given - stub (필요 순서대로
        doReturn(categories).when(categoryRepository).findByCategoryNames(any());
        doNothing().when(moimRepository).save(any());

        // when
        Moim moimOut = moimService.createMoim(requestDto, curMember);

        // then
        assertThat(moimOut.getMoimName()).isEqualTo(moimName);
        assertThat(moimOut.getMaxMember()).isEqualTo(maxMember);
        assertThat(moimOut.getCurMemberCount()).isEqualTo(1);
        assertFalse(Objects.isNull(moimOut.getMoimJoinRule()));
        assertThat(moimOut.getMoimJoinRule().getAgeMin()).isEqualTo(10);
        assertThat(moimOut.getMoimCategoryLinkers().size()).isEqualTo(2);

    }


    @Test
    void createMoim_shoulReturnMoimEntity_whenSuccessfulWithoutJoinRule() {

        // given - hasJoinRule - false 이므로 column 값들은 어떤 값이 들어오든 매핑이 안된다
        MoimCreateReqDto requestDto = mockMoimCreateReqDto(moimName, maxMember, false, false, 0, 0
                , MemberGender.N, depth1SampleCategory, depth2SampleCategory);

        // given - return val ready
        List<Category> categories = new ArrayList<>();
        Category depth1 = mockCategory(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        Category depth2 = mockCategory(2L, CategoryName.fromValue(depth2SampleCategory), 2, depth1);
        categories.add(depth1);
        categories.add(depth2);


        // given - stub (필요 순서대로
        doReturn(categories).when(categoryRepository).findByCategoryNames(any());
        doNothing().when(moimRepository).save(any());


        // when
        Moim moimOut = moimService.createMoim(requestDto, curMember);

        // then
        assertThat(moimOut.getMoimName()).isEqualTo(moimName);
        assertThat(moimOut.getMaxMember()).isEqualTo(maxMember);
        assertThat(moimOut.getCurMemberCount()).isEqualTo(1);
        assertTrue(Objects.isNull(moimOut.getMoimJoinRule()));
        assertThat(moimOut.getMoimCategoryLinkers().size()).isEqualTo(2);
    }


    @Test
    void createMoim_shouldThrowException_whenCategoryParentMatchWrong() {

        // given
        String wrongCategoryVal = "댄스/무용";
        MoimCreateReqDto requestDto = mockMoimCreateReqDto(moimName, maxMember, false, false, 0, 0
                , MemberGender.N, depth1SampleCategory, depth2SampleCategory);

        // given - return val ready
        List<Category> categories = new ArrayList<>();
        Category wrongParent = mockCategory(3L, CategoryName.fromValue(wrongCategoryVal), 1, null);
        Category depth1 = mockCategory(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        Category depth2 = mockCategory(2L, CategoryName.fromValue(depth2SampleCategory), 2, wrongParent); // 부모가 다른 Category 였지만 depth1 과 들어옴
        categories.add(depth1);
        categories.add(depth2);

        // when
        // then
        assertThatThrownBy(() -> moimService.createMoim(requestDto, curMember)).isInstanceOf(MoimingApiException.class);
    }


    @Test
    void getMemberMoims_shouldReturnCollection_whenSuccessful() {

        // given - curMember 가 가입된 모임이 두개이다
        Moim mockedMoim = mockMoim(1L, moimName, maxMember, true, true, 40, 20, MemberGender.N, depth1SampleCategory, depth2SampleCategory, curMember);
        Moim mockedMoim2 = mockMoim(2L, moimName2, maxMember2, false, false, 0, 0, null, depth2SampleCategory, depth2SampleCategory2, curMember);



        // when
//        doReturn().when(moimMemberRepository).findWithMoimAndCategoryByMemberId(curMember.getId());

        // then

    }
}