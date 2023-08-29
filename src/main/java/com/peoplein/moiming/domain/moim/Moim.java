package com.peoplein.moiming.domain.moim;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    /*
      현재 MoimMemberState 가 Active 인 Member 기준
     */
    private int curMemberCount;

    private int maxMember;

    @Embedded
    private Area moimArea;

    private Long creatorId;

    private Long updaterId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "moim_join_rule_id")
    private MoimJoinRule moimJoinRule;

    @OneToMany(mappedBy = "moim", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MoimMember> moimMembers = new ArrayList<>();

    @OneToMany(mappedBy = "moim", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MoimCategoryLinker> moimCategoryLinkers = new ArrayList<>();

    public static Moim createMoim(String moimName, String moimInfo, int maxMember, Area moimArea, Member creator) {
        // 생성 시점에서 수정자는 동일
        Moim moim = new Moim(moimName, moimInfo, maxMember, moimArea, creator.getId());
        MoimMember.memberJoinMoim(creator, moim, MoimMemberRoleType.MANAGER, MoimMemberState.ACTIVE);
        return moim;
    }


    private Moim(String moimName, String moimInfo, int maxMember, Area moimArea, Long creatorId) {
        this.moimName = moimName;
        this.moimInfo = moimInfo;
        this.maxMember = maxMember;
        this.moimArea = moimArea;
        this.creatorId = creatorId;
        this.updaterId = creatorId;
    }


    public void addCurMemberCount() {
        this.curMemberCount++;
    }


    public void minusCurMemberCount() {
        if (this.curMemberCount == 0) {
            throw new MoimingApiException("이미 회원수가 0입니다");
        } else {
            this.curMemberCount--;
        }
    }

    public void setMoimJoinRule(MoimJoinRule moimJoinRule) {
        this.moimJoinRule = moimJoinRule;
    }

//    // 똑같은 게 있을 수도 있고, 아닐 수도 있다.
//    public MoimMemberState checkRuleJoinCondition(MemberInfo memberInfo, List<MoimMember> moimMembers) {
//
//        RuleJoin ruleJoin = this.getRuleJoin();
//
//        // 1. 생년월일 판별
//        if (ruleJoin.getBirthMax() != 0 && ruleJoin.getBirthMin() != 0) { // 판별조건이 있다면
//            if (memberInfo.getMemberBirth().getYear() < ruleJoin.getBirthMin()
//                    || memberInfo.getMemberBirth().getYear() > ruleJoin.getBirthMax()) {
//                return MoimMemberState.WAIT_BY_AGE;
//            }
//        }
//
//        // 2. 성별 판별
//        if (ruleJoin.getGender() != MemberGender.N) { // 판별조건이 있다면
//            if (ruleJoin.getGender() != memberInfo.getMemberGender()) {
//                return MoimMemberState.WAIT_BY_GENDER;
//            }
//        }
//
//        // count 부분이 자기 자신이라면 항상 빼야한다. 즉, 그 부분을 나눠줘야함.
//        if (!ruleJoin.isDupLeaderAvailable() || !ruleJoin.isDupManagerAvailable() || ruleJoin.getMoimMaxCount() > 0) { // 겸직 조건이 하나라도 있거나 최대 모임 갯수 조건이 있음
//            boolean isMemberAnyLeader = false;
//            boolean isMemberAnyManager = false;
//            int cntInactiveMoim = 0;
//
//            for (MoimMember memberMoimLinker : moimMembers) {
//                if (memberMoimLinker.getMoimMemberRoleType().equals(MoimMemberRoleType.LEADER)) {
//                    isMemberAnyLeader = true;
//                }
//                if (memberMoimLinker.getMoimMemberRoleType().equals(MoimMemberRoleType.MANAGER)) {
//                    isMemberAnyManager = true;
//                }
//                if (memberMoimLinker.getMemberState() != MoimMemberState.ACTIVE) {
//                    cntInactiveMoim++;
//                }
//            }
//
//            // 3. 겸직 여부 판별
//            if (!ruleJoin.isDupLeaderAvailable()) { // 모임장 겸직 금지인데
//                if (isMemberAnyLeader) return MoimMemberState.WAIT_BY_DUP;
//            }
//
//            if (!ruleJoin.isDupManagerAvailable()) { // 운영진 겸직 금지인데
//                if (isMemberAnyManager) return MoimMemberState.WAIT_BY_DUP;
//            }
//
//            // 4. 가입 모임 수 제한
//            if (ruleJoin.getMoimMaxCount() <= moimMembers.size() - cntInactiveMoim) {
//                return MoimMemberState.WAIT_BY_MOIM_CNT;
//            }
//        }
//
//        // 모임 가입 조건 충족시 그냥 가입된다
//        return MoimMemberState.ACTIVE;
//    }

    public boolean shouldCreateNewMemberMoimLinker(Optional<MoimMember> memberMoimLinker) {
        return memberMoimLinker.isEmpty();
    }
}