package com.peoplein.moiming.domain.rules;

import com.peoplein.moiming.domain.Moim;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "rule_persist")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RulePersist extends MoimRule {

    private boolean doGreeting;
    private int attendMonthly;
    private int attendCount;

    public RulePersist(boolean doGreeting, int attendMonthly, int attendCount
            , Moim moim, String createdUid) {

        this.doGreeting = doGreeting;
        this.attendMonthly = attendMonthly;
        this.attendCount = attendCount;
        this.createdUid = createdUid;

         /*
         초기화
         */
        this.createdAt = LocalDateTime.now();
        this.ruleType = "P";

        /*
         연관관계 매핑 및 편의 메소드
         */
        this.moim = moim;
        this.moim.setHasRulePersist(true);
        this.moim.getMoimRules().add(this);
    }

}
