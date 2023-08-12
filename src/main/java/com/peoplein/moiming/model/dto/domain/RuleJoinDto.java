package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.rules.RuleJoin;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RuleJoinDto {

    private Long moimId;
    private int birthMax;
    private int birthMin;
    private MemberGender gender;
    private int moimMaxCount;

    private boolean dupLeaderAvailable;
    private boolean dupManagerAvailable;
    private LocalDateTime createdAt;
    private Long createdMemberId;
    private LocalDateTime updatedAt;
    private Long updatedMemberId;


    /*
     Constructor -1
     RuleJoin Entity 를 통한 Dto 형성
     */
    public RuleJoinDto(RuleJoin ruleJoin) {
        if (!Objects.isNull(ruleJoin)) {
            this.moimId = ruleJoin.getMoim().getId();
            this.birthMax = ruleJoin.getBirthMax();
            this.birthMin = ruleJoin.getBirthMin();
            this.gender = ruleJoin.getGender();
            this.moimMaxCount = ruleJoin.getMoimMaxCount();
            this.dupLeaderAvailable = ruleJoin.isDupLeaderAvailable();
            this.dupManagerAvailable = ruleJoin.isDupManagerAvailable();
            this.createdAt = ruleJoin.getCreatedAt();
            this.createdMemberId = ruleJoin.getCreatedMemberId();
            this.updatedAt = ruleJoin.getUpdatedAt();
            this.updatedMemberId = ruleJoin.getUpdatedMemberId();
        }
    }

    /*
     Constructor -2
     각 필드 주입을 통한 Dto 형성
     */
    public RuleJoinDto(Long moimId, int birthMax, int birthMin, MemberGender gender, int moimMaxCount, boolean dupLeaderAvailable, boolean dupManagerAvailable
            , LocalDateTime createdAt, Long createdMemberId , LocalDateTime updatedAt, Long updatedMemberId) {

        this.moimId = moimId;
        this.birthMax = birthMax;
        this.birthMin = birthMin;
        this.gender = gender;
        this.moimMaxCount = moimMaxCount;
        this.dupLeaderAvailable = dupLeaderAvailable;
        this.dupManagerAvailable = dupManagerAvailable;
        this.createdAt = createdAt;
        this.createdMemberId = createdMemberId;
        this.updatedAt = updatedAt;
        this.updatedMemberId = updatedMemberId;
    }

    /*
     Request Model Test 를 위한 Constructor, 인앱 사용금지
     RuleJoin 생성용
     */
    public RuleJoinDto(int birthMax, int birthMin, MemberGender gender, int moimMaxCount, boolean dupLeaderAvailable, boolean dupManagerAvailable) {

        this.birthMax = birthMax;
        this.birthMin = birthMin;
        this.gender = gender;
        this.moimMaxCount = moimMaxCount;
        this.dupLeaderAvailable = dupLeaderAvailable;
        this.dupManagerAvailable = dupManagerAvailable;

    }

}