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
    private final CategoryService categoryService;


    // 모임 생성
    public Moim createMoim(MoimCreateReqDto requestDto, Member curMember) {

        // 생성 자격에 대해서 논할 필요는 없음
        // 1. 카테고리를 준비한다 (같이 생성 필요)
        // Category 가 완성된걸 추가만 한다
        List<Category> categories = categoryService.generateCategoryList(requestDto.getCategoryNameValues());

        // 2.생성 Trial
        Moim moim = Moim.createMoim(requestDto.getMoimName(), requestDto.getMoimInfo(), requestDto.getMaxMember()
                , new Area(requestDto.getAreaState(), requestDto.getAreaCity()), categories, curMember);


        // 가입조건 있을시 SU
        if (requestDto.getHasJoinRule()) {
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



    // 모임 수정하기
    public Moim updateMoim(MoimUpdateReqDto requestDto, Member curMember) {

        // 차피 Moim 존속 여부랑은 별개로, MoimMember 가 있으면 되는 것이니, MoimMember 로 그냥 join 해와버리자
        MoimMember moimMemberPs = moimMemberRepository.findByMemberAndMoimId(curMember.getId(), requestDto.getMoimId()).orElseThrow(() -> new MoimingApiException("찾을 수 없습니다"));

        if (!moimMemberPs.hasPermissionOfManager()) {
            throw new MoimingApiException("해당 모임에 대한 수정 권한이 없는 회원입니다");
        }

        List<Category> requestCategories = categoryService.generateCategoryList(requestDto.getCategoryNameValues());
        moimMemberPs.getMoim().updateMoim(requestDto, requestCategories, curMember.getId()); // Moim 도 영속화됨

        return moimMemberPs.getMoim();

    }



    // 모임 삭제 (MANAGER 권한)


    // Entity 에 DTO 누수하지 않기 위함
    private MoimJoinRule generateJoinRule(JoinRuleCreateReqDto ruleDto) {
        return MoimJoinRule.createMoimJoinRule(ruleDto.getHasAgeRule(), ruleDto.getAgeMax(), ruleDto.getAgeMin(), ruleDto.getMemberGender());
    }

}