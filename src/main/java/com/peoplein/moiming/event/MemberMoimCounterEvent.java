package com.peoplein.moiming.event;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MemberMoimCounterEvent {

    private final Long memberId;
    private final Long moimId;
    private final LocalDate visitDated;

    private MemberMoimCounterEvent(Long memberId, Long moimId, LocalDate visitDated) {
        this.memberId = memberId;
        this.moimId = moimId;
        this.visitDated = visitDated;
    }

    public static MemberMoimCounterEvent create(Long memberId, Long moimId, LocalDate visitDated) {
        return new MemberMoimCounterEvent(memberId, moimId, visitDated);
    }
}