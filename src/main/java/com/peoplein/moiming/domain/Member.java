package com.peoplein.moiming.domain;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"uid"}, name = "unique_uid")})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    /*
     Member Columns
     */
    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "uid", nullable = false)
    private String uid;

    @Column(nullable = false)
    private String password;

    private String refreshToken;

    private String fcmToken;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime logonAt;

    /*
     Mapped Columns
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_info_id")
    private MemberInfo memberInfo;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberRoleLinker> roles = new ArrayList<>();

    /*
     생성자는 Private 으로, 생성 방식을 create 함수로만 제어한다
     */
    private Member(String uid, String password, MemberInfo memberInfo) {

        DomainChecker.checkRightString("Member Entity", false, uid, password);
        DomainChecker.checkWrongObjectParams("Member Entity", memberInfo);

        this.uid = uid;
        this.password = password;
        this.memberInfo = memberInfo;

        this.createdAt = LocalDateTime.now();
    }

    public static Member createMember(String uid, String password, MemberInfo memberInfo) {
        Member member = new Member(uid, password, memberInfo);
        return member;
    }


    /*
     연관관계 편의 메소드
     */
    public void addRole(MemberRoleLinker roleLinker) {
        this.roles.add(roleLinker);
    }


    // MEMO : 수정 메소드 영속화 확인 필수
    // Member 생성 이후 수정 메소드
    public void changeRefreshToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new IllegalArgumentException("잘못된 입력");
        }
        this.refreshToken = refreshToken;
    }

    public void changePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("잘못된 입력");
        }
        this.password = password;
    }


}
