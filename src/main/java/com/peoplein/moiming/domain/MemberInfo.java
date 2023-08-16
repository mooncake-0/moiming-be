package com.peoplein.moiming.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.security.exception.AuthErrorEnum;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_phone"}, name = "unique_member_phone")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberInfo {

    @Id
    @Column(name = "member_info_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String memberName;

    @Column(name = "member_phone")
    private String memberPhone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberGender memberGender;

    private LocalDate memberBirth;

    private String memberBank;

    private String memberBankNumber;

    private boolean isDormant;

    private boolean isForeigner;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "memberInfo", fetch = FetchType.LAZY)
    private Member member;


    // 초기 Input 값을 통해 생성
    public MemberInfo(String memberName, String memberPhone, MemberGender memberGender, boolean isForeigner, LocalDate memberBirth) {

        this.memberName = memberName;
        this.memberPhone = memberPhone;
        this.memberGender = memberGender;
        this.memberBirth = memberBirth;
        this.isForeigner = isForeigner;

        // 초기화
        this.createdAt = LocalDateTime.now();
        this.isDormant = false;
    }


}