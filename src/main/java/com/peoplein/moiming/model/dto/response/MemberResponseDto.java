package com.peoplein.moiming.model.dto.response;


import com.peoplein.moiming.model.dto.domain.MemberDto;
import com.peoplein.moiming.model.dto.domain.MemberInfoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDto {

    private MemberDto memberDto;
    private MemberInfoDto memberInfoDto;

}
