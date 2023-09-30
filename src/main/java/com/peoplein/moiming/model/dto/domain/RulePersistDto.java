//package com.peoplein.moiming.model.dto.domain;
//
//import com.peoplein.moiming.domain.rules.RulePersist;
//import lombok.AccessLevel;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//import java.util.Objects;
//
//@Data
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class RulePersistDto {
//
//    private Long moimId;
//    private boolean doGreeting;
//    private int attendMonthly;
//    private int attendCount;
//
//    private LocalDateTime createdAt;
//    private Long createdMemberId;
//    private LocalDateTime updatedAt;
//    private Long updatedMemberId;
//
//
//    /*
//     Constructor -1
//     RulePersist Entity 를 통한 Dto 형성
//     */
//    public RulePersistDto(RulePersist rulePersist) {
//        if (!Objects.isNull(rulePersist)) {
//            this.moimId = rulePersist.getMoim().getId();
//            this.doGreeting = rulePersist.isDoGreeting();
//            this.attendMonthly = rulePersist.getAttendMonthly();
//            this.attendCount = rulePersist.getAttendCount();
//            this.createdAt = rulePersist.getCreatedAt();
//            this.createdMemberId = rulePersist.getCreatedMemberId();
//            this.updatedAt = rulePersist.getUpdatedAt();
//            this.updatedMemberId = rulePersist.getUpdatedMemberId();
//        }
//    }
//
//    /*
//     Constructor -2
//     각 필드 주입을 통한 Dto 형성
//     */
//    public RulePersistDto(Long moimId, boolean doGreeting, int attendMonthly, int attendCount
//            , LocalDateTime createdAt, Long createdMemberId, LocalDateTime updatedAt, Long updatedMemberId) {
//        this.moimId = moimId;
//        this.doGreeting = doGreeting;
//        this.attendMonthly = attendMonthly;
//        this.attendCount = attendCount;
//        this.createdAt = createdAt;
//        this.createdMemberId = createdMemberId;
//        this.updatedAt = updatedAt;
//        this.updatedMemberId = updatedMemberId;
//    }
//
//
//    /*
//     Request Model Test 를 위한 Constructor, 인앱 사용금지
//     RulePersist 생성용
//     */
//    public RulePersistDto(boolean doGreeting, int attendMonthly, int attendCount) {
//
//        this.doGreeting = doGreeting;
//        this.attendMonthly = attendMonthly;
//        this.attendCount = attendCount;
//
//    }
//
//}