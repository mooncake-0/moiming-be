package com.peoplein.moiming.model.dto.request_b;

import com.peoplein.moiming.model.dto.domain.RuleJoinDto;
import com.peoplein.moiming.model.dto.domain.RulePersistDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RuleRequestDto {

    private Long moimId;
    private RuleJoinDto ruleJoinDto;
    private RulePersistDto rulePersistDto;

}
