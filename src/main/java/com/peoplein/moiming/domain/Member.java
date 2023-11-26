package com.peoplein.moiming.domain;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.fixed.Role;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_email"}, name = "unique_member_email"),
        @UniqueConstraint(columnNames = {"nickname"}, name = "unique_nickname"),
        @UniqueConstraint(columnNames = {"ci"}, name = "unique_ci")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    /*
     Member Columns
     */
    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "member_email", nullable = false)
    private String memberEmail;

    @Column(nullable = false)
    private String password;

    private String nickname;

    private String ci;

    private String refreshToken;

    private String fcmToken;

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
    private Member(String memberEmail, String password, String fcmToken, String ci, MemberInfo memberInfo) {
        this.memberEmail = memberEmail;
        this.password = password;
        this.fcmToken = fcmToken;
        this.ci = ci;
        this.memberInfo = memberInfo;

    }


    // Password should be always encrypted.
    public static Member createMember(String memberEmail,
                                      String encryptedPassword,
                                      String memberName,
                                      String memberPhone,
                                      MemberGender memberGender,
                                      boolean foreigner,
                                      LocalDate memberBirth,
                                      String fcmToken,
                                      String ci,
                                      Role role
    ) {

        MemberInfo memberInfo = new MemberInfo(memberName, memberPhone, memberGender, foreigner, memberBirth);
        Member createdMember = new Member(memberEmail, encryptedPassword, fcmToken, ci, memberInfo);
        MemberRoleLinker.grantRoleToMember(createdMember, role);

        return createdMember;
    }

    /*
     연관관계 편의 메소드
     */
    public void addRole(MemberRoleLinker roleLinker) {
        this.roles.add(roleLinker);
    }


    // MEMO : 수정 메소드 영속화 확인 필수
    // Member 생성 이후 수정 메소드
    // Refresh Token 삭제 시도일 수도 있으니 빈칸 허용
    public void changeRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }


    public void changePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("잘못된 입력");
        }
        this.password = password;
    }


    public void changeNickname(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            throw new IllegalArgumentException("잘못된 입력");
        }
        this.nickname = nickname;
    }


    // MemberInfo 정보긴 하지만, Member 도메인이 훨씬 Active 하기 떄문에
    // 앱 내에서 [나이] 란, 이전 한국 나이를 말한다
    public int getMemberAge() {
        int birthYear = this.memberInfo.getMemberBirth().getYear();
        int todayYear = LocalDate.now().getYear();
        return todayYear - birthYear + 1;
    }


    // WARN: ID 변경은 MOCK 용: 호출된 곳이 test Pckg 인지 확인
    public void changeMockObjectIdForTest(Long mockObjectId, URL classUrl) {

        try {
            URI uri = classUrl.toURI();
            File file = new File(uri);
            String absolutePath = file.getAbsolutePath();

            if (absolutePath.contains("test")) { // 빌드 Class 경로가 test 내부일경우
                this.id = mockObjectId;
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}