package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.DomainChecker;
import com.peoplein.moiming.domain.MemberMoimLinker;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MoimMembersDto {
    private MyMoimLinkerDto myMoimLinkerDto;
    private List<MoimMemberInfoDto> moimMemberInfoDto = new ArrayList<>();

}
