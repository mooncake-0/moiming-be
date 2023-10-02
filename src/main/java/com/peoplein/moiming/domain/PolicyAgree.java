package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.PolicyType;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.peoplein.moiming.domain.enums.PolicyType.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolicyAgree extends BaseEntity {

    public static final int CUR_MOIMING_REQ_POLICY_CNT = values().length;

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

    // 적합하지 않은 동의 여부를 검증한다 - 필수인데 False 인 항목
    private void checkInvalidPolicyAgreement(PolicyType policyType, boolean hasAgreed) {
        if (policyType.equals(SERVICE) && !hasAgreed) {
            throw new MoimingApiException("서비스 약관은 필수적인 동의 항목입니다");
        }

        if (policyType.equals(PRIVACY) && !hasAgreed) {
            throw new MoimingApiException("개인정보 약관은 필수적인 동의 항목입니다");
        }

        if (policyType.equals(AGE) && !hasAgreed) {
            throw new MoimingApiException("나이 약관은 필수적인 동의 항목입니다");
        }
    }

    public void setHasAgreed(boolean hasAgreed) {
        this.hasAgreed = hasAgreed;
    }

    public void setUpdaterId(Long updaterId) {
        this.updaterId = updaterId;
    }
}
