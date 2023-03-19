package com.peoplein.moiming.domain.rules;

import com.peoplein.moiming.domain.DomainChecker;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.enums.MemberGender;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "rule_join")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RuleJoin extends MoimRule {

    private int birthMax;
    private int birthMin;
    private MemberGender gender;
    private int moimMaxCount;
    private boolean dupLeaderAvailable;
    private boolean dupManagerAvailable;

    public RuleJoin(int birthMax, int birthMin, MemberGender gender, int moimMaxCount, boolean dupLeaderAvailable, boolean dupManagerAvailable,
                    Moim moim, String createdUid) {

        DomainChecker.checkWrongObjectParams(this.getClass().getName(), gender, moim);
        DomainChecker.checkRightString(this.getClass().getName(), false, createdUid);

        this.birthMax = birthMax;
        this.birthMin = birthMin;
        this.gender = gender;
        this.moimMaxCount = moimMaxCount;
        this.createdUid = createdUid;
        this.dupLeaderAvailable = dupLeaderAvailable;
        this.dupManagerAvailable = dupManagerAvailable;

        /*
         초기화
         */
        this.createdAt = LocalDateTime.now();
        this.ruleType = "J";

        /*
         연관관계 매핑 및 편의 메소드
         */
        this.moim = moim;
        this.moim.setHasRuleJoin(true);
        this.moim.getMoimRules().add(this);
    }

}
