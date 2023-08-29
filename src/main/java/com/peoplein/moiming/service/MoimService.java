package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MoimCategoryLinker;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.repository.CategoryRepository;
import com.peoplein.moiming.repository.MoimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.MoimCreateReqDto.*;

@Service
@Transactional
@RequiredArgsConstructor
public class MoimService {

    private final MoimRepository moimRepository;
    private final CategoryRepository categoryRepository;

    // 모임 생성
    public Moim createMoim(MoimCreateReqDto requestDto, Member curMember) {

        // 1.생성 자격에 대해서 논할 필요는 없음
        // 2.생성 Trial
        Moim moim = Moim.createMoim(requestDto.getMoimName(), requestDto.getMoimInfo(), requestDto.getMaxMember()
                , new Area(requestDto.getAreaState(), requestDto.getAreaCity()), curMember);

        // 가입조건 있을시 SU
        if (requestDto.hasJoinRule()) {
            moim.setMoimJoinRule(generateJoinRule(requestDto.getJoinRuleDto()));
        }

        // Category 가 완성된걸 추가만 한다
        List<Category> categories = generateCategoryList(requestDto.getCategoryNameValues());
        for (Category category : categories) {
            MoimCategoryLinker addedCategoryLinker = MoimCategoryLinker.addMoimCategory(moim, category);
        }

        // 카테고리 SU
        moimRepository.save(moim);

        return moim;
    }

    // 모임 일반 조회

    // 모임 세부 조회

    // 모임 수정 (MANAGER 권한)

    // 모임 삭제 (MANAGER 권한)


    // Entity 에 DTO 누수하지 않기 위함
    private MoimJoinRule generateJoinRule(JoinRuleCreateReqDto ruleDto) {
        return MoimJoinRule.createMoimJoinRule(ruleDto.isAgeRule(), ruleDto.getAgeMax(), ruleDto.getAgeMin(), ruleDto.getMemberGender());
    }


    // String Category Value --> Category 로 최종적으로 반환해준다
    private List<Category> generateCategoryList(List<String> categoryNameValues) {
        List<CategoryName> queryList = categoryNameValues.stream().map(CategoryName::fromValue).collect(Collectors.toList());
        return categoryRepository.findByCategoryNames(queryList);
    }

}