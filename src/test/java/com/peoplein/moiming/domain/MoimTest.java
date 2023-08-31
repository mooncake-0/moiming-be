package com.peoplein.moiming.domain;


import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.support.TestMockCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

public class MoimTest extends TestMockCreator {


    // 생성 필수 정보
    private Member mockMember;
    private List<Category> mockCategories = new ArrayList<>();

    @BeforeEach
    void be_moim_info_su() {
        mockMember = mockMember(1L, memberEmail, memberName, memberPhone, new Role(1L, "", RoleType.USER));
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

}