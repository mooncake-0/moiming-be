package com.peoplein.moiming.service.input;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.model.dto.domain.MoimDto;
import com.peoplein.moiming.model.dto.domain.RuleJoinDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Builder
@Getter
public class MoimServiceInput {

    private MoimDto inputMoimDto;
    private Moim findMoim;

    private Member curMember;
    private MoimRoleType updatePermission;
    private List<Category> moimCategoriesForCreate;

    private RuleJoinDto requestRuleJoinDto;







}
