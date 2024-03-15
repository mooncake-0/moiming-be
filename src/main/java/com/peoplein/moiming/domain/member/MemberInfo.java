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
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_phone"}, name = "unique_member_phone")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberInfo extends BaseEntity {

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

    private boolean dormant;

    private Long pfImgFileId;

    private String pfImgUrl;

    @OneToOne(mappedBy = "memberInfo", fetch = FetchType.LAZY)
    private Member member;


    // 초기 Input 값을 통해 생성
    public MemberInfo(String memberName, String memberPhone, MemberGender memberGender, LocalDate memberBirth) {

        this.memberName = memberName;
        this.memberPhone = memberPhone;
        this.memberGender = memberGender;
        this.memberBirth = memberBirth;

        // 초기화
        this.dormant = false;
    }


    void changeDormant(boolean dormant) {
        this.dormant = dormant;
    }

    public void changePfImg(File file) {
        this.pfImgFileId = file.getId();
        this.pfImgUrl = file.getFileUrl();
    }

    public void deletePfImg() {
        this.pfImgFileId = null;
        this.pfImgUrl = null;
    }

}