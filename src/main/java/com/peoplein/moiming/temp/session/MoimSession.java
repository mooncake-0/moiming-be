package com.peoplein.moiming.temp.session;


import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.temp.Schedule;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 COMPILE FIXED
 */
//@Entity
@Getter
//@Table(name = "moim_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimSession {

//    @Id
//    @Column(name = "moim_session_id")
//    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String sessionName;
    private String sessionInfo;
    private int totalCost;

    private int curCost;

    private int totalSenderCount;

    private int curSenderCount;

    private boolean isFinished;

    private LocalDateTime createdAt;
    private Long createdMemberId;

    private LocalDateTime updatedAt;
    private Long updatedMemberId;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "moim_id")
    private Moim moim;

    /*
     @Nullable
     특정 일정에 대한 정산활동일 수도 있고, 아닐 수도 있다
     */
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    /*
     다대다 연관관계 매핑
     */
//    @OneToMany(mappedBy = "moimSession", cascade = CascadeType.PERSIST)
    private List<SessionCategoryItem> sessionCategoryItems = new ArrayList<>();

//    @OneToMany(mappedBy = "moimSession", cascade = CascadeType.PERSIST)
    private List<MemberSessionLinker> memberSessionLinkers = new ArrayList<>();


    public static MoimSession createMoimSession(String sessionName, String sessionInfo, int totalCost, int totalSenderCount, Long createdMemberId
            , Moim moim, Schedule schedule) {
        MoimSession moimSession = new MoimSession(
                sessionName, sessionInfo, totalCost, totalSenderCount, createdMemberId, moim, schedule
        );

        return moimSession;
    }


    private MoimSession(String sessionName, String sessionInfo, int totalCost, int totalSenderCount, Long createdMemberId
            , Moim moim, Schedule schedule) {

        // 생성
        this.sessionName = sessionName;
        this.sessionInfo = sessionInfo;
        this.totalCost = totalCost;
        this.totalSenderCount = totalSenderCount;
        this.createdMemberId = createdMemberId;

        // 초기화
        this.isFinished = false;
        this.curCost = 0;
        this.curSenderCount = 0;
        this.createdAt = LocalDateTime.now();

        // 연관관계 매핑
        this.moim = moim;
        this.schedule = schedule;
    }


    public void changeCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void addCurCost(int sentCost) {
        this.curCost += sentCost;
        if (this.curCost == this.totalCost) {
            //  정산완료
            this.isFinished = true;
        }
    }

    public void addCurSenderCount() {
        this.curSenderCount += 1;
    }

    // 보냈던걸 취소할 경우
    public void removalCurCost(int removalCurCost) {
        if (curCost - removalCurCost < 0) {
            throw new RuntimeException("계산이 맞지 않습니다 (curCost < 0)");
        }
        this.curCost -= removalCurCost;
    }

    public void removalCurSenderCount() {
        if (this.curSenderCount == 0) {
            throw new RuntimeException("인원이 맞지 않습니다 (curSenderCount < 0)");
        }
        this.curSenderCount -= 1;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUpdatedMemberId(Long updatedMemberId) {
        this.updatedMemberId = updatedMemberId;
    }
}