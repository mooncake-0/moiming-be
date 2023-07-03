package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.PolicyType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PolicyAgree {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "policy_agree_id")
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private PolicyType policyType;

    private boolean isAgreed;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdUid;
    private String updatedUid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public static PolicyAgree createPolicyAgree(Member member, PolicyType policyType, boolean isAgreed) {
        return new PolicyAgree(member, policyType, isAgreed);
    }

    private PolicyAgree(Member member, PolicyType policyType, boolean isAgreed) {

        DomainChecker.checkWrongObjectParams(getClass().getName(), member, policyType);

        this.policyType = policyType;
        this.isAgreed = isAgreed;

        // 연관관계
        this.member = member;

        // 초기화
        this.createdAt = LocalDateTime.now();
        this.createdUid = member.getUid();

    }

    public void setAgreed(boolean isAgreed) {
        this.isAgreed = isAgreed;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void changeUpdatedUid(String updatedUid) {
        if (!StringUtils.hasText(updatedUid)) {
            throw new RuntimeException("약관 수정 변경자 수정 에러");
        }
        this.updatedUid = updatedUid;
    }

}
