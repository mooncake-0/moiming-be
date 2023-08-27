package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.rules.MoimRule;
import com.peoplein.moiming.domain.rules.RuleJoin;
import com.peoplein.moiming.domain.rules.RulePersist;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Getter
@Table(name = "moim")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Moim extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "moim_id")
    private Long id;

    private String moimName;
    private String moimInfo;
    private String moimPfImg;

    private boolean hasRuleJoin;
    private boolean hasRulePersist;

    /*
      현재 MoimMemberState 가 Active 인 Member 기준
     */
    private int curMemberCount;
    @Embedded
    private Area moimArea;

    private Long createdMemberId;

    private Long updatedMemberId;

    @OneToMany(mappedBy = "moim", cascade = CascadeType.ALL)
    private List<MoimRule> moimRules = new ArrayList<>();

    @OneToMany(mappedBy = "moim", cascade = CascadeType.ALL)
    private List<MemberMoimLinker> memberMoimLinkers = new ArrayList<>();

    public static Moim createMoim(String moimName,
                                  String moimInfo,
                                  String moimPfImg,
                                  Area moimArea,
                                  Long createdMemberId) {
        Moim moim = new Moim(moimName, moimInfo, moimPfImg, moimArea, createdMemberId);
        return moim;
    }

    private Moim(String moimName, String moimInfo, String moimPfImg, Area moimArea, Long createdMemberId) {

        this.moimName = moimName;
        this.moimInfo = moimInfo;
        this.moimPfImg = moimPfImg;
        this.moimArea = moimArea;
        this.createdMemberId = createdMemberId;

        /*
         기본값
         */
        this.hasRulePersist = false;
        this.curMemberCount = 0;
    }

    /*
     Setter Open
     */
    public void setHasRuleJoin(boolean hasRuleJoin) {
        this.hasRuleJoin = hasRuleJoin;
    }

    public void setHasRulePersist(boolean hasRulePersist) {
        this.hasRulePersist = hasRulePersist;
    }

    public void addCurMemberCount() {
        this.curMemberCount++;
    }

    public void minusCurMemberCount() {
        if (this.curMemberCount == 0) {
            // TOOD:: ERROR 정확히 명시, 그룹 삭제인 것으로 추정
            System.out.println("더 이상 모임원이 없습니다");
        } else {
            this.curMemberCount--;
        }
    }

    public RuleJoin getRuleJoin() {
        for (MoimRule moimRule : moimRules) {
            if (moimRule instanceof RuleJoin) {
                return (RuleJoin) moimRule;
            }
        }
        return null;
    }

    public RulePersist getRulePersist() {
        for (MoimRule moimRule : moimRules) {
            if (moimRule instanceof RulePersist) {
                return (RulePersist) moimRule;
            }
        }
        return null;
    }

    public void removeRuleJoin() {

        setHasRuleJoin(false);
        this.moimRules.removeIf(moimRule -> moimRule instanceof RuleJoin);

    }

    public void removeRulePersist() {
        setHasRulePersist(false);
        this.moimRules.removeIf(moimRule -> moimRule instanceof RulePersist);
    }

    public void changeMoimName(String moimName) {
        this.moimName = moimName;
    }

    public void changeMoimArea(Area moimArea) {
        this.moimArea = moimArea;
    }

    public void changeUpdatedUid(Long updatedMemberId) {
        this.updatedMemberId = updatedMemberId;
    }

    public void setMoimInfo(String moimInfo) {
        this.moimInfo = moimInfo;
    }


    // 똑같은 게 있을 수도 있고, 아닐 수도 있다.
    public MoimMemberState checkRuleJoinCondition(MemberInfo memberInfo, List<MemberMoimLinker> memberMoimLinkers) {

        RuleJoin ruleJoin = this.getRuleJoin();

        // 1. 생년월일 판별
        if (ruleJoin.getBirthMax() != 0 && ruleJoin.getBirthMin() != 0) { // 판별조건이 있다면
            if (memberInfo.getMemberBirth().getYear() < ruleJoin.getBirthMin()
                    || memberInfo.getMemberBirth().getYear() > ruleJoin.getBirthMax()) {
                return MoimMemberState.WAIT_BY_AGE;
            }
        }

        // 2. 성별 판별
        if (ruleJoin.getGender() != MemberGender.N) { // 판별조건이 있다면
            if (ruleJoin.getGender() != memberInfo.getMemberGender()) {
                return MoimMemberState.WAIT_BY_GENDER;
            }
        }

        // count 부분이 자기 자신이라면 항상 빼야한다. 즉, 그 부분을 나눠줘야함.
        if (!ruleJoin.isDupLeaderAvailable() || !ruleJoin.isDupManagerAvailable() || ruleJoin.getMoimMaxCount() > 0) { // 겸직 조건이 하나라도 있거나 최대 모임 갯수 조건이 있음
            boolean isMemberAnyLeader = false;
            boolean isMemberAnyManager = false;
            int cntInactiveMoim = 0;

            for (MemberMoimLinker memberMoimLinker : memberMoimLinkers) {
                if (memberMoimLinker.getMoimRoleType().equals(MoimRoleType.LEADER)) {
                    isMemberAnyLeader = true;
                }
                if (memberMoimLinker.getMoimRoleType().equals(MoimRoleType.MANAGER)) {
                    isMemberAnyManager = true;
                }
                if (memberMoimLinker.getMemberState() != MoimMemberState.ACTIVE) {
                    cntInactiveMoim++;
                }
            }

            // 3. 겸직 여부 판별
            if (!ruleJoin.isDupLeaderAvailable()) { // 모임장 겸직 금지인데
                if (isMemberAnyLeader) return MoimMemberState.WAIT_BY_DUP;
            }

            if (!ruleJoin.isDupManagerAvailable()) { // 운영진 겸직 금지인데
                if (isMemberAnyManager) return MoimMemberState.WAIT_BY_DUP;
            }

            // 4. 가입 모임 수 제한
            if (ruleJoin.getMoimMaxCount() <= memberMoimLinkers.size() - cntInactiveMoim) {
                return MoimMemberState.WAIT_BY_MOIM_CNT;
            }
        }

        // 모임 가입 조건 충족시 그냥 가입된다
        return MoimMemberState.ACTIVE;
    }

    public boolean shouldCreateNewMemberMoimLinker(Optional<MemberMoimLinker> memberMoimLinker) {
        return memberMoimLinker.isEmpty();
    }
}