package com.peoplein.moiming.service;

import com.peoplein.moiming.BaseTest;
import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.enums.MoimMemberStateAction;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.enums.ScheduleMemberState;
import com.peoplein.moiming.model.dto.request.MoimJoinRequestDto;
import com.peoplein.moiming.model.dto.request.MoimMemberActionRequestDto;
import com.peoplein.moiming.model.dto.request.ScheduleRequestDto;
import com.peoplein.moiming.model.dto.response.ScheduleResponseDto;
import com.peoplein.moiming.repository.MemberMoimLinkerRepository;
import com.peoplein.moiming.repository.MemberScheduleLinkerRepository;
import com.peoplein.moiming.repository.ScheduleRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
//@Rollback(value = false)
class ScheduleServiceTest extends BaseTest {


    @Autowired
    EntityManager em;

    @Autowired
    ScheduleService scheduleService;

    @Autowired
    MoimMemberService moimMemberService;

    @Autowired
    MoimService moimService;

    @Autowired
    MemberScheduleLinkerRepository memberScheduleLinkerRepository;

    @Autowired
    ScheduleRepository scheduleRepository;

    @Autowired
    MemberMoimLinkerRepository memberMoimLinkerRepository;

    @Test
    @DisplayName("updateSchedule : 성공하는 경우")
    void updateScheduleSuccessTest() {

        // Given :
        String expectedTitle = "change-title";
        String expectedLocation = "change-location";
        String expectedDate = "202301010130";
        int expectedMaxCount = 15;


        Member creator = TestUtils.initMemberAndMemberInfo("creator-uid","creator", "creator@gmail.com");
        Member joiner = TestUtils.initMemberAndMemberInfo("joiner-uid","joiner", "joiner@gmail.com");
        Moim moim = TestUtils.createMoimOnly();

        persist(creator.getRoles().get(0).getRole(),
                creator,
                joiner,
                moim);

        MoimJoinRequestDto moimJoinRequestDto = new MoimJoinRequestDto(moim.getId());
        moimMemberService.requestJoin(moimJoinRequestDto, creator);
        moimMemberService.requestJoin(moimJoinRequestDto, joiner);

        Schedule schedule = createScheduleForUpdate(moim, creator);
        MemberScheduleLinker linker = MemberScheduleLinker.memberJoinSchedule(joiner, schedule, ScheduleMemberState.ATTEND);
        persist(schedule, linker);

        ScheduleRequestDto scheduleRequestDto = new ScheduleRequestDto(
                moim.getId(),
                schedule.getId(),
                expectedTitle,
                expectedLocation,
                expectedDate,
                expectedMaxCount,
                false);

        // When :
        ScheduleResponseDto result = scheduleService.updateSchedule(scheduleRequestDto, creator);

        // Then :
        result.getScheduleMemberDto().sort((o1, o2) -> o1.getMoimMemberInfoDto().getMemberId().compareTo(o2.getMoimMemberInfoDto().getMemberId()));

        assertThat(result.getScheduleMemberDto().size()).isEqualTo(2);
        assertThat(result.getScheduleDto().getScheduleTitle()).isEqualTo(expectedTitle);
        assertThat(result.getScheduleDto().getScheduleLocation()).isEqualTo(expectedLocation);
        assertThat(result.getScheduleDto().getMaxCount()).isEqualTo(expectedMaxCount);
    }


    @Test
    @DisplayName("updateSchedule : 바뀐 게 없을 때")
    void updateScheduleFail2Test() {

        // Given :
        Member creator = TestUtils.initMemberAndMemberInfo("creator-uid","creator", "creator@gmail.com");
        Member joiner = TestUtils.initMemberAndMemberInfo("joiner-uid","joiner", "joiner@gmail.com");
        Moim moim = TestUtils.createMoimOnly();

        persist(creator.getRoles().get(0).getRole(),
                creator,
                joiner,
                moim);

        MoimJoinRequestDto moimJoinRequestDto = new MoimJoinRequestDto(moim.getId());
        moimMemberService.requestJoin(moimJoinRequestDto, creator);
        moimMemberService.requestJoin(moimJoinRequestDto, joiner);

        Schedule schedule = createScheduleForUpdate(moim, creator);
        MemberScheduleLinker linker = MemberScheduleLinker.memberJoinSchedule(joiner, schedule, ScheduleMemberState.ATTEND);
        persist(schedule, linker);

        ScheduleRequestDto scheduleRequestDto = new ScheduleRequestDto(
                moim.getId(),
                schedule.getId(),
                schedule.getScheduleTitle(),
                schedule.getScheduleLocation(),
                "202301010130",
                schedule.getMaxCount(),
                false);


        // When + Then:
        assertThatThrownBy(() -> scheduleService.updateSchedule(scheduleRequestDto, joiner))
                .isInstanceOf(RuntimeException.class);
    }


