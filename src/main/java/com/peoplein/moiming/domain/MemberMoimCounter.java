package com.peoplein.moiming.domain;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberMoimCounter {

    @Id
    @Column(name = "member_moim_counter_id")
    @GeneratedValue
    private Long id;

    private Long memberId;

    private Long moimId;

    private LocalDate visitDate;

    private MemberMoimCounter(Long memberId, Long moimId, LocalDate visitDate) {
        this.memberId = memberId;
        this.moimId = moimId;
        this.visitDate = visitDate;
    }

    public static MemberMoimCounter create(long memberId, long moimId, LocalDate visitDate) {
        if (visitDate == null)
            throw new IllegalArgumentException(String.format("wrong input. MemberId = {}, MoimId = {}, visitDate = {}",
                    memberId, moimId, visitDate));
        return new MemberMoimCounter(memberId, moimId, visitDate);
    }

}
