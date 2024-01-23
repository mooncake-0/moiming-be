package com.peoplein.moiming.domain.moim;


import com.peoplein.moiming.domain.member.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimDailyCount {

    @Id
    @GeneratedValue
    @Column(name = "moim_daily_count_id")
    private Long id;

    private int count;

    private LocalDate accessDate;

    /*
     연관관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id")
    private Moim moim;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public static MoimDailyCount createMoimAccessCount(Member member, Moim moim) {
        return new MoimDailyCount(member, moim);
    }

    private MoimDailyCount(Member member, Moim moim) {
        this.member = member;
        this.moim = moim;

        // 초기화
        this.accessDate = LocalDate.now();
        this.count = 1;
    }

    public void increaseMemberAccessCount() {
        this.count += 1;
    }
}
