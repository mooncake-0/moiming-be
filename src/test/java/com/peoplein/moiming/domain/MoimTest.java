package com.peoplein.moiming.domain;


import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.request.MoimReqDto;
import com.peoplein.moiming.support.TestMockCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class MoimTest extends TestMockCreator {


    // 생성 필수 정보
    private Member mockMember;
    private List<Category> mockCategories = new ArrayList<>();

    @BeforeEach
    void be_moim_info_su() {
        mockMember = mockMember(1L, memberEmail, memberName, memberPhone, ci, new Role(1L, "", RoleType.USER));
        Category parent = mockCategory(1L, CategoryName.DANCE, 1, null);
        mockCategories.add(parent);
        mockCategories.add(mockCategory(2L, CategoryName.SWING_DANCE, 2, parent));
    }


    @Test
    void createMoim_shouldCreateMoim_whenRightInfoPassed() {

        // given
        // when
        Moim moim = Moim.createMoim(moimName, moimInfo, maxMember, moimArea, mockCategories, mockMember);

        // then
        assertThat(moim.getMoimName()).isEqualTo(moimName);
        assertThat(moim.getCreatorId()).isEqualTo(mockMember.getId());
        assertThat(moim.getCurMemberCount()).isEqualTo(1);
        assertThat(moim.getMoimMembers().size()).isEqualTo(1);
        assertThat(moim.getMoimCategoryLinkers().size()).isEqualTo(2);

    }

    @Test
    void addCurMemberCount_shouldAddCurMemberCount_whenSuccessful() {
        // given
        Moim moim = Moim.createMoim(moimName, moimInfo, maxMember, moimArea, mockCategories, mockMember);

        // when
        moim.addCurMemberCount();

        // then
        assertThat(moim.getCurMemberCount()).isEqualTo(2);
    }


    @Test
    void minusCurMemberCount_shouldMinusCurMemberCount_whenSuccessful() {
        // given
        Moim moim = Moim.createMoim(moimName, moimInfo, maxMember, moimArea, mockCategories, mockMember);
        moim.addCurMemberCount(); // 위에서 검증한대로 +1 이 되므로 2일 것

        // when
        moim.minusCurMemberCount(); // -1 이 되는지

        // then
        assertThat(moim.getCurMemberCount()).isEqualTo(1);
    }


    @Test
    void minusCurMemberCount_shouldThrowException_whenCurCountIsOne() {
        // given
        Moim moim = Moim.createMoim(moimName, moimInfo, maxMember, moimArea, mockCategories, mockMember);

        // when
        // then
        assertThatThrownBy(moim::minusCurMemberCount).isInstanceOf(MoimingApiException.class);
    }



    // TODO :: 미뤄뒀던 updateMoim 함수를 Test 해야한다 - value 확인이 필요한 test
    //         외부 개입이 없이 함수의 연산에 대한 결과 확인은 value 확인이 필요하다
    //         requestDto 는 validate 되므로 notnull - 필요 없는 test (Controller 에선 해보자)

    // 정상 Test
    // moimName, maxMember, area 하나 에 대한 각각 변경 진행
    // 3가지가 변경되었을 떄 진행
    // category 가 두개 다 잘 왔을때
    // Categories 역시 NotNull (CategoryService 에서 검증함)
    // 비었거나, 두개 들었거나 only


    @Test
    void updateMoim_shouldUpdateMoimName_whenRightInfoPassed() {

        // given
        Member changeReqMember = mock(Member.class);
        doReturn(2L).when(changeReqMember).getId();

        // given - 기존 모임이 있음
        Moim mockMoim = mockMoimWithoutRuleJoin(1L, moimName, maxMember, depth1SampleCategory, depth2SampleCategory, mockMember);

        // given - 수정에 대해 요청함 (수정 안할 필드는 Null)
        MoimUpdateReqDto reqDto = mockMoimUpdateReqDto(mockMoim.getId(), moimName2, null, null, null);

        // when
        mockMoim.updateMoim(reqDto, new ArrayList<>(), changeReqMember.getId()); // 카테고리 변경사항 없으면 빈 Array 들어감

        // then
        assertThat(mockMoim.getMoimName()).isEqualTo(moimName2);
        assertThat(mockMoim.getMoimInfo()).isEqualTo(moimInfo);
        assertThat(mockMoim.getMaxMember()).isEqualTo(maxMember);
        assertThat(mockMoim.getMoimArea().getState()).isEqualTo(moimArea.getState());
        assertThat(mockMoim.getUpdaterId()).isEqualTo(changeReqMember.getId());

    }



    @Test
    void updateMoim_shouldUpdateMoimMaxMember_whenRightInfoPassed() {

        // given
        Member changeReqMember = mock(Member.class);
        doReturn(2L).when(changeReqMember).getId();

        // given - 기존 모임이 있음
        Moim mockMoim = mockMoimWithoutRuleJoin(1L, moimName, maxMember, depth1SampleCategory, depth2SampleCategory, mockMember);

        // given - 수정에 대해 요청함 (수정 안할 필드는 Null)
        MoimUpdateReqDto reqDto = mockMoimUpdateReqDto(mockMoim.getId(), null, maxMember2, null, null);

        // when
        mockMoim.updateMoim(reqDto, new ArrayList<>(), changeReqMember.getId()); // 카테고리 변경사항 없으면 빈 Array 들어감

        // then
        assertThat(mockMoim.getMoimName()).isEqualTo(moimName);
        assertThat(mockMoim.getMoimInfo()).isEqualTo(moimInfo);
        assertThat(mockMoim.getMaxMember()).isEqualTo(maxMember2);
        assertThat(mockMoim.getMoimArea().getState()).isEqualTo(moimArea.getState());
    }



    // 한번에 여러 정보 수정 요청
    @Test
    void updateMoim_shouldUpdatePassedMoimInfo_whenRightInfoPassed() {

        // given
        Member changeReqMember = mock(Member.class);
        doReturn(2L).when(changeReqMember).getId();

        // given - 기존 모임이 있음
        Moim mockMoim = mockMoimWithoutRuleJoin(1L, moimName, maxMember, depth1SampleCategory, depth2SampleCategory, mockMember);

        // given - 수정에 대해 요청함 (수정 안할 필드는 Null)
        MoimUpdateReqDto reqDto = mockMoimUpdateReqDto(mockMoim.getId(), moimName2, maxMember2, null, moimArea2.getCity());

        // when
        mockMoim.updateMoim(reqDto, new ArrayList<>(), changeReqMember.getId()); // 카테고리 변경사항 없으면 빈 Array 들어감

        // then
        assertThat(mockMoim.getMoimName()).isEqualTo(moimName2);
        assertThat(mockMoim.getMoimInfo()).isEqualTo(moimInfo);
        assertThat(mockMoim.getMaxMember()).isEqualTo(maxMember2);
        assertThat(mockMoim.getMoimArea().getCity()).isEqualTo(moimArea2.getCity());

    }



    @Test
    void updateMoim_shouldUpdateCategories_whenRightInfoPassed() {


        // given
        Member changeReqMember = mock(Member.class);
        doReturn(2L).when(changeReqMember).getId();

        // given - 기존 모임이 있음
        Moim mockMoim = mockMoimWithoutRuleJoin(1L, moimName, maxMember, depth1SampleCategory, depth2SampleCategory, mockMember);

        // given - 수정에 대해 요청함 (수정 안할 필드는 Null)
        MoimUpdateReqDto reqDto = mockMoimUpdateReqDto(mockMoim.getId(), moimName2, null, null, null);
        Category mockCategory1 = mockCategory(1L, CategoryName.fromValue(depth2SampleCategory), 1, null);
        Category mockCategory2 = mockCategory(2L, CategoryName.fromValue(depth2SampleCategory2), 2, mockCategory1);


        // when
        mockMoim.updateMoim(reqDto, List.of(mockCategory1, mockCategory2), changeReqMember.getId()); // 카테고리 변경사항 없으면 빈 Array 들어감


        // then
        assertThat(mockMoim.getMoimName()).isEqualTo(moimName2);
        assertThat(mockMoim.getMoimInfo()).isEqualTo(moimInfo);
        assertThat(mockMoim.getMoimCategoryLinkers().size()).isEqualTo(2);

        for (MoimCategoryLinker mcLinker : mockMoim.getMoimCategoryLinkers()) {
            if (mcLinker.getCategory().getCategoryDepth() == 1) {
                assertThat(mcLinker.getCategory().getCategoryName()).isEqualTo(CategoryName.fromValue(depth2SampleCategory));
            } else {
                assertThat(mcLinker.getCategory().getCategoryName()).isEqualTo(CategoryName.fromValue(depth2SampleCategory2));
            }
        }
    }

}