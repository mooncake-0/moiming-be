package com.peoplein.moiming.service;

import com.peoplein.moiming.BaseTest;
import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.model.dto.domain.MoimDto;
import com.peoplein.moiming.model.dto.domain.RuleJoinDto;
import com.peoplein.moiming.service.core.MoimServiceCore;
import com.peoplein.moiming.service.input.MoimServiceInput;
import com.peoplein.moiming.service.output.MoimServiceOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class MoimServiceUnitTest extends BaseTest {

    @Autowired
    MoimService moimService;

    Member baseMember;
    Moim baseMoim;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void initInstance() {
        TestUtils.truncateAllTable(jdbcTemplate);
        baseMember = TestUtils.initMemberAndMemberInfo();
        baseMoim = TestUtils.initMoimAndRuleJoin();
    }


    @Test
    void updateMoimSuccessTest() {

        // given
        MoimDto moimDto = TestUtils.createOtherMoimDtoForUpdate();
        MoimServiceInput input = MoimServiceInput.builder()
                .inputMoimDto(moimDto)
                .findMoim(baseMoim)
                .updatePermission(MoimRoleType.LEADER)
                .curMember(baseMember)
                .build();
        MoimServiceCore moimServiceCore = moimService.getMoimServiceCore();

        // when
        MoimServiceOutput result = moimServiceCore.hasAnyUpdated(input);

        // then
        assertThat(result.isAnyUpdated()).isTrue();
    }

    @Test
    void updateMoimTestFailIfNotPermitted2() {

        // given
        MoimDto moimDto = TestUtils.createOtherMoimDtoForUpdate();
        MoimServiceInput input = MoimServiceInput.builder()
                .inputMoimDto(moimDto)
                .findMoim(baseMoim)
                .updatePermission(MoimRoleType.NORMAL)
                .curMember(baseMember)
                .build();
        MoimServiceCore moimServiceCore = moimService.getMoimServiceCore();

        // when + then
        assertThatThrownBy(() -> moimServiceCore.hasAnyUpdated(input))
                .isInstanceOf(RuntimeException.class);
    }



    @Test
    void updateMoimTestFailIfMoimNotExisted() {
        // given
        MoimDto moimDto = TestUtils.createOtherMoimDtoForUpdate();
        MoimServiceInput input = MoimServiceInput.builder()
                .inputMoimDto(moimDto)
                .findMoim(null)
                .updatePermission(MoimRoleType.NORMAL)
                .curMember(baseMember)
                .build();
        MoimServiceCore moimServiceCore = moimService.getMoimServiceCore();

        // when + then
        assertThatThrownBy(() -> moimServiceCore.hasAnyUpdated(input))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
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

        MoimServiceCore moimServiceCore = moimService.getMoimServiceCore();

        // when
        MoimServiceOutput result = moimServiceCore.createMoim(input);

        // then
        assertThat(result.getCreatedMoim().getMoimName()).isEqualTo(moimDto.getMoimName());
        assertThat(result.getCreatedMoimCategoryLinkers().get(0).getCategory()).isEqualTo(moimCategories.get(0));
        assertThat(result.getCreatedMemberMoimLinker()).isNotNull();
    }

}
