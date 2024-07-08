package com.peoplein.moiming.domain.moim;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.file.File;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.peoplein.moiming.domain.enums.MoimMemberState.ACTIVE;
import static com.peoplein.moiming.exception.ExceptionValue.COMMON_INVALID_SITUATION;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;

@Slf4j
@Entity
@Getter
@Table(name = "moim")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Moim extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    private Long curManagerId;

    private Long updaterId;

    private Long imgFileId;

    private String imgUrl;

    private LocalDateTime deletedAt;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST) // ID 매핑과 동시에 Persist 를 걸어 set 시점에 자동으로 persist 시켜준다
    @JoinColumn(name = "moim_join_rule_id")
    private MoimJoinRule moimJoinRule;


    // MEMO :: Fetch 시 MoimMember 는 모든 상태를 불러온다 // curMemberCount 값과 size 가 다를 수 있음
    //         24.06 > List<Moim> 을 조회하는 일이 언제든 발생하기 때문에, N+1 방지를 위해 해당 조회 type LAZY 로 변경된다
    @OneToMany(mappedBy = "moim", cascade = CascadeType.PERSIST)
    private List<MoimMember> moimMembers = new ArrayList<>();

    @OneToMany(mappedBy = "moim", cascade = CascadeType.PERSIST)
    private List<MoimCategoryLinker> moimCategoryLinkers = new ArrayList<>();

    public Long getCreatorId() {
        return creatorId;
    }

    public static Moim createMoim(String moimName, String moimInfo, int maxMember, Area moimArea, List<Category> categories, Member creator) {
        // 생성 시점에서 수정자는 동일
        Moim moim = new Moim(moimName, moimInfo, maxMember, moimArea, creator.getId());
        MoimMember.memberJoinMoim(creator, moim, MoimMemberRoleType.MANAGER, ACTIVE);
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
        this.curManagerId = creatorId; // 초기에는 동일하다
        this.updaterId = creatorId;
    }


    public void addCurMemberCount() {
        if (this.curMemberCount + 1 > this.maxMember) {
            throw new MoimingApiException(ExceptionValue.MOIM_JOIN_FAIL_BY_MEMBER_FULL);
        }
        this.curMemberCount++;
    }


    public void minusCurMemberCount() {
        // 모임원이 0 이 된다면 삭제된 모임임
        if (this.curMemberCount < 1) {
            throw new MoimingApiException(ExceptionValue.MOIM_LEAVE_FAIL_BY_NO_MEMBER); // 운영중이지 않은 모임
        } else {
            this.curMemberCount--;
        }
    }


    public void judgeMemberJoinByRule(MoimMember moimMember, Member curMember) {

        this.moimJoinRule.judgeByRule(curMember); // 가입시도를 진행한다 / 성공하면 하기 플로우를 진행한다

        if (moimMember != null) { // moimMember 가 있었던 상태면 ACTIVE 변환
            moimMember.changeMemberState(ACTIVE);
        } else { // 없었던 상태면 새로 생성, PERSIST 되어 있기 때문에 따로 전달 불필요
            MoimMember.memberJoinMoim(curMember, this, MoimMemberRoleType.NORMAL, ACTIVE);
        }
    }


    // 값 존재 > Update 해야함 > Create 처럼 List<Category> 는 따로 넣어주는게 맞다
    public void updateMoim(MoimUpdateReqDto requestDto, List<Category> categories, Long updaterId) {

        boolean isChanged = false;

        if (requestDto.getMoimName() != null) {
            isChanged = true;
            this.setMoimName(requestDto.getMoimName());
        }

        if (requestDto.getMoimInfo() != null) {
            isChanged = true;
            this.setMoimInfo(requestDto.getMoimInfo());
        }

        if (requestDto.getAreaState() != null || requestDto.getAreaCity() != null) {
            // 둘 중 적어도 하나는 바뀌므로, 새 Area 필요
            isChanged = true;
            this.setMoimArea(this.moimArea.checkAndIssueNewArea(requestDto.getAreaState(), requestDto.getAreaCity()));
        }

        if (!categories.isEmpty()) {
            isChanged = true;
            for (Category newCategory : categories) {
                for (MoimCategoryLinker curCategoryLinker : this.getMoimCategoryLinkers()) {
                    if (newCategory.getCategoryDepth() == curCategoryLinker.getCategory().getCategoryDepth()) {
                        curCategoryLinker.changeCategory(newCategory);
                    }
                }
            }
        }

        if (isChanged) {
            this.updaterId = updaterId; // validate 통과이므로 call 되면 수정되는 것
        }
    }


    // MEMO :: Max Member 정보만 가입 조건 UI 에 있어서, 해당 플로우에서 요청이 날라오기 때문에 다로 빼줌
    public void updateMaxMember(int maxMember, Long updaterId) {
        if (maxMember < this.curMemberCount) { // 현존하는 회원 수보다 적게 수정하려 시도
            throw new MoimingApiException(ExceptionValue.MOIM_UPDATE_FAIL_BY_EXCEED_CUR_MEMBER);
        }
        this.setMaxMember(maxMember);
        this.updaterId = updaterId;
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

    private void changeCategory(List<Category> categories) {
        for (Category category : categories) {
            MoimCategoryLinker.addMoimCategory(this, category);
        }
    }


    // 모임 삭제를 인식하는 기준이 된다
    public void markDeletedTime() {
        this.deletedAt = LocalDateTime.now();
    }


    //
    public void changeCurManagerId(Long curManagerId) {
        this.curManagerId = curManagerId;
    }


    /*
     Moim 이 Persist 되어 있다면 자동으로 JoinRule 도 반영된다
     */
    public void initJoinRule(MoimJoinRule moimJoinRule) {
        this.moimJoinRule = moimJoinRule;
    }


    public void changeImg(File file) {
        this.imgFileId = file.getId();
        this.imgUrl = file.getFileUrl();
    }

    public void deleteImg() {
        this.imgFileId = null;
        this.imgUrl = null;
    }


    public boolean hasImg() {
        if (this.imgFileId != null && this.imgUrl != null) {
            return true;
        } else if (this.imgFileId == null && this.imgUrl == null) {
            return false;
        } else {
            log.error("{}, Moim Status :: {}", this.getClass().getName(), "Moim 이미지 상태 이상");
            throw new MoimingApiException(COMMON_INVALID_SITUATION);
        }
    }


    // Sample 혹은 필요시 사용
    public void changeMoimCreatedTimeInNeed(LocalDateTime neededChangeCreatedAt) {
        changeCreatedAtInNeed(neededChangeCreatedAt);
    }
}