package com.peoplein.moiming.domain.moim;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.fixed.Category;
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


    public static Moim createMoim(String moimName, String moimInfo, int maxMember, Area moimArea, List<Category> categories, Member creator) {
        // 생성 시점에서 수정자는 동일
        Moim moim = new Moim(moimName, moimInfo, maxMember, moimArea, creator.getId());
        MoimMember.memberJoinMoim(creator, moim, MoimMemberRoleType.MANAGER, MoimMemberState.ACTIVE);
        for (Category category : categories) {
            MoimCategoryLinker.addMoimCategory(moim, category);
        }
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
        if (this.curMemberCount == 1) {
            throw new MoimingApiException("마지막 회원입니다");
        } else {
            this.curMemberCount--;
        }
    }

    public void setMoimJoinRule(MoimJoinRule moimJoinRule) {
        this.moimJoinRule = moimJoinRule;
    }


    // 내가 만든거 아님
    public boolean shouldCreateNewMemberMoimLinker(Optional<MoimMember> memberMoimLinker) {
        return memberMoimLinker.isEmpty();
    }


    // WARN: ID 변경은 MOCK 용
    public void changeMockObjectIdForTest(Long mockObjectId, Class<?> callClass) {
        if (callClass.getName().equals("TestMockCreator")) {
            this.id = mockObjectId;
        }
    }
}