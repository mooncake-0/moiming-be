package com.peoplein.moiming.domain;

import com.peoplein.moiming.TestUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class ScheduleTest {

    @Test
    void hasAnyUpdateTest1() {
        // Given
        Moim moim = TestUtils.createMoimOnly();
        Member member = TestUtils.initMemberAndMemberInfo();
        Schedule schedule = Schedule.createSchedule("title", "location",
                getLocalDateTime(),
                10,
                moim,
                member);

        // When
        boolean result = schedule.hasAnyUpdate("change-title", "location", getLocalDateTime(), 10);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasAnyUpdateTest2() {
        // Given
        Moim moim = TestUtils.createMoimOnly();
        Member member = TestUtils.initMemberAndMemberInfo();
        Schedule schedule = Schedule.createSchedule("title", "location",
                getLocalDateTime(),
                10,
                moim,
                member);

        // When
        boolean result = schedule.hasAnyUpdate("title", "location", getLocalDateTime(), 10);

        // Then
        assertThat(result).isFalse();
    }

    LocalDateTime getLocalDateTime() {
        return LocalDateTime.of(2023, 1, 1, 1, 30);
    }


}