    @Test
    @DisplayName("updateSchedule : 권한 없어서 실패")
    void updateScheduleFailTest() {

        // Given :
        String expectedTitle = "change-title";
        String expectedLocation = "change-location";
        String expectedDate = "202301010130";
        int expectedMaxCount = 15;

        Member creator = TestUtils.initMemberAndMemberInfo("creator-uid","creator", "creator@gmail.com");
        Member joiner = TestUtils.initMemberAndMemberInfo("joiner-uid","joiner", "joiner@gmail.com");
        Moim moim = TestUtils.createMoimOnly();

        persist(creator.getRoles().get(0).getRole(),
                creator,
                joiner,
                moim);

        MoimJoinRequestDto moimJoinRequestDto = new MoimJoinRequestDto(moim.getId());
        moimMemberService.requestJoin(moimJoinRequestDto, creator);
        moimMemberService.requestJoin(moimJoinRequestDto, joiner);

        Schedule schedule = createScheduleForUpdate(moim, creator);
        MemberScheduleLinker linker = MemberScheduleLinker.memberJoinSchedule(joiner, schedule, ScheduleMemberState.ATTEND);
        persist(schedule, linker);

        ScheduleRequestDto scheduleRequestDto = new ScheduleRequestDto(
                moim.getId(),
                schedule.getId(),
                expectedTitle,
                expectedLocation,
                expectedDate,
                expectedMaxCount,
                false);


        // When + Then:
        assertThatThrownBy(() -> scheduleService.updateSchedule(scheduleRequestDto, joiner))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("updateSchedule : 모임에 속하지 않았을 때, 삭제 요청하면? 실패")
    void updateScheduleFail3Test() {

        // Given :
        String expectedTitle = "change-title";
        String expectedLocation = "change-location";
        String expectedDate = "202301010130";
        int expectedMaxCount = 15;

        Member creator = TestUtils.initMemberAndMemberInfo("creator-uid","creator", "creator@gmail.com");
        Member joiner = TestUtils.initMemberAndMemberInfo("joiner-uid","joiner", "joiner@gmail.com");
        Moim moim = TestUtils.createMoimOnly();
        Moim otherMoim = TestUtils.createMoimOnly("other-moim");

        persist(creator.getRoles().get(0).getRole(),
                creator,
                joiner,
                moim,
                otherMoim);

        MoimJoinRequestDto moimJoinRequestDto = new MoimJoinRequestDto(moim.getId());
        moimMemberService.requestJoin(moimJoinRequestDto, creator);

        MoimJoinRequestDto ohterMoimJoinRequestDto = new MoimJoinRequestDto(otherMoim.getId());
        moimMemberService.requestJoin(ohterMoimJoinRequestDto, joiner);

        Schedule schedule = createScheduleForUpdate(moim, creator);
        MemberScheduleLinker linker = MemberScheduleLinker.memberJoinSchedule(joiner, schedule, ScheduleMemberState.ATTEND);
        persist(schedule, linker);

        ScheduleRequestDto scheduleRequestDto = new ScheduleRequestDto(
                moim.getId(),
                schedule.getId(),
                expectedTitle,
                expectedLocation,
                expectedDate,
                expectedMaxCount,
                false);


        // When + Then:
        assertThatThrownBy(() -> scheduleService.updateSchedule(scheduleRequestDto, joiner))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("deleteScheduleTest : 성공")
    void deleteScheduleSuccessTest() {

        // Given :
        Member creator = TestUtils.initMemberAndMemberInfo("creator-uid","creator", "creator@gmail.com");
        Member joiner = TestUtils.initMemberAndMemberInfo("joiner-uid","joiner", "joiner@gmail.com");
        Moim moim = TestUtils.createMoimOnly();
        Moim otherMoim = TestUtils.createMoimOnly("other-moim");

        persist(creator.getRoles().get(0).getRole(),
                creator,
                joiner,
                moim,
                otherMoim);

        MoimJoinRequestDto moimJoinRequestDto = new MoimJoinRequestDto(moim.getId());
        moimMemberService.requestJoin(moimJoinRequestDto, creator);
        moimMemberService.requestJoin(moimJoinRequestDto, joiner);

        Schedule schedule = createScheduleForUpdate(moim, creator);
        MemberScheduleLinker linker = MemberScheduleLinker.memberJoinSchedule(joiner, schedule, ScheduleMemberState.ATTEND);
        persist(schedule, linker);
        em.flush();
        em.clear();

        // When
        scheduleService.deleteSchedule(schedule.getId(), creator);
        em.flush();
        em.clear();

        // Then
        Schedule findSchedule = scheduleRepository.findById(schedule.getId());
        MemberScheduleLinker linker1 = memberScheduleLinkerRepository.findByMemberAndScheduleId(creator.getId(), schedule.getId());
        MemberScheduleLinker linker2 = memberScheduleLinkerRepository.findByMemberAndScheduleId(joiner.getId(), schedule.getId());

        assertThat(findSchedule).isNull();
        assertThat(linker1).isNull();
        assertThat(linker2).isNull();
    }


    @Test
    @DisplayName("deleteScheduleTest : 실패. 권한 문제")
    void deleteScheduleFailTest() {

        // Given :
        Member creator = TestUtils.initMemberAndMemberInfo("creator-uid","creator", "creator@gmail.com");
        Member joiner = TestUtils.initMemberAndMemberInfo("joiner-uid","joiner", "joiner@gmail.com");
        Moim moim = TestUtils.createMoimOnly();
        Moim otherMoim = TestUtils.createMoimOnly("other-moim");

        persist(creator.getRoles().get(0).getRole(),
                creator,
                joiner,
                moim,
                otherMoim);

        MoimJoinRequestDto moimJoinRequestDto = new MoimJoinRequestDto(moim.getId());
        moimMemberService.requestJoin(moimJoinRequestDto, creator);
        moimMemberService.requestJoin(moimJoinRequestDto, joiner);

        Schedule schedule = createScheduleForUpdate(moim, creator);
        MemberScheduleLinker linker = MemberScheduleLinker.memberJoinSchedule(joiner, schedule, ScheduleMemberState.ATTEND);
        persist(schedule, linker);

        // When + Then
        assertThatThrownBy(() -> scheduleService.deleteSchedule(schedule.getId(), joiner))
                .isInstanceOf(RuntimeException.class);
    }


    @Test
    @DisplayName("changeMemberState : 스케쥴 처음으로 가입하고자 할 때임.")
    void changeMemberStateSuccess1Test() {

        // Given :
        Member creator = TestUtils.initMemberAndMemberInfo("creator-uid","creator", "creator@gmail.com");
        Member joiner = TestUtils.initMemberAndMemberInfo("joiner-uid","joiner", "joiner@gmail.com");
        Moim moim = TestUtils.createMoimOnly();
        Moim otherMoim = TestUtils.createMoimOnly("other-moim");

        persist(creator.getRoles().get(0).getRole(),
                creator,
                joiner,
                moim,
                otherMoim);

        MoimJoinRequestDto moimJoinRequestDto = new MoimJoinRequestDto(moim.getId());
        moimMemberService.requestJoin(moimJoinRequestDto, creator);
        moimMemberService.requestJoin(moimJoinRequestDto, joiner);

        Schedule schedule = createScheduleForUpdate(moim, creator);
        persist(schedule);
        em.flush();
        em.clear();

        // When
        ScheduleService.ChangeMemberTuple result = scheduleService.changeMemberState(schedule.getId(), true, joiner);
        em.flush();
        em.clear();

        // Then
        assertThat(result.getMemberScheduleLinker()).isNotNull();
        assertThat(result.getMemberScheduleLinker().getMemberState()).isEqualTo(ScheduleMemberState.ATTEND);
        assertThat(result.getMoimMemberInfoDto().getMemberUid()).isEqualTo(joiner.getUid());

        MemberScheduleLinker findLinker = memberScheduleLinkerRepository.findByMemberAndScheduleId(joiner.getId(), schedule.getId());
        assertThat(findLinker).isNotNull();
    }

    @Test
    @DisplayName("changeMemberState : 성공. 기존 스케쥴이 있는데 미참석으로 수정하는 경우.")
    void changeMemberStateSuccess2Test() {

        // Given :
        Member creator = TestUtils.initMemberAndMemberInfo("creator-uid","creator", "creator@gmail.com");
        Member joiner = TestUtils.initMemberAndMemberInfo("joiner-uid","joiner", "joiner@gmail.com");
        Moim moim = TestUtils.createMoimOnly();
        Moim otherMoim = TestUtils.createMoimOnly("other-moim");

        persist(creator.getRoles().get(0).getRole(),
                creator,
                joiner,
                moim,
                otherMoim);

        MoimJoinRequestDto moimJoinRequestDto = new MoimJoinRequestDto(moim.getId());
        moimMemberService.requestJoin(moimJoinRequestDto, creator);
        moimMemberService.requestJoin(moimJoinRequestDto, joiner);

        Schedule schedule = createScheduleForUpdate(moim, creator);
        MemberScheduleLinker linker = MemberScheduleLinker.memberJoinSchedule(joiner, schedule, ScheduleMemberState.ATTEND);
        persist(schedule, linker);
        em.flush();
        em.clear();

        // When
        ScheduleService.ChangeMemberTuple result = scheduleService.changeMemberState(schedule.getId(), false, joiner);
        em.flush();
        em.clear();

        // Then
        assertThat(result.getMemberScheduleLinker()).isNotNull();
        assertThat(result.getMemberScheduleLinker().getMemberState()).isEqualTo(ScheduleMemberState.NONATTEND);
        assertThat(result.getMoimMemberInfoDto().getMemberUid()).isEqualTo(joiner.getUid());
    }

    private void persist(Object ... objects) {
        Arrays.stream(objects).forEach(o -> em.persist(o));
    }

    private Schedule createScheduleForUpdate(Moim moim, Member creator) {
        return Schedule.createSchedule("title", "location",
                LocalDateTime.of(2023, 1, 1, 1, 30),
                10,
                moim,
                creator);
    }

}