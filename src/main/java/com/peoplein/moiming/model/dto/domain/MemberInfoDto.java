package com.peoplein.moiming.model.dto.domain;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.peoplein.moiming.domain.MemberInfo;
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