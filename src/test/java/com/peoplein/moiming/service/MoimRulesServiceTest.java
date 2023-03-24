package com.peoplein.moiming.service;

import com.peoplein.moiming.BaseTest;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MemberMoimLinker;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.rules.RuleJoin;
import com.peoplein.moiming.domain.rules.RulePersist;
import com.peoplein.moiming.model.dto.domain.RuleJoinDto;
import com.peoplein.moiming.model.dto.domain.RulePersistDto;
import com.peoplein.moiming.model.dto.request.RuleRequestDto;
import com.peoplein.moiming.repository.MemberMoimLinkerRepository;
import com.peoplein.moiming.repository.MoimRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MoimRulesServiceTest {
    @InjectMocks
    private MoimRulesService moimRulesService;

    @Mock
    private MemberMoimLinkerRepository memberMoimLinkerRepository;
    @Mock
    private MoimRuleRepository moimRuleRepository;

    @Mock
    private Member curMember;
    @Mock
    private Member requestMember;
    @Mock
    private Moim curMoim;
    @Mock
    private MemberMoimLinker curLinker;
    @Mock
    private MemberMoimLinker requestLinker;



    @BeforeEach
    void be() {
        curMember = mock(Member.class);
        curMoim = mock(Moim.class);
        curLinker = mock(MemberMoimLinker.class);

        lenient().when(curMember.getId()).thenReturn(1L);
        lenient().when(curMember.getUid()).thenReturn("wrock.kang");

        lenient().when(curMoim.getId()).thenReturn(2L);
        lenient().when(curMoim.getMoimName()).thenReturn("모이밍");
        lenient().when(curMoim.isHasRuleJoin()).thenReturn(false);
        lenient().when(curMoim.getRuleJoin()).thenReturn(null);
        lenient().when(curMoim.isHasRulePersist()).thenReturn(false);
        lenient().when(curMoim.getRulePersist()).thenReturn(null);
        lenient().when(curMoim.getMoimRules()).thenReturn(new ArrayList<>());

        lenient().when(curLinker.getMoimRoleType()).thenReturn(MoimRoleType.MANAGER);
        lenient().when(curLinker.getMemberState()).thenReturn(MoimMemberState.ACTIVE);
        lenient().when(curLinker.getMoim()).thenReturn(curMoim);
        lenient().when(curLinker.getMember()).thenReturn(curMember);

        // REPO
        lenient().when(memberMoimLinkerRepository.findWithMoimByMemberAndMoimId(curMember.getId(), curMoim.getId())).thenReturn(curLinker);

        // 수정 및 삭제 요청에서 다른 멤버의 요청으로 setting 해보자 (별로 상관은 없음..)
        requestMember = mock(Member.class);
        lenient().when(requestMember.getId()).thenReturn(2L);
        lenient().when(requestMember.getUid()).thenReturn("bhp1234");

        lenient().when(requestLinker.getMoimRoleType()).thenReturn(MoimRoleType.LEADER);
        lenient().when(requestLinker.getMemberState()).thenReturn(MoimMemberState.ACTIVE);
        lenient().when(requestLinker.getMoim()).thenReturn(curMoim);
        lenient().when(requestLinker.getMember()).thenReturn(requestMember);

        lenient().when(memberMoimLinkerRepository.findWithMoimByMemberAndMoimId(requestMember.getId(), curMoim.getId())).thenReturn(requestLinker);
    }

    @Test
    @DisplayName("성공 @ createJoinRule() - 생성 성공")
    void 가입조건_생성_성공() {

        //given
        RuleJoinDto preJoinDto = new RuleJoinDto(curMoim.getId(), 2000, 1990, MemberGender.N, 3, true, false, null, null, null, null);
        RuleRequestDto ruleRequestDto = new RuleRequestDto(curMoim.getId(), preJoinDto, null);

        //when
        RuleJoinDto postJoinDto = moimRulesService.createJoinRule(ruleRequestDto, curMember);

        //then
        assertEquals(curMoim.getId(), postJoinDto.getMoimId());
        assertEquals(2000, postJoinDto.getBirthMax());
        assertEquals(1990, postJoinDto.getBirthMin());
        assertEquals(MemberGender.N, postJoinDto.getGender());
        assertEquals(3, postJoinDto.getMoimMaxCount());
        assertTrue(postJoinDto.isDupLeaderAvailable());
        assertFalse(postJoinDto.isDupManagerAvailable());
        // 생성자로 만든 DTO 이므로
        assertEquals(curMember.getUid(), postJoinDto.getCreatedUid());
        assertNotNull(postJoinDto.getCreatedAt());
    }

    @Test
    @DisplayName("실패 @ createJoinRule() - 속하지 않은 모임")
    void 가입조건_생성_실패_속하지_않은_모임() {

        //given - MML 을 찾지 못함
        lenient().when(memberMoimLinkerRepository.findWithMoimByMemberAndMoimId(curMember.getId(), curMoim.getId())).thenReturn(null);
        RuleJoinDto preJoinDto = new RuleJoinDto(curMoim.getId(), 2000, 1990, MemberGender.N, 3, true, false, null, null, null, null);
        RuleRequestDto ruleRequestDto = new RuleRequestDto(curMoim.getId(), preJoinDto, null);

        //when
        //then
        assertThatThrownBy(() -> moimRulesService.createJoinRule(ruleRequestDto, curMember)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("실패 @ createJoinRule() - 가입조건 변경 권한이 없는 모임")
    void 가입조건_생성_실패_권한_없는_모임() {

        //given
        lenient().when(curLinker.getMoimRoleType()).thenReturn(MoimRoleType.NORMAL);
        RuleJoinDto preJoinDto = new RuleJoinDto(curMoim.getId(), 2000, 1990, MemberGender.N, 3, true, false, null, null, null, null);
        RuleRequestDto ruleRequestDto = new RuleRequestDto(curMoim.getId(), preJoinDto, null);

        //when
        //then
        assertThatThrownBy(() -> moimRulesService.createJoinRule(ruleRequestDto, curMember)).isInstanceOf(RuntimeException.class);

    }

    @Test
    @DisplayName("실패 @ createJoinRule() - 이미 가입 조건이 있는 모임")
    void 가입조건_생성_실패_가입조건이_있는_모임() {

        //given
        lenient().when(curMoim.isHasRuleJoin()).thenReturn(true);
        RuleJoin existingRule = new RuleJoin(2000, 1990, MemberGender.N, 7, false, false, curMoim, "abc");
        lenient().when(curMoim.getRuleJoin()).thenReturn(existingRule);
        lenient().when(curMoim.getMoimRules()).thenReturn(List.of(existingRule));
        RuleJoinDto preJoinDto = new RuleJoinDto(curMoim.getId(), 2000, 1990, MemberGender.N, 3, true, false, null, null, null, null);
        RuleRequestDto ruleRequestDto = new RuleRequestDto(curMoim.getId(), preJoinDto, null);

        //when
        //then
        assertThatThrownBy(() -> moimRulesService.createJoinRule(ruleRequestDto, curMember)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("실패 @ createJoinRule() - 잘못된 요청 모임")
    void 가입조건_생성_잘못된_요청() {

        //given - MML 을 찾지 못함
        RuleRequestDto ruleRequestDto = new RuleRequestDto(curMoim.getId(), null, null);

        //when
        //then
        assertThatThrownBy(() -> moimRulesService.createJoinRule(ruleRequestDto, curMember)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("성공 @ createPersistRule() - 생성 성공")
    void 유지조건_생성_성공() {

        //given
        RulePersistDto rulePersistDto = new RulePersistDto(curMoim.getId(), false, 2, 2, null, null, null, null);
        RuleRequestDto ruleRequestDto = new RuleRequestDto(curMoim.getId(), null, rulePersistDto);

        //when
        RulePersistDto postPersistDto = moimRulesService.createPersistRule(ruleRequestDto, curMember);

        //then
        assertEquals(curMoim.getId(), postPersistDto.getMoimId());
        assertFalse(rulePersistDto.isDoGreeting());
        assertEquals(2, rulePersistDto.getAttendMonthly());
        assertEquals(2, rulePersistDto.getAttendCount());
        // 생성자로 만든 DTO 이므로
        assertEquals(curMember.getUid(), postPersistDto.getCreatedUid());
        assertNotNull(postPersistDto.getCreatedAt());
    }

    /*
     가입 조건과 비즈니스 로직이 똑같으므로 유지 조건은 실패 Case 를 간단히 한다
     */

    @Test
    @DisplayName("실패 @ createPersistRule() - 이미 유지 조건이 있는 모임")
    void 유지조건_생성_실패_유지조건이_있는_모임() {

        //given
        lenient().when(curMoim.isHasRulePersist()).thenReturn(true);
        RulePersist existingPersist = new RulePersist(true, 2, 2, curMoim, "abc");
        lenient().when(curMoim.getRulePersist()).thenReturn(existingPersist);
        lenient().when(curMoim.getMoimRules()).thenReturn(List.of(existingPersist));
        RulePersistDto rulePersistDto = new RulePersistDto(curMoim.getId(), false, 2, 2, null, null, null, null);
        RuleRequestDto ruleRequestDto = new RuleRequestDto(curMoim.getId(), null, rulePersistDto);

        //when
        //then
        assertThatThrownBy(() -> moimRulesService.createPersistRule(ruleRequestDto, curMember)).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("성공 @ getJoinRule()")
    void 가입조건_조회_성공() {

        //given
        lenient().when(curMoim.isHasRuleJoin()).thenReturn(true);
        RuleJoin existingRule = new RuleJoin(2000, 1990, MemberGender.N, 7, false, false, curMoim, "abc");
        lenient().when(curMoim.getRuleJoin()).thenReturn(existingRule);
        lenient().when(curMoim.getMoimRules()).thenReturn(List.of(existingRule));
        when(moimRuleRepository.findByMoimAndType(curMoim.getId(), "J")).thenReturn(existingRule);

        //when
        RuleJoinDto joinDto = moimRulesService.getJoinRule(curMoim.getId(), curMember);

        //then (위와 동일)
        assertEquals(curMoim.getId(), joinDto.getMoimId());
        assertEquals(2000, joinDto.getBirthMax());
        assertEquals(1990, joinDto.getBirthMin());
        assertEquals(MemberGender.N, joinDto.getGender());
        assertEquals(7, joinDto.getMoimMaxCount());
        assertFalse(joinDto.isDupLeaderAvailable());
        assertFalse(joinDto.isDupManagerAvailable());
        assertEquals("abc", joinDto.getCreatedUid());
        assertNotNull(joinDto.getCreatedAt());

    }

    @Test
    @DisplayName("실패 @ getJoinRule() - 가입조건이 없음")
    void 가입조건_조회_실패() {

        //given
        when(moimRuleRepository.findByMoimAndType(curMoim.getId(), "J")).thenReturn(null);

        //when
        //then
        assertThatThrownBy(() -> moimRulesService.getJoinRule(curMoim.getId(), curMember)).isInstanceOf(RuntimeException.class);

    }

    @Test
    @DisplayName("성공 @ getPersistRule()")
    void 유지조건_조회_성공() {

        //given
        lenient().when(curMoim.isHasRulePersist()).thenReturn(true);
        RulePersist existingPersist = new RulePersist(true, 2, 2, curMoim, "abc");
        lenient().when(curMoim.getRulePersist()).thenReturn(existingPersist);
        lenient().when(curMoim.getMoimRules()).thenReturn(List.of(existingPersist));
        when(moimRuleRepository.findByMoimAndType(curMoim.getId(), "P")).thenReturn(existingPersist);

        //when
        RulePersistDto persistDto = moimRulesService.getPersistRule(curMoim.getId(), curMember);

        //then
        assertEquals(curMoim.getId(), persistDto.getMoimId());
        assertTrue(persistDto.isDoGreeting());
        assertEquals(2, persistDto.getAttendMonthly());
        assertEquals(2, persistDto.getAttendCount());
        // 생성자로 만든 DTO 이므로
        assertEquals("abc", persistDto.getCreatedUid());
        assertNotNull(persistDto.getCreatedAt());

    }

    @Test
    @DisplayName("실패 @ getJoinRule() - 유지조건이 없음")
    void 유지조건_조회_실패() {

        //given
        when(moimRuleRepository.findByMoimAndType(curMoim.getId(), "P")).thenReturn(null);

        //when
        //then
        assertThatThrownBy(() -> moimRulesService.getPersistRule(curMoim.getId(), curMember)).isInstanceOf(RuntimeException.class);

    }

    @Test
    @DisplayName("성공 @ changeJoinRule()")
    void 가입조건_수정_성공() {

        //given
        lenient().when(curMoim.isHasRuleJoin()).thenReturn(true);
        RuleJoin existingRule = new RuleJoin(2000, 1990, MemberGender.N, 7, false, false, curMoim, curMember.getUid());
        lenient().when(curMoim.getRuleJoin()).thenReturn(existingRule);
        lenient().when(curMoim.getMoimRules()).thenReturn(List.of(existingRule));

        when(moimRuleRepository.findByMoimAndType(curMoim.getId(), "J")).thenReturn(existingRule);

        RuleJoinDto changeJoinDto = new RuleJoinDto(curMoim.getId(), 2000, 1992, MemberGender.N, 3, true, false, null, null, null, null);
        RuleRequestDto ruleRequestDto = new RuleRequestDto(curMoim.getId(), changeJoinDto, null);

        //when
        RuleJoinDto updatedJoinDto = moimRulesService.changeJoinRule(ruleRequestDto, requestMember);

        //then
        assertEquals(curMoim.getId(), updatedJoinDto.getMoimId());
        assertEquals(2000, updatedJoinDto.getBirthMax());
        assertEquals(1992, updatedJoinDto.getBirthMin());
        assertEquals(MemberGender.N, updatedJoinDto.getGender());
        assertEquals(3, updatedJoinDto.getMoimMaxCount());
        assertTrue(updatedJoinDto.isDupLeaderAvailable());
        assertFalse(updatedJoinDto.isDupManagerAvailable());
        assertEquals(curMember.getUid(), updatedJoinDto.getCreatedUid());
        assertNotNull(updatedJoinDto.getCreatedAt());

        // 정말 의미 있는 것
        assertEquals(requestMember.getUid(), updatedJoinDto.getUpdatedUid());
        assertNotNull(updatedJoinDto.getUpdatedAt());
    }

    @Test
    @DisplayName("실패 @ changeJoinRule() - 수정 권한 없음")
    void 가입조건_수정_실패_권한_없음() {
        //given (가입조건이 있는 상태니까, 기본적으로 깔려야 함)
        lenient().when(curMoim.isHasRuleJoin()).thenReturn(true);
        RuleJoin existingRule = new RuleJoin(2000, 1990, MemberGender.N, 7, false, false, curMoim, curMember.getUid());
        lenient().when(curMoim.getRuleJoin()).thenReturn(existingRule);
        lenient().when(curMoim.getMoimRules()).thenReturn(List.of(existingRule));

        lenient().when(moimRuleRepository.findByMoimAndType(curMoim.getId(), "J")).thenReturn(existingRule);
        when(requestLinker.getMoimRoleType()).thenReturn(MoimRoleType.NORMAL); // 권한이 없음

        RuleJoinDto changeJoinDto = new RuleJoinDto(curMoim.getId(), 2000, 1992, MemberGender.N, 3, true, false, null, null, null, null);
        RuleRequestDto ruleRequestDto = new RuleRequestDto(curMoim.getId(), changeJoinDto, null);

        // when
        // then
        assertThatThrownBy(() -> moimRulesService.changeJoinRule(ruleRequestDto, requestMember));
    }

    @Test
    @DisplayName("실패 @ changeJoinRule() - 모임 소속이 아님, 잘못된 요청")
    void 가입조건_수정_실패_모임_미소속() {

        //given (가입조건이 있는 상태니까, 기본적으로 깔려야 함)
        lenient().when(curMoim.isHasRuleJoin()).thenReturn(true);
        RuleJoin existingRule = new RuleJoin(2000, 1990, MemberGender.N, 7, false, false, curMoim, curMember.getUid());
        lenient().when(curMoim.getRuleJoin()).thenReturn(existingRule);
        lenient().when(curMoim.getMoimRules()).thenReturn(List.of(existingRule));

        lenient().when(moimRuleRepository.findByMoimAndType(curMoim.getId(), "J")).thenReturn(existingRule);
        when(memberMoimLinkerRepository.findWithMoimByMemberAndMoimId(requestMember.getId(), curMoim.getId())).thenReturn(null); // 요청 유저가 가입되어 있지 않은 모임

        RuleJoinDto changeJoinDto = new RuleJoinDto(curMoim.getId(), 2000, 1992, MemberGender.N, 3, true, false, null, null, null, null);
        RuleRequestDto ruleRequestDto = new RuleRequestDto(curMoim.getId(), changeJoinDto, null);

        // when
        // then
        assertThatThrownBy(() -> moimRulesService.changeJoinRule(ruleRequestDto, requestMember));

    }

    @Test
    @DisplayName("실패 @ changeJoinRule() - 요청에 ruleJoinDto 가 없음")
    void 가입조건_수정_실패_잘못된_요청() {

        //given (가입조건이 있는 상태니까, 기본적으로 깔려야 함)
        lenient().when(curMoim.isHasRuleJoin()).thenReturn(true);
        RuleJoin existingRule = new RuleJoin(2000, 1990, MemberGender.N, 7, false, false, curMoim, curMember.getUid());
        lenient().when(curMoim.getRuleJoin()).thenReturn(existingRule);
        lenient().when(curMoim.getMoimRules()).thenReturn(List.of(existingRule));

        lenient().when(moimRuleRepository.findByMoimAndType(curMoim.getId(), "J")).thenReturn(existingRule);

        RuleRequestDto ruleRequestDto = new RuleRequestDto(curMoim.getId(), null, null);

        // when
        // then
        assertThatThrownBy(() -> moimRulesService.changeJoinRule(ruleRequestDto, requestMember));
    }

    @Test
    @DisplayName("실패 @ changeJoinRule() - 수정된 사항이 없음")
    void 가입조건_수정_실패_수정사항_없음() {

        //given (가입조건이 있는 상태니까, 기본적으로 깔려야 함)
        lenient().when(curMoim.isHasRuleJoin()).thenReturn(true);
        RuleJoin existingRule = new RuleJoin(2000, 1990, MemberGender.N, 7, false, false, curMoim, curMember.getUid());
        lenient().when(curMoim.getRuleJoin()).thenReturn(existingRule);
        lenient().when(curMoim.getMoimRules()).thenReturn(List.of(existingRule));

        lenient().when(moimRuleRepository.findByMoimAndType(curMoim.getId(), "J")).thenReturn(existingRule);
        // 똑같은 조건으로 세팅
        RuleJoinDto changeJoinDto = new RuleJoinDto(curMoim.getId(), 2000, 1990, MemberGender.N, 7, false, false, null, null, null, null);
        RuleRequestDto ruleRequestDto = new RuleRequestDto(curMoim.getId(), changeJoinDto, null);

        //when
        //then
        assertThatThrownBy(() -> moimRulesService.changeJoinRule(ruleRequestDto, requestMember));
    }

    @Test
    @DisplayName("실패 @ changeJoinRule() - 모임에 수정할 조건이 없음")
    void 가입조건_수정_실패_모임에_가입조건_없음() {

        //given
        lenient().when(moimRuleRepository.findByMoimAndType(curMoim.getId(), "J")).thenReturn(null);

        RuleJoinDto changeJoinDto = new RuleJoinDto(curMoim.getId(), 2000, 1992, MemberGender.N, 3, true, false, null, null, null, null);
        RuleRequestDto ruleRequestDto = new RuleRequestDto(curMoim.getId(), changeJoinDto, null);

        //when
        //then
        assertThatThrownBy(() -> moimRulesService.changeJoinRule(ruleRequestDto, requestMember));
    }

    @Test
    @DisplayName("성공 @ changePersistRule()")
    void 유지조건_수정_성공() {

        //given
        lenient().when(curMoim.isHasRulePersist()).thenReturn(true);
        RulePersist existingPersist = new RulePersist(true, 2, 2, curMoim, curMember.getUid());
        lenient().when(curMoim.getRulePersist()).thenReturn(existingPersist);
        lenient().when(curMoim.getMoimRules()).thenReturn(List.of(existingPersist));

        when(moimRuleRepository.findByMoimAndType(curMoim.getId(), "P")).thenReturn(existingPersist);

        RulePersistDto changePersistDto = new RulePersistDto(curMoim.getId(), false, 2, 1, null, null, null, null);
        RuleRequestDto ruleRequestDto = new RuleRequestDto(curMoim.getId(), null, changePersistDto);

        //when
        RulePersistDto updatedPersistDto = moimRulesService.changePersistRule(ruleRequestDto, requestMember);

        //then
        assertEquals(curMoim.getId(), updatedPersistDto.getMoimId());
        assertFalse(updatedPersistDto.isDoGreeting());
        assertEquals(2, updatedPersistDto.getAttendMonthly());
        assertEquals(1, updatedPersistDto.getAttendCount());
        assertEquals(curMember.getUid(), updatedPersistDto.getCreatedUid());
        assertNotNull(updatedPersistDto.getCreatedAt());

        // 정말 의미 있는 것
        assertEquals(requestMember.getUid(), updatedPersistDto.getUpdatedUid());
        assertNotNull(updatedPersistDto.getUpdatedAt());

    }

    /*
     가입 조건과 비즈니스 로직이 똑같으므로 유지 조건은 실패 Case 를 간단히 한다
     */

    @Test
    @DisplayName("실패 @ changePersistRule() - 수정 권한 없음")
    void 유지조건_수정_실패_권한_없음() {
        //given
        lenient().when(curMoim.isHasRulePersist()).thenReturn(true);
        RulePersist existingPersist = new RulePersist(true, 2, 2, curMoim, curMember.getUid());
        lenient().when(curMoim.getRulePersist()).thenReturn(existingPersist);
        lenient().when(curMoim.getMoimRules()).thenReturn(List.of(existingPersist));

        lenient().when(moimRuleRepository.findByMoimAndType(curMoim.getId(), "P")).thenReturn(existingPersist);

        RulePersistDto changePersistDto = new RulePersistDto(curMoim.getId(), false, 2, 1, null, null, null, null);
        RuleRequestDto ruleRequestDto = new RuleRequestDto(curMoim.getId(), null, changePersistDto);

        when(requestLinker.getMoimRoleType()).thenReturn(MoimRoleType.NORMAL); // 권한 없는 유저의 요청

        // when
        // then
        assertThatThrownBy(() -> moimRulesService.changePersistRule(ruleRequestDto, requestMember));
    }

    @Test
    @DisplayName("실패 @ changePersistRule() - 수정된 사항이 없음")
    void 유지조건_수정_실패_수정사항_없음() {
        //given
        lenient().when(curMoim.isHasRulePersist()).thenReturn(true);
        RulePersist existingPersist = new RulePersist(true, 2, 2, curMoim, curMember.getUid());
        lenient().when(curMoim.getRulePersist()).thenReturn(existingPersist);
        lenient().when(curMoim.getMoimRules()).thenReturn(List.of(existingPersist));

        when(moimRuleRepository.findByMoimAndType(curMoim.getId(), "P")).thenReturn(existingPersist);

        // 동일하게 세팅
        RulePersistDto changePersistDto = new RulePersistDto(curMoim.getId(), true, 2, 2, null, null, null, null);
        RuleRequestDto ruleRequestDto = new RuleRequestDto(curMoim.getId(), null, changePersistDto);

        // when
        // then
        assertThatThrownBy(() -> moimRulesService.changePersistRule(ruleRequestDto, requestMember));
    }

    @Test
    @DisplayName("실패 @ changePersistRule() - 모임에 수정할 조건이 없음")
    void 유지조건_수정_실패_모임에_유지조건_없음() {

        //given
        lenient().when(moimRuleRepository.findByMoimAndType(curMoim.getId(), "P")).thenReturn(null); // 차피 없기 떄문에 여기까지 안옴

        RulePersistDto changePersistDto = new RulePersistDto(curMoim.getId(), false, 2, 1, null, null, null, null);
        RuleRequestDto ruleRequestDto = new RuleRequestDto(curMoim.getId(), null, changePersistDto);

        // when
        // then
        assertThatThrownBy(() -> moimRulesService.changePersistRule(ruleRequestDto, requestMember));
    }

    /*
     삭제 실패 Test Case 는 위와 동일한 로직
     */

    @Test
    @DisplayName("성공 @ deleteJoinRule()")
    void 가입조건_삭제_성공() {

        //given (가입조건이 있는 상태니까, 기본적으로 깔려야 함)
        lenient().when(curMoim.isHasRuleJoin()).thenReturn(true);
        RuleJoin existingRule = new RuleJoin(2000, 1990, MemberGender.N, 7, false, false, curMoim, curMember.getUid());
        lenient().when(curMoim.getRuleJoin()).thenReturn(existingRule);
        lenient().when(curMoim.getMoimRules()).thenReturn(List.of(existingRule));

        when(moimRuleRepository.findByMoimAndType(curMoim.getId(), "J")).thenReturn(existingRule);

        //when
        //then 아무 Exception 없이 진행되면 성공
        assertDoesNotThrow(() -> moimRulesService.deleteJoinRule(curMoim.getId(), curMember));
    }

    @Test
    @DisplayName("성공 @ deletePersistRule()")
    void 유지조건_삭제_성공() {
        //given
        lenient().when(curMoim.isHasRulePersist()).thenReturn(true);
        RulePersist existingPersist = new RulePersist(true, 2, 2, curMoim, curMember.getUid());
        lenient().when(curMoim.getRulePersist()).thenReturn(existingPersist);
        lenient().when(curMoim.getMoimRules()).thenReturn(List.of(existingPersist));

        when(moimRuleRepository.findByMoimAndType(curMoim.getId(), "P")).thenReturn(existingPersist);

        //when
        //then 아무 Exception 없이 진행되면 성공
        assertDoesNotThrow(() -> moimRulesService.deletePersistRule(curMoim.getId(), curMember));
    }

}