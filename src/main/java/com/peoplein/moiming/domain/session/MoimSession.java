package com.peoplein.moiming.domain.session;


import com.peoplein.moiming.domain.DomainChecker;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.Schedule;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "moim_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimSession {

    @Id
    @Column(name = "moim_session_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String sessionName;
    private String sessionInfo;
    private int totalCost;

    private int curCost;

    private int totalSenderCount;

    private int curSenderCount;

    private LocalDateTime createdAt;
    private String createdUid;

    private LocalDateTime updatedAt;
    private String updatedUid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id")
    private Moim moim;

    /*
     @Nullable
     특정 일정에 대한 정산활동일 수도 있고, 아닐 수도 있다
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    /*
     다대다 연관관계 매핑
     */
    @OneToMany(mappedBy = "moimSession", cascade = CascadeType.PERSIST)
    private List<SessionCategoryItem> sessionCategoryItems = new ArrayList<>();

    @OneToMany(mappedBy = "moimSession", cascade = CascadeType.PERSIST)
    private List<MemberSessionLinker> memberSessionLinkers = new ArrayList<>();


    public static MoimSession createMoimSession(String sessionName, String sessionInfo, int totalCost, int totalSenderCount, String createdUid
            , Moim moim, Schedule schedule) {
        MoimSession moimSession = new MoimSession(
                sessionName, sessionInfo, totalCost, totalSenderCount, createdUid, moim, schedule
        );

        return moimSession;
    }


    private MoimSession(String sessionName, String sessionInfo, int totalCost, int totalSenderCount, String createdUid
            , Moim moim, Schedule schedule) {

        // NN 검증
        DomainChecker.checkRightString(this.getClass().getName(), false, sessionName, createdUid);
        DomainChecker.checkWrongObjectParams(this.getClass().getName(), moim);

        // 생성
        this.sessionName = sessionName;
        this.sessionInfo = sessionInfo;
        this.totalCost = totalCost;
        this.totalSenderCount = totalSenderCount;
        this.createdUid = createdUid;

        // 초기화
        this.curCost = 0;
        this.curSenderCount = 0;
        this.createdAt = LocalDateTime.now();

        // 연관관계 매핑
        this.moim = moim;
        this.schedule = schedule;
    }


    public void changeCreatedAt(LocalDateTime createdAt) {
        DomainChecker.checkWrongObjectParams(getClass().getName(), createdAt);
        this.createdAt = createdAt;
    }

    public void setCurCost(int curCost) {
        this.curCost = curCost;
    }

    public void setCurSenderCount(int curSenderCount) {
        this.curSenderCount = curSenderCount;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUpdatedUid(String updatedUid) {
        this.updatedUid = updatedUid;
    }
}
