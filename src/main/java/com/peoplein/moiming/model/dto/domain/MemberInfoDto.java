package com.peoplein.moiming.model.dto.domain;


import com.peoplein.moiming.domain.member.MemberInfo;
import com.peoplein.moiming.domain.enums.MemberGender;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberInfoDto {

    private String memberName;
    private String memberPhone;
    private MemberGender memberGender;
    private LocalDate memberBirth;
    private boolean isDormant;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MemberInfoDto(MemberInfo memberInfo) {
        this.memberName = memberInfo.getMemberName();
        this.memberPhone = memberInfo.getMemberPhone();
        this.memberGender = memberInfo.getMemberGender();
        this.memberBirth = memberInfo.getMemberBirth();
        this.isDormant = memberInfo.isDormant();
        this.createdAt = memberInfo.getCreatedAt();
        this.updatedAt = memberInfo.getUpdatedAt();
    }

}