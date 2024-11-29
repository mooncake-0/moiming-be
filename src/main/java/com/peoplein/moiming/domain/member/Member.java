package com.peoplein.moiming.domain.member;


import com.peoplein.moiming.domain.BaseEntity;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.file.File;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.peoplein.moiming.exception.ExceptionValue.*;


@Slf4j
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_phone"}, name = "unique_member_phone"),
        @UniqueConstraint(columnNames = {"nickname"}, name = "unique_nickname"),
        @UniqueConstraint(columnNames = {"ci"}, name = "unique_ci")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "member_phone", nullable = false)
    private String memberPhone;

    @Column(nullable = false)
    private String password;

    private String nickname;

    private String ci;

    private String refreshToken;

    private String fcmToken;

    private boolean hasDeleted;

    private Long pfImgFileId;

    private String pfImgUrl;

    private LocalDateTime lastLoginAt;

    /*
     Mapped Columns
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_info_id")
    private MemberInfo memberInfo;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberRole> roles = new ArrayList<>();

    /*
     생성자는 Private 으로, 생성 방식을 create 함수로만 제어한다
     */
    private Member(String memberPhone, String password, String fcmToken, String ci) {
        this.memberPhone = memberPhone;
        this.password = password;
        this.fcmToken = fcmToken;
        this.ci = null;

        this.hasDeleted = false;
        this.lastLoginAt = LocalDateTime.now();
    }


    // Password should be always encrypted.
    public static Member createMember(String memberPhone, String encryptedPassword, String fcmToken, String ci, Role role) {

        Member createdMember = new Member(memberPhone, encryptedPassword, fcmToken, ci);
        MemberRole.grantRoleToMember(createdMember, role);

        return createdMember;
    }


    /*
     2407 Update
     - 쿼리 없이 유저 인증을 진행해주기 위해서 가짜 인증 객체를 생성하기 위한 Member
     - 사실 가짜는 아니고, token 만으로 Spring 의 Authentication 을 허용해주기 위한 장치
     - 사용하는 주체는 오직 Token Provider
     - 인 앱에서 가객체로 활용하는게 사실상 ID 뿐!!!
     */
    private Member(Long id) {
        this.id = id;
    }

    public static Member createTokenAuthenticationMember(Long id) {
        return new Member(id);
    }



    /*
     연관관계 편의 메소드
     */
    public void addRole(MemberRole roleLinker) {
        this.roles.add(roleLinker);
    }

    public void saveMemberInfo(MemberInfo memberInfo) {
        this.memberInfo = memberInfo;
    }


    // MEMO : 수정 메소드 영속화 확인 필수
    public void changeRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void changeFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void changeLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void changePfImg(File file) {
        this.pfImgFileId = file.getId();
        this.pfImgUrl = file.getFileUrl();
    }


    public void deletePfImg() {
        this.pfImgFileId = null;
        this.pfImgUrl = null;
    }


    public void changePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("잘못된 입력");
        }

        if (this.password.equals(password)) { // 현재 비밀번호와 같으면 안됨
            throw new MoimingApiException(MEMBER_NOT_FOUND); // TODO:: 예외 Refactor
        }
        this.password = password;
    }


    public void changeNickname(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            throw new IllegalArgumentException("잘못된 입력");
        }
        this.nickname = nickname;
    }

    public void isMemberInfoRegistered() {
        if (this.memberInfo == null) {
            throw new MoimingApiException(MEMBER_INFO_NOT_REGISTERED);
        }
    }

    // v0.1 - Email DEPRECATED
//    public String getMaskedEmail() {
//
//        String memberEmail = "api_deprecated@notavailable.com";
//        String[] emailSplit = memberEmail.split("@", 2);
//        String front = emailSplit[0];
//
//        if (front.length() == 1) {
//            front = "*";
//        } else if (front.length() == 2) {
//            front = "**";
//        } else if (front.length() == 3) {
//            front = front.charAt(0) + "**";
//        } else if (front.length() > 3) {
//            front = front.substring(0, front.length() - 3) + "***";
//        } else {
//            log.error("{}, getMaskedEmail :: {}, {}", this.getClass().getName(), "적절하지 못한 이메일 길이 < 1", front);
//            throw new MoimingApiException(COMMON_INVALID_SITUATION);
//        }
//
//        return front + "@" + emailSplit[1];
//    }

}