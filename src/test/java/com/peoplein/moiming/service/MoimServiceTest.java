package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.response.MoimRespDto;
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
import java.util.Optional;

import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;

import static com.peoplein.moiming.model.dto.response.MoimRespDto.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MoimServiceTest extends TestMockCreator {

    @Spy
    @InjectMocks
    private MoimService moimService;

    @Mock
    private CategoryService categoryService;

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
        doReturn(categories).when(categoryService).generateCategoryList(any());
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
        doReturn(categories).when(categoryService).generateCategoryList(any());
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



    // 어차피 mock 써도 get val 값들 다 넣어줘야함.. 그냥 실객체로 Mocking 처럼 쓰자\
    // 상당히 잘못된 방향이긴 한듯... return value 에 집착하지 말아라....
    @Test
    void getMemberMoims_shouldReturnCollection_whenSuccessful() {

        // given
        // given - return val ready
        Moim moim1 = mockMoimWithoutRuleJoin(1L, moimName, maxMember, depth1SampleCategory, depth2SampleCategory, curMember);
        Moim moim2 = mockMoimWithRuleJoin(2L, moimName2, maxMember2, true, 20, 15, null, depth1SampleCategory2, depth2SampleCategory2, curMember);
        MoimMember moimMember = mockMoimMember(1L, curMember, moim1);
        MoimMember moimMember2 = mockMoimMember(2L, curMember, moim2);
        List<MoimMember> mockedMoimMembers = List.of(moimMember, moimMember2);

        // given - stub
        doReturn(mockedMoimMembers).when(moimMemberRepository).findWithMoimAndCategoryByMemberId(curMember.getId());

        // when
        List<MoimViewRespDto> curMemberMoims = moimService.getMemberMoims(curMember);

        // then
        assertThat(curMemberMoims.size()).isEqualTo(2);

        // 원하는대로 들어갔는지 확인한다
        for (MoimViewRespDto curMemberMoim : curMemberMoims) {
            if (curMemberMoim.getMoimJoinRuleDto() == null) {
                assertThat(curMemberMoim.getMoimId()).isEqualTo(1L);
            } else {
                assertThat(curMemberMoim.getMoimId()).isEqualTo(2L);
            }
        }
    }


    // 상호 작용 기반 테스트
    @Test
    void updateMoim_shouldReturnMoimEntity_whenSuccessful() {

        // given
        MoimUpdateReqDto mockReqDto = mock(MoimUpdateReqDto.class);
        Moim moim = mock(Moim.class);
        MoimMember moimMember = mock(MoimMember.class);
        // moimMember.setMoimMemberRoleType(MoimMemberRoleType.MANAGER); // 다른 함수의 문제가 개입될 수 있는건 모두  Stubbing 해야함 (종속관계에 있는 것)

        // given - stub
        doReturn(true).when(moimMember).hasPermissionForUpdate();
        doReturn(Optional.ofNullable(moimMember)).when(moimMemberRepository).findByMemberAndMoimId(any(), any());
        doReturn(new ArrayList<>()).when(categoryService).generateCategoryList(any());
        doReturn(moim).when(moimMember).getMoim();

        // when
        moimService.updateMoim(mockReqDto, curMember); // 그냥 뭐를 넣어주든 그닥 상관 없고, return val 에도 그닥 관심 없음

        verify(moim, times(1)).updateMoim(any(), any(), any()); //

    }

    // 연산의 결과를 반환하는 것이면 반환값 중요
    // 종속된 객체를 호출해서 값을 확인해야한다면, 반환값이 중요하지 않고, 객체를 어떻게 호출했는지가 중요
    // 생성자에서 argument로 주입된 객체(aggregation) or 함수의 argument로 전달된 객체(dependency) or 생성자에서 생성된 객체(composition)
    // singleton pattern을 썼거나 함수 내부에서 객체를 생성하는 행위 등은 테스트가 좀 어려울 수 있다. code smell.
    /*
     * Bad Way
     * public class Human{
     *
     *   public void 공격행동(총알 ar){
     *       총 gun = new 총(ar);
     *   }
     * }
     *
     * Good Way
     * public class Human{
     *
     *   public void 공격행동(Attackable attackable){
     *       attackable.Attack();
     *   }
     * }
     *
     * public class 기관총 : Attackable {
     *   public 기관총(총알){
     *
     *   }
     *   public void Attack(){
     *   }
     * }
     *
     * public interface Attackable{
     *   void Attack();
     * }
     * */

    // 결과 기반 테스트
    @Test
    void updateMoim_shouldReturnMoimEntity_whenSuccessful2() {

        // given
        Moim mockMoim = mockMoimWithoutRuleJoin(1L, moimName, maxMember, depth1SampleCategory, depth2SampleCategory, curMember);
        MoimUpdateReqDto reqDto = mockMoimUpdateReqDto(mockMoim.getId(), moimName2, maxMember2, moimArea2.getState(), moimArea2.getCity());
        MoimMember mockMoimMember = mockMoimMember(1L, curMember, mockMoim);
        mockMoimMember.setMoimMemberRoleType(MoimMemberRoleType.MANAGER); // 실객체들의 함수는 정말 동작해야한다 // 근데 이런게 관심 없는게 맞음

        Category category1 = mockCategory(1L, CategoryName.fromValue(depth1SampleCategory2), 1, null); // 수정되려는 애임
        Category category2 = mockCategory(1L, CategoryName.fromValue(depth2SampleCategory2), 2, category1);

        // given - stub
        doReturn(Optional.ofNullable(mockMoimMember)).when(moimMemberRepository).findByMemberAndMoimId(any(), any());
        doReturn(List.of(category1, category2)).when(categoryService).generateCategoryList(any());

        // when
        Moim moim = moimService.updateMoim(reqDto, curMember);

        // then
        assertThat(moim.getMoimName()).isEqualTo(moimName2); // 수정됨
        assertThat(moim.getMoimInfo()).isEqualTo(moimInfo); // 수정 안됨
        assertThat(moim.getMaxMember()).isEqualTo(maxMember2);
        assertThat(moim.getMoimArea().getState()).isEqualTo(moimArea2.getState());
    }

}