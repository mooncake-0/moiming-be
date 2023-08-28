package com.peoplein.moiming.domain.moim;

import com.peoplein.moiming.domain.BaseEntity;
import com.peoplein.moiming.domain.enums.MemberGender;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "moim_join_rule")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimJoinRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "moim_join_rule_id")
    private Long id;

    /*
     isAgeRule = false 일 시 ageMax, ageMin = -1
     */
    private boolean isAgeRule;

    private int ageMax;

    private int ageMin;

    @Enumerated(EnumType.STRING)
    private MemberGender memberGender;
    public static MoimJoinRule createMoimJoinRule(boolean isAgeRule, int ageMax, int ageMin, MemberGender memberGender) {
        if (!isAgeRule) {
            ageMax = -1;
            ageMin = -1;
        }
        return new MoimJoinRule(isAgeRule, ageMax, ageMin, memberGender);
    }


    private MoimJoinRule(boolean isAgeRule, int ageMax, int ageMin, MemberGender memberGender) {
        this.isAgeRule = isAgeRule;
        this.ageMax = ageMax;
        this.ageMin = ageMin;
        this.memberGender = memberGender;
    }

}
