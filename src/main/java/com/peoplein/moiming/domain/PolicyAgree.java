package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.PolicyType;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

import static com.peoplein.moiming.domain.enums.PolicyType.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolicyAgree extends BaseEntity {

    public static final int CUR_MOIMING_REQ_POLICY_CNT = PolicyType.values().length;

    @Id
    @GeneratedValue
    @Column(name = "policy_agree_id")
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private PolicyType policyType;

    private boolean hasAgreed;

    private Long creatorId;
    private Long updaterId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public static PolicyAgree createPolicyAgree(Member member, PolicyType policyType, boolean hasAgreed) {
        return new PolicyAgree(member, policyType, hasAgreed);
    }

    private PolicyAgree(Member member, PolicyType policyType, boolean hasAgreed) {

        checkInvalidPolicyAgreement(policyType, hasAgreed);

        this.policyType = policyType;
        this.hasAgreed = hasAgreed;

        // 연관관계
        this.member = member;

        // 초기화
        this.creatorId = member.getId();

    }

    public void changeHasAgreed(boolean hasAgreed, Long memberId) {

        if (!(this.getPolicyType().equals(MARKETING_EMAIL) || this.getPolicyType().equals(MARKETING_SMS))) {
            throw new MoimingApiException(MEMBER_POLICY_ESSENTIAL);
        }

        //
        if (!Objects.equals(this.member.getId(), memberId)) {
            throw new MoimingApiException(MEMBER_POLICY_UPDATE_FORBIDDEN);
        }

        this.hasAgreed = hasAgreed;
        this.updaterId = memberId;
    }


    // 적합하지 않은 동의 여부를 검증한다 - 필수인데 False 인 항목
    private void checkInvalidPolicyAgreement(PolicyType policyType, boolean hasAgreed) {
        if (policyType.equals(SERVICE) && !hasAgreed
                || policyType.equals(PRIVACY) && !hasAgreed
                || policyType.equals(AGE) && !hasAgreed
        ) {
            throw new MoimingApiException(MEMBER_POLICY_ESSENTIAL);
        }
    }

}
