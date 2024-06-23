package com.peoplein.moiming.domain.member;

import com.peoplein.moiming.domain.BaseEntity;
import com.peoplein.moiming.domain.enums.MemberGender;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.time.LocalDate;

@Slf4j
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSetting extends BaseEntity {

    // 식별관계를 위해 직접 memberId 받아온다 // GeneratedValue 를 사용하지 않음
    @Id
    private Long id;

    private boolean allowNoticeNotify;
    private boolean allowMoimNotify;

    @MapsId
    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public static MemberSetting saveMemberSetting(Member member) {
        return new MemberSetting(member);
    }

    private MemberSetting(Member member) {
        this.id = member.getId();
        this.member = member;
        this.allowNoticeNotify = false;
        this.allowMoimNotify = false;
    }

    public Member getMember() {
        return member;
    }

    public boolean isMoimActivityNotifyAllowed() {
        return this.allowMoimNotify;
    }

    public boolean isNoticeNotifyAllowed() {
        return this.allowNoticeNotify;
    }

    public void changeNotifySettings(boolean allowNoticeNotify, boolean allowMoimNotify) {
        if (this.allowNoticeNotify != allowNoticeNotify) {
            this.allowNoticeNotify = allowNoticeNotify;
        }

        if (this.allowMoimNotify != allowMoimNotify) {
            this.allowMoimNotify = allowMoimNotify;
        }
    }

}
