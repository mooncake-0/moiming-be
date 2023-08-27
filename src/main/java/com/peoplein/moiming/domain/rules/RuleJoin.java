package com.peoplein.moiming.domain.rules;

import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.enums.MemberGender;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "rule_join")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RuleJoin extends MoimRule {

    private int birthMax;
    private int birthMin;
    private MemberGender gender;
    private int moimMaxCount;
    private boolean dupLeaderAvailable;
    private boolean dupManagerAvailable;
    private boolean possibleReJoinIfExitedByWill;
    private boolean possibleReJoinIfExitedByForce;

    // TODO : DTO 수정 후, 가장 아래에 있는 생성자 삭제 및 나머지 코드 수정이 필요함.
    public RuleJoin(int birthMax,
                    int birthMin,
                    MemberGender gender,
                    int moimMaxCount,
                    boolean dupLeaderAvailable,
                    boolean dupManagerAvailable,
                    Moim moim,
                    Long createdMemberId,
                    boolean possibleReJoinIfExitedByWill,
                    boolean possibleReJoinIfExitedByForce) {

        this.birthMax = birthMax;
        this.birthMin = birthMin;
        this.gender = gender;
        this.moimMaxCount = moimMaxCount;
        this.dupLeaderAvailable = dupLeaderAvailable;
        this.dupManagerAvailable = dupManagerAvailable;
        this.createdMemberId = createdMemberId;
        this.possibleReJoinIfExitedByWill = possibleReJoinIfExitedByWill;
        this.possibleReJoinIfExitedByForce = possibleReJoinIfExitedByForce;

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

    public RuleJoin(int birthMax, int birthMin, MemberGender gender, int moimMaxCount, boolean dupLeaderAvailable, boolean dupManagerAvailable,
                    Moim moim, Long createdMemberId) {

        this.birthMax = birthMax;
        this.birthMin = birthMin;
        this.gender = gender;
        this.moimMaxCount = moimMaxCount;
        this.createdMemberId = createdMemberId;
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