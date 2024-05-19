package com.peoplein.moiming.domain.member;

import com.peoplein.moiming.domain.BaseEntity;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.file.File;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberInfo extends BaseEntity {

    // 식별관계를 위해 직접 memberId 받아온다 // GeneratedValue 를 사용하지 않음
    @Id
    @Column(name = "member_info_id")
    private Long id;

    @Column(nullable = false)
    private String memberName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberGender memberGender;

    @Column(nullable = false)
    private LocalDate memberBirth;

    private Long pfImgFileId;

    private String pfImgUrl;


    public static MemberInfo saveMemberInfo(Member member, String memberName, MemberGender memberGender, LocalDate memberBirth) {
        return new MemberInfo(member, memberName, memberGender, memberBirth);
    }


    private MemberInfo(Member member, String memberName, MemberGender memberGender, LocalDate memberBirth) {
        this.id = member.getId();
        this.memberName = "";
        this.memberGender = memberGender;
        this.memberBirth = memberBirth;

        // 연관관계 편의
        member.saveMemberInfo(this);
    }


    public void changePfImg(File file) {
        this.pfImgFileId = file.getId();
        this.pfImgUrl = file.getFileUrl();
    }


    public void deletePfImg() {
        this.pfImgFileId = null;
        this.pfImgUrl = null;
    }


    // MemberInfo 정보긴 하지만, Member 도메인이 훨씬 Active 하기 떄문에
    // 앱 내에서 [나이] 란, 이전 한국 나이를 말한다
    public int getMemberAge() {
        int birthYear = this.getMemberBirth().getYear();
        int todayYear = LocalDate.now().getYear();
        return todayYear - birthYear + 1;
    }

}