package com.peoplein.moiming.model.query;

import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.rules.MoimRule;
import com.peoplein.moiming.model.dto.domain.CategoryDto;
import com.peoplein.moiming.model.dto.domain.RuleJoinDto;
import com.peoplein.moiming.model.dto.domain.RulePersistDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 Moim 의 기본 정보 가져오기 위한 Query Dto
 */
@Data
public class QueryJoinedMoimBasic {

    /*
     1. Moim 정보
     */
    private Long moimId;
    private String moimName;
    private String moimInfo;
    private boolean hasRuleJoin;
    private boolean hasRulePersist;
    private int curMemberCount;
    private Area moimArea;
    private LocalDateTime createdAt;
    private Long createdMemberId;
    private LocalDateTime updatedAt;
    private Long updatedMemberId;

    /*
     2. MemberMoimLinker 정보
     */
    private MoimRoleType moimRoleType;
    private MoimMemberState memberState;
    private LocalDateTime memberLinkerCreatedAt;
    private LocalDateTime memberLinkerUpdatedAt;

    /*
     3. Rule & Category 정보 - Collection Fetch 필요
     */
    private RuleJoinDto ruleJoinDto;
    private RulePersistDto rulePersistDto;
    private List<CategoryDto> categoriesDto = new ArrayList<>();

    public QueryJoinedMoimBasic(
            Long moimId, String moimName, String moimInfo, boolean hasRuleJoin, boolean hasRulePersist, int curMemberCount
            , Area moimArea, LocalDateTime createdAt, Long createdMemberId, LocalDateTime updatedAt, Long updatedMemberId
            , MoimRoleType moimRoleType, MoimMemberState memberState, LocalDateTime memberLinkerCreatedAt, LocalDateTime memberLinkerUpdatedAt
    ) {
        this.moimId = moimId;
        this.moimName = moimName;
        this.moimInfo = moimInfo;
        this.hasRuleJoin = hasRuleJoin;
        this.hasRulePersist = hasRulePersist;
        this.curMemberCount = curMemberCount;
        this.moimArea = moimArea;
        this.createdAt = createdAt;
        this.createdMemberId = createdMemberId;
        this.updatedAt = updatedAt;
        this.updatedMemberId= updatedMemberId;
        this.moimRoleType = moimRoleType;
        this.memberState = memberState;
        this.memberLinkerCreatedAt = memberLinkerCreatedAt;
        this.memberLinkerUpdatedAt = memberLinkerUpdatedAt;
    }

}
