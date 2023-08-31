package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MoimCategoryLinker;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.response.MoimRespDto;
import com.peoplein.moiming.repository.CategoryRepository;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.MoimCreateReqDto.*;
import static com.peoplein.moiming.model.dto.response.MoimRespDto.*;

@Service
@Transactional
@RequiredArgsConstructor
public class MoimService {

    private final MoimRepository moimRepository;
    private final MoimMemberRepository moimMemberRepository;
    private final CategoryRepository categoryRepository;

    // 모임 생성
    public Moim createMoim(MoimCreateReqDto requestDto, Member curMember) {

        // 생성 자격에 대해서 논할 필요는 없음
        // 1. 카테고리를 준비한다 (같이 생성 필요)
        // Category 가 완성된걸 추가만 한다
        List<Category> categories = generateCategoryList(requestDto.getCategoryNameValues());

        // 2.생성 Trial
        Moim moim = Moim.createMoim(requestDto.getMoimName(), requestDto.getMoimInfo(), requestDto.getMaxMember()
                , new Area(requestDto.getAreaState(), requestDto.getAreaCity()), categories, curMember);


        // 가입조건 있을시 SU
        if (requestDto.hasJoinRule()) {
            moim.setMoimJoinRule(generateJoinRule(requestDto.getJoinRuleDto()));
        }

        // 카테고리 SU
        moimRepository.save(moim);

        return moim;
    }


    // 모임 일반 조회
    public List<MoimViewRespDto> getMemberMoims(Member curMember) {

        List<MoimMember> moimMembers = moimMemberRepository.findWithMoimAndCategoryByMemberId(curMember.getId());
        return moimMembers.stream().map(MoimViewRespDto::new).collect(Collectors.toList());
    }

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
        List<Category> categories = categoryRepository.findByCategoryNames(queryList);

        // Category 검증
        Category parentCategory = null;
        Category childCategory = null;

        for (Category category : categories) {
            if (category.getCategoryDepth() == 1) {
                parentCategory = category;
            } else {
                childCategory = category;
            }
        }

        // 검증 -> 1. 둘 중 하나라도 없으면 Exception   2. 둘이 종속관계가 아니면 Exception
        if (Objects.isNull(parentCategory) || Objects.isNull(childCategory) || !Objects.equals(childCategory.getParent().getId(), parentCategory.getId())) {
            throw new MoimingApiException("전달받은 카테고리들이 잘못된 관계에 있습니다");
        }

        return categories;
    }
}