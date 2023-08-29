package com.peoplein.moiming.service;

import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.model.dto.domain.MoimDto;
import com.peoplein.moiming.model.dto.domain.RuleJoinDto;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 해당 도메인 서비스단 재설계 예정
 - Service 는 단위테스트만 진행 예정 (DB 개입 필요 없음)
 - Repo 단위테스트, Controlller 통합 테스트로 진행
 */
//@SpringBootTest
public class MoimServiceOldUnitTest {

    @Autowired
    MoimServiceOld moimServiceOld;

    Member baseMember;
    Moim baseMoim;

    @BeforeEach
    void initInstance() {
        baseMember = TestUtils.initMemberAndMemberInfo();
        baseMoim = TestUtils.initMoimAndRuleJoin();
    }


//    @Test
    void updateMoimSuccessTest() {

        // given
        MoimDto moimDto = TestUtils.createOtherMoimDtoForUpdate();
        MoimServiceInput input = MoimServiceInput.builder()
                .inputMoimDto(moimDto)
                .findMoim(baseMoim)
                .updatePermission(MoimMemberRoleType.LEADER)
                .curMember(baseMember)
                .build();
        MoimServiceCore moimServiceCore = moimServiceOld.getMoimServiceCore();

        // when
        MoimServiceOutput result = moimServiceCore.hasAnyUpdated(input);

        // then
        assertThat(result.isAnyUpdated()).isTrue();
    }

//    @Test
    void updateMoimTestFailIfNotPermitted2() {

        // given
        MoimDto moimDto = TestUtils.createOtherMoimDtoForUpdate();
        MoimServiceInput input = MoimServiceInput.builder()
                .inputMoimDto(moimDto)
                .findMoim(baseMoim)
                .updatePermission(MoimMemberRoleType.NORMAL)
                .curMember(baseMember)
                .build();
        MoimServiceCore moimServiceCore = moimServiceOld.getMoimServiceCore();

        // when + then
        assertThatThrownBy(() -> moimServiceCore.hasAnyUpdated(input))
                .isInstanceOf(RuntimeException.class);
    }



//    @Test
    void updateMoimTestFailIfMoimNotExisted() {
        // given
        MoimDto moimDto = TestUtils.createOtherMoimDtoForUpdate();
        MoimServiceInput input = MoimServiceInput.builder()
                .inputMoimDto(moimDto)
                .findMoim(null)
                .updatePermission(MoimMemberRoleType.NORMAL)
                .curMember(baseMember)
                .build();
        MoimServiceCore moimServiceCore = moimServiceOld.getMoimServiceCore();

        // when + then
        assertThatThrownBy(() -> moimServiceCore.hasAnyUpdated(input))
                .isInstanceOf(RuntimeException.class);
    }

//    @Test
    void createMoimTestSuccess() {
        // given
        MoimDto moimDto = TestUtils.createOtherMoimDtoForUpdate();
        RuleJoinDto ruleJoinDto = TestUtils.initRuleJoinDto();
        List<Category> moimCategories = TestUtils.createMoimCategories();

        MoimServiceInput input = MoimServiceInput.builder()
                .inputMoimDto(moimDto)
                .curMember(baseMember)
                .requestRuleJoinDto(ruleJoinDto)
                .moimCategoriesForCreate(moimCategories)
                .build();

        MoimServiceCore moimServiceCore = moimServiceOld.getMoimServiceCore();

        // when
        MoimServiceOutput result = moimServiceCore.createMoim(input);

        // then
        assertThat(result.getCreatedMoim().getMoimName()).isEqualTo(moimDto.getMoimName());
        assertThat(result.getCreatedMoimCategoryLinkers().get(0).getCategory()).isEqualTo(moimCategories.get(0));
        assertThat(result.getCreatedMoimMember()).isNotNull();
    }

}
