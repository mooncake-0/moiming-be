package com.peoplein.moiming.model.dto.domain;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.peoplein.moiming.domain.enums.MemberGender;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberInfoDto {

    private String memberEmail;
    private String memberName;
    //    private String memberPhone;
    private MemberGender memberGender;
//    private LocalDate memberBirth;
//    private String memberPfImg;
//    private String memberBank;
//    private String memberBankNumber;

    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;

}
