package com.peoplein.moiming.model.dto.domain;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MoimMembersDto {
    private MyMoimLinkerDto myMoimLinkerDto;
    private List<MoimMemberInfoDto> moimMemberInfoDto = new ArrayList<>();

}