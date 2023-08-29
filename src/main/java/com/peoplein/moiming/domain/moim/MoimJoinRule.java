package com.peoplein.moiming.domain.moim;

import com.peoplein.moiming.domain.BaseEntity;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.exception.MoimingApiException;
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

    public void judgeByRule(Member member) {

        if (this.isAgeRule) {
            // 나이 판별
        }

        if (this.memberGender != MemberGender.N) {
            if (!member.getMemberInfo().getMemberGender().equals(this.memberGender)) {
                throw new MoimingApiException("요청한 유저가 가입 조건에 부합하지 않습니다: 요구성별 - " +  this.memberGender.toString());
            }
        }

    }

}
