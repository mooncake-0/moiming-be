package com.peoplein.moiming.model.dto.domain;

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
    private String createdUid;
    private LocalDateTime updatedAt;
    private String updatedUid;
}
