package com.peoplein.moiming.model.dto.response_b;


import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.model.dto.domain.MemberDto;
import com.peoplein.moiming.model.dto.domain.MemberInfoDto;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberResponseDto {

    private MemberDto memberDto;
    private MemberInfoDto memberInfoDto;

    public MemberResponseDto(Member member) {

        this.memberDto = new MemberDto(member);
        this.memberInfoDto = new MemberInfoDto(member.getMemberInfo());

    }

}