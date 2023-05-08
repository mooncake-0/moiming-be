package com.peoplein.moiming.domain;

import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.enums.ScheduleMemberState;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;


class MemberScheduleLinkerTest {

    Moim moim;
    Member creator;
    Member joiner;
    Schedule schedule;

    @BeforeEach
    void setUp() {

        moim = TestUtils.createMoimOnly();
        creator = TestUtils.initMemberAndMemberInfo();
        joiner = TestUtils.initMemberAndMemberInfo("other-name", "other-email@mail.net");
        schedule = Schedule.createSchedule(
                "title",
                "location",
                LocalDateTime.of(2022, 1, 2, 6, 30),
                10,
                moim,
                creator);
    }

    @Test
    void constructorSuccessCaseTest() {
        // When :
        MemberScheduleLinker result = MemberScheduleLinker.memberJoinSchedule(joiner, schedule, ScheduleMemberState.ATTEND);

        // Then
        assertThat(result.getMember()).isEqualTo(joiner);
        assertThat(result.getSchedule()).isEqualTo(schedule);
        assertThat(result.getMemberState()).isEqualTo(ScheduleMemberState.ATTEND);
    }

    // max Count Validation.
    @Test
    void constructorFailCaseTest() {
        // Given
        Schedule schedule = Schedule.createSchedule(
                "title",
                "location",
                LocalDateTime.of(2022, 1, 2, 6, 30),
                1,
                moim,
                creator);

        // When + Then :
        assertThatThrownBy(() -> MemberScheduleLinker.memberJoinSchedule(joiner, schedule, ScheduleMemberState.ATTEND))
                .isInstanceOf(RuntimeException.class);
    }
}