package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.session.MoimSession;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimSessionDto {


    private Long moimId;
    private Long scheduleId; // Nullable
    private String sessionName;
    private String sessionInfo;
    private int totalCost;
    private int totalSenderCount;

    /*
     생성 요청 시에는 부재인 정보들
     */
    private Long sessionId;
    private boolean isFinished;
    private int curCost;
    private int curSenderCount;
    private LocalDateTime createdAt;
    private Long createdMemberId;
    private LocalDateTime updatedAt;
    private Long updatedMemberId;

    /*
     요청시 사용 X, 응답시에는 moimId, scheduleId 필요하지 않는다
     MoimSession 도메인 정보를 가지고 DTO 를 만들어내는 Constructor
     */
    public MoimSessionDto(MoimSession moimSession) {

        this.moimId = moimSession.getMoim().getId();
        if (!Objects.isNull(moimSession.getSchedule())) { // 없을 수도 있음
            this.scheduleId = moimSession.getSchedule().getId();
        }
        //
        this.sessionId = moimSession.getId();
        this.sessionName = moimSession.getSessionName();
        this.sessionInfo = moimSession.getSessionInfo();
        this.totalCost = moimSession.getTotalCost();
        this.totalSenderCount = moimSession.getTotalSenderCount();
        this.curCost = moimSession.getCurCost();
        this.curSenderCount = moimSession.getCurSenderCount();
        this.createdAt = moimSession.getCreatedAt();
        this.createdMemberId = moimSession.getCreatedMemberId();
        this.updatedAt = moimSession.getUpdatedAt();
        this.updatedMemberId = moimSession.getUpdatedMemberId();
        this.isFinished = moimSession.isFinished();
    }

}