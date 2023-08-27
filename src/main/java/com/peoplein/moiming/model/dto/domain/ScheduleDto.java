package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.Schedule;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleDto {
    private Long scheduleId;
    private String scheduleTitle;
    private String scheduleLocation;
    private LocalDateTime scheduleDate;
    private int maxCount;
    private boolean isClosed;
    private LocalDateTime createdAt;
    private Long createdMemberId;
    private LocalDateTime updatedAt;
    private Long updatedMemberId;

    /*
     Constructor 1
     Entity 로 Domain Dto 만들기
     */
    public ScheduleDto(Schedule schedule) {
        this.scheduleId = schedule.getId();
        this.scheduleTitle = schedule.getScheduleTitle();
        this.scheduleLocation = schedule.getScheduleLocation();
        this.scheduleDate = schedule.getScheduleDate();
        this.maxCount = schedule.getMaxCount();
        this.isClosed = schedule.isClosed();
        this.createdAt = schedule.getCreatedAt();
        this.createdMemberId = schedule.getCreatedMemberId();
        this.updatedAt = schedule.getUpdatedAt();
        this.updatedMemberId = schedule.getUpdatedMemberId();
    }

    public static ScheduleDto createScheduleDto(Schedule schedule) {
        return new ScheduleDto(
                schedule.getId(),
                schedule.getScheduleTitle(),
                schedule.getScheduleLocation(),
                schedule.getScheduleDate(),
                schedule.getMaxCount(),
                schedule.isClosed(),
                schedule.getCreatedAt(),
                schedule.getCreatedMemberId(),
                schedule.getUpdatedAt(),
                schedule.getUpdatedMemberId()
        );
    }
}