package com.peoplein.moiming.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.exception.BadAuthParameterInputException;
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
        @UniqueConstraint(columnNames = {"member_phone"}, name = "unique_member_phone"),
        @UniqueConstraint(columnNames = {"member_email"}, name = "unqiue_member_email")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberInfo {

    @Id
    @Column(name = "member_info_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "member_email", nullable = false)
    private String memberEmail;

    @Column(nullable = false)
    private String memberName;

    @Column(name = "member_phone")
    private String memberPhone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberGender memberGender;

    private LocalDate memberBirth;

    private String memberPfImg;

    private String memberBank;

    private String memberBankNumber;

    private boolean isDormant;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "memberInfo", fetch = FetchType.LAZY)
    private Member member;

    // 초기 Input 값을 통해 생성
    public MemberInfo(String memberEmail, String memberName, MemberGender memberGender) {

        DomainChecker.checkRightString("Member Info Entity", true, memberEmail, memberName);

        if (Objects.isNull(memberGender)) {
            throw new IllegalArgumentException("temp");
        }
        this.memberName = memberName;
        this.memberEmail = memberEmail;
        this.memberGender = memberGender;

        this.createdAt = LocalDateTime.now();
    }

    public void setMemberBirth(LocalDate memberBirth) {
        this.memberBirth = memberBirth;
    }
}
