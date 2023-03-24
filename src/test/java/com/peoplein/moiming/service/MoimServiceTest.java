package com.peoplein.moiming.service;

import com.peoplein.moiming.BaseTest;
import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MemberMoimLinker;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.model.dto.domain.MoimDto;
import com.peoplein.moiming.model.dto.domain.RuleJoinDto;
import com.peoplein.moiming.model.dto.request.MoimRequestDto;
import com.peoplein.moiming.model.dto.response.MoimResponseDto;
import com.peoplein.moiming.repository.*;
import com.peoplein.moiming.repository.jpa.query.MoimJpaQueryRepository;
import com.peoplein.moiming.service.core.MoimServiceCore;
import com.peoplein.moiming.service.shell.MoimServiceShell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
//@Transactional
class MoimServiceTest extends BaseTest {


    @Mock
    MoimRepository moimRepository;

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    MoimCategoryLinkerRepository moimCategoryLinkerRepository;

    @Mock
    MoimJpaQueryRepository moimJpaQueryRepository;

    @Mock
    MemberMoimLinkerRepository memberMoimLinkerRepository;

    @Mock
    MoimPostRepository moimPostRepository;

    @Mock
    ScheduleRepository scheduleRepository;

    @Mock
    PostCommentRepository postCommentRepository;

    @Mock
    MemberScheduleLinkerRepository memberScheduleLinkerRepository;

    @Mock
    MoimServiceShell moimServiceShell;

    MoimService moimService;


    Member baseMember;
    Moim baseMoim;


    @BeforeEach
    void initTestInstance() {

        MoimServiceCore moimServiceCore = new MoimServiceCore();
        moimService = new MoimService(moimRepository,
                categoryRepository,
                moimCategoryLinkerRepository,
                moimJpaQueryRepository,
                memberMoimLinkerRepository,
                moimPostRepository,
                scheduleRepository,
                postCommentRepository,
                memberScheduleLinkerRepository,
                moimServiceShell,
                moimServiceCore);

        baseMember = TestUtils.initMemberAndMemberInfo();
        baseMoim = TestUtils.initMoimAndRuleJoin();
    }

    // TODO : 통합 테스트로 업데이트 필요함. 그 때 까지 사용하지 않음.
//    @Test
    void createMoimTest() {

        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.initMoimAndRuleJoin();
        MoimDto moimDto = new MoimDto(moim);
        RuleJoinDto ruleJoinDto = new RuleJoinDto(moim.getRuleJoin());

        List<CategoryName> categoryNames = TestUtils.initCategoryName();
        Category category = new Category();
        category.setCategoryName(categoryNames.get(0));

        MoimRequestDto requestDto = new MoimRequestDto(moimDto, ruleJoinDto, categoryNames);

        // stub
        when(categoryRepository.findByCategoryNames(requestDto.getCategoryNames()))
                .thenReturn(List.of(category));

        // when
        MoimResponseDto result = moimService.createMoim(member, requestDto);


        // then
        assertThat(result.getMoimDto().getMoimName()).isEqualTo(moim.getMoimName());
        assertThat(result.getMoimDto().getMoimInfo()).isEqualTo(moim.getMoimInfo());

        assertThat(result.getRuleJoinDto().getBirthMax()).isEqualTo(ruleJoinDto.getBirthMax());
        assertThat(result.getRuleJoinDto().getBirthMin()).isEqualTo(ruleJoinDto.getBirthMin());

        assertThat(result.getCategoriesDto().size()).isEqualTo(1);

        assertThat(result.getMoimMembersDto().getMoimMemberInfoDto().size()).isEqualTo(0);

        // verify
        verify(moimRepository, times(1)).save(any());
    }


    @Test
    void getMoimTest() {
        // given
        Member member = spy(baseMember);
        Moim moim = spy(baseMoim);
        MemberMoimLinker memberMoimLinker = MemberMoimLinker.memberJoinMoim(member, moim, MoimRoleType.NORMAL, MoimMemberState.ACTIVE);

        // stub
        when(member.getId()).thenReturn(10L);
        when(moimRepository.findOptionalById(moim.getId())).thenReturn(Optional.of(moim));


        // when
        MoimResponseDto result = moimService.getMoim(moim.getId(), member);

        // then
        assertThat(result.getMoimDto().getMoimId()).isEqualTo(moim.getId());
        assertThat(result.getMoimMembersDto().getMyMoimLinkerDto()).isNotNull();
        assertThat(result.getRuleJoinDto().getBirthMax()).isEqualTo(moim.getRuleJoin().getBirthMax());
    }

    @Test
    void getMoimFailTest() {
        // given
        Member member = spy(baseMember);
        Moim moim = spy(baseMoim);
        MemberMoimLinker memberMoimLinker = MemberMoimLinker.memberJoinMoim(member, moim, MoimRoleType.NORMAL, MoimMemberState.ACTIVE);

        // stub
        when(member.getId()).thenReturn(10L);
        when(moim.getId()).thenReturn(11L);
        when(moimRepository.findById(moim.getId())).thenReturn(null);

        // when + then
        assertThatThrownBy(() -> moimService.getMoim(moim.getId(), member))
                .isInstanceOf(RuntimeException.class);
    }


}