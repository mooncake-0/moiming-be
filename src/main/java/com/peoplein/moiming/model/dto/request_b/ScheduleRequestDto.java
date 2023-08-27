package com.peoplein.moiming.model.dto.request_b;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ScheduleRequestDto {

    private Long moimId;
    private Long scheduleId;
    private String scheduleTitle;
    private String scheduleLocation;
    private String scheduleDate; // FORMAT : "yyyyMMddHHmm"
    private int maxCount;
    private boolean isFullNotice;
}
