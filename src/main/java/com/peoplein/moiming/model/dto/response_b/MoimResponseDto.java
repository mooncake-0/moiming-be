package com.peoplein.moiming.model.dto.response_b;

import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.rules.RuleJoin;
import com.peoplein.moiming.domain.rules.RulePersist;
import com.peoplein.moiming.model.dto.domain.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimResponseDto {

    private MoimDto moimDto;
    private RuleJoinDto ruleJoinDto;
    private RulePersistDto rulePersistDto;
    private MoimMembersDto moimMembersDto;
    private List<CategoryDto> categoriesDto = new ArrayList<>();

    /*
     Constructor -1
     Entity 들을 통해 Dto 가 생성될 수 있도록 각 도메인에 전달
     잘못된 input 은 각 Dto 에서 담당한다
     */
    public MoimResponseDto(Moim moim, RuleJoin ruleJoin, RulePersist rulePersist
            , List<Category> categories, MoimMembersDto moimMembersDto) {

        this.moimDto = new MoimDto(moim);
        this.ruleJoinDto = new RuleJoinDto(ruleJoin);
        this.rulePersistDto = new RulePersistDto(rulePersist);
        this.categoriesDto = initCategoriesDto(categories);
        this.moimMembersDto = moimMembersDto;
    }

    /*
     Constructor -2
     각 정보들을 매핑해서 Dto 형성
     */
    public MoimResponseDto(MoimDto moimDto, RuleJoinDto ruleJoinDto, RulePersistDto rulePersistDto, MoimMembersDto moimMembersDto, List<CategoryDto> categoriesDto) {

        this.moimDto = moimDto;
        this.ruleJoinDto = ruleJoinDto;
        this.rulePersistDto = rulePersistDto;
        this.moimMembersDto = moimMembersDto;
        this.categoriesDto = categoriesDto;

    }

    public List<CategoryDto> initCategoriesDto(List<Category> categories) {
        return categories.stream().map(CategoryDto::new).collect(Collectors.toList());
    }

}
