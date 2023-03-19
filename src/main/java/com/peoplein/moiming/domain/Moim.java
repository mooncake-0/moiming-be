package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.embeddable.Area;
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

@Entity
@Getter
@Table(name = "moim")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Moim {

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

    private LocalDateTime createdAt;
    private String createdUid;

    private LocalDateTime updatedAt;
    private String updatedUid;

    @OneToMany(mappedBy = "moim", cascade = CascadeType.ALL)
    private List<MoimRule> moimRules = new ArrayList<>();

    @OneToMany(mappedBy = "moim", cascade = CascadeType.ALL)
    private List<MemberMoimLinker> memberMoimLinkers = new ArrayList<>();

    public static Moim createMoim(String moimName,
                                  String moimInfo,
                                  String moimPfImg,
                                  Area moimArea,
                                  String createdUid) {
        Moim moim = new Moim(moimName, moimInfo, moimPfImg, moimArea, createdUid);
        return moim;
    }

    private Moim(String moimName, String moimInfo, String moimPfImg, Area moimArea, String createdUid) {

        DomainChecker.checkRightString("Moim Entity", false, moimName, moimArea.getCity(), moimArea.getState(), createdUid);
        this.moimName = moimName;
        this.moimInfo = moimInfo;
        this.moimPfImg = moimPfImg;
        this.moimArea = moimArea;
        this.createdUid = createdUid;

        /*
         기본값
         */
        this.createdAt = LocalDateTime.now();
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
        DomainChecker.checkRightString(this.getClass().getName(), false, moimName);
        this.moimName = moimName;
    }

    public void changeMoimArea(Area moimArea) {
        DomainChecker.checkRightString(this.getClass().getName(), false, moimArea.getState(), moimArea.getCity());
        this.moimArea = moimArea;
    }

    public void changeUpdatedUid(String updatedUid) {
        DomainChecker.checkRightString(this.getClass().getName(), false, updatedUid);
        this.updatedUid = updatedUid;
    }

    public void setMoimInfo(String moimInfo) {
        this.moimInfo = moimInfo;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
