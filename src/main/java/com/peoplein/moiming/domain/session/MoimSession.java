package com.peoplein.moiming.domain.session;


import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.Schedule;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "moim_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimSession {

    @Id
    @Column(name = "moim_session_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private int totalCost;

    private int curCost;

    private int totalSenderCount;

    private int curSenderCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

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

}
