//package com.peoplein.moiming.domain.fixed;
//
//
//import com.peoplein.moiming.domain.enums.SessionCategoryType;
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import javax.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Getter
//@Table(name = "session_category")
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class SessionCategory {
//
//    @Id
//    @Column(name = "session_category_id")
//    @GeneratedValue(strategy = GenerationType.SEQUENCE)
//    private Long id;
//
//    @Enumerated(value = EnumType.STRING)
//    private SessionCategoryType categoryType;
//
//    private boolean isUsing;
//
//    private LocalDateTime createdAt;
//
//    private LocalDateTime updatedAt;
//
//    public static SessionCategory createSessionCategory(SessionCategoryType categoryType) {
//        SessionCategory sessionCategory = new SessionCategory(categoryType);
//        return sessionCategory;
//    }
//
//    private SessionCategory(SessionCategoryType categoryType) {
//
//        this.categoryType = categoryType;
//
//        // 초기화
//        this.isUsing = true;
//        this.createdAt = LocalDateTime.now();
//
//    }
//
//}