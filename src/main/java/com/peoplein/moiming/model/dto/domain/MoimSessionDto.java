package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.session.MoimSession;
import com.peoplein.moiming.model.dto.response.MoimSessionResponseDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private int curCost;
    private int curSenderCount;
    private LocalDateTime createdAt;
    private String createdUid;
    private LocalDateTime updatedAt;
    private String updatedUid;

    /*
     요청시 사용 X, 응답시에는 moimId, scheduleId 필요하지 않는다
     MoimSession 도메인 정보를 가지고 DTO 를 만들어내는 Constructor
     */
    public MoimSessionDto(MoimSession moimSession) {
        this.sessionId = moimSession.getId();
        this.sessionName = moimSession.getSessionName();
        this.sessionInfo = moimSession.getSessionInfo();
        this.totalCost = moimSession.getTotalCost();
        this.totalSenderCount = moimSession.getTotalSenderCount();
        this.curCost = moimSession.getCurCost();
        this.curSenderCount = moimSession.getCurSenderCount();
        this.createdAt = moimSession.getCreatedAt();
        this.createdUid = moimSession.getCreatedUid();
        this.updatedAt = moimSession.getUpdatedAt();
        this.updatedUid = moimSession.getUpdatedUid();
    }

//    /*
//     Constructor -2
//     요청 Object Mapper 를 위한 생성자
//     */
//    public MoimSessionDto(Long moimId, Long scheduleId, String sessionName, String sessionInfo, int totalCost, int totalSenderCount) {
//        this.moimId = moimId;
//        this.scheduleId = scheduleId;
//        this.sessionName = sessionName;
//        this.sessionInfo = sessionInfo;
//        this.totalCost = totalCost;
//        this.totalSenderCount = totalSenderCount;
//    }
}
