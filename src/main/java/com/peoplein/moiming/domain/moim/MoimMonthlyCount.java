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
public class MoimMonthlyCount {

    @Id
    @GeneratedValue
    @Column(name = "moim_monthly_count_id")
    private Long id;

    private int monthlyCount;

    private LocalDate countDate; // 년 / 월에 해당하는 데이터


    /*
     연관관계
    */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id")
    private Moim moim;

    public static MoimMonthlyCount createMoimMonthlyCount(Moim moim) {
        return new MoimMonthlyCount(moim);
    }

    private MoimMonthlyCount(Moim moim) {
        this.moim = moim;

        // 초기화
        LocalDate currentYearMonth = LocalDate.now();
        this.countDate = currentYearMonth.withDayOfMonth(1);
        this.monthlyCount = 1;
    }

    public void increaseMonthlyCount() {
        this.monthlyCount += 1;
    }

}
