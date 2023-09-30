package com.peoplein.moiming.model.dto.request_b;

import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.model.dto.domain.MoimDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MoimRequestDto {

    private MoimDto moimDto;
//    private RuleJoinDto ruleJoinDto;
    private List<CategoryName> categoryNames = new ArrayList<>();

}
