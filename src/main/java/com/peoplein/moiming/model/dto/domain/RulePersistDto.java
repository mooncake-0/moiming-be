package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.DomainChecker;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.rules.RulePersist;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RulePersistDto {

    private Long moimId;
    private boolean doGreeting;
    private int attendMonthly;
    private int attendCount;

    private LocalDateTime createdAt;
    private String createdUid;
    private LocalDateTime updatedAt;
    private String updatedUid;


    /*
     Constructor -1
     RulePersist Entity 를 통한 Dto 형성
     */
    public RulePersistDto(RulePersist rulePersist) {
        if (!Objects.isNull(rulePersist)) {
            this.moimId = rulePersist.getMoim().getId();
            this.doGreeting = rulePersist.isDoGreeting();
            this.attendMonthly = rulePersist.getAttendMonthly();
            this.attendCount = rulePersist.getAttendCount();
            this.createdAt = rulePersist.getCreatedAt();
            this.createdUid = rulePersist.getCreatedUid();
            this.updatedAt = rulePersist.getUpdatedAt();
            this.updatedUid = rulePersist.getUpdatedUid();
        }
    }

    /*
     Constructor -2
     각 필드 주입을 통한 Dto 형성
     */
    public RulePersistDto(Long moimId, boolean doGreeting, int attendMonthly, int attendCount
            , LocalDateTime createdAt, String createdUid, LocalDateTime updatedAt, String updatedUid) {
        DomainChecker.checkWrongObjectParams(this.getClass().getName(), moimId);
        this.moimId = moimId;
        this.doGreeting = doGreeting;
        this.attendMonthly = attendMonthly;
        this.attendCount = attendCount;
        this.createdAt = createdAt;
        this.createdUid = createdUid;
        this.updatedAt = updatedAt;
        this.updatedUid = updatedUid;
    }


    /*
     Request Model Test 를 위한 Constructor, 인앱 사용금지
     RulePersist 생성용
     */
    public RulePersistDto(boolean doGreeting, int attendMonthly, int attendCount) {

        this.doGreeting = doGreeting;
        this.attendMonthly = attendMonthly;
        this.attendCount = attendCount;

    }

}
