package com.peoplein.moiming.domain.moim;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.request.MoimReqDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;

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


    // 값 존재 > Update 해야함
    public void updateMoim(MoimUpdateReqDto requestDto) {

        if (requestDto.getMoimName() != null) {
            this.setMoimName(requestDto.getMoimName());
        }

        if (requestDto.getMoimInfo() != null) {
            this.setMoimInfo(requestDto.getMoimInfo());
        }

        if (requestDto.getMaxMember() != null) {
            this.setMaxMember(requestDto.getMaxMember());
        }

        if (requestDto.getAreaState() != null || requestDto.getAreaCity() != null) {
            // 둘 중 적어도 하나는 값이 존재한다
            String newAreaState = this.moimArea.getState();
            String newAreaCity = this.moimArea.getCity();

            if (requestDto.getAreaState() != null) {
                newAreaState = requestDto.getAreaState();
            }

            if (requestDto.getAreaCity() != null) {
                newAreaCity = requestDto.getAreaCity();
            }

            this.setMoimArea(new Area(newAreaState, newAreaCity));
        }
    }

    public void changeCategory(List<Category> categories) {
        for (Category category : categories) {
            MoimCategoryLinker.addMoimCategory(this, category);
        }
    }

    /*
     private 하게 바꿀 수 있도록 해서, update 함수 외에는 실행할 수 없게 한다
     update() 함수로 오는 과정에선 충분히 validation 거침
     */
    private void setMoimName(String moimName) {
        this.moimName = moimName;
    }

    private void setMoimInfo(String moimInfo) {
        this.moimInfo = moimInfo;
    }

    private void setMaxMember(int maxMember) {
        this.maxMember = maxMember;
    }

    private void setMoimArea(Area moimArea) {
        this.moimArea = moimArea;
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