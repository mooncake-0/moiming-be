package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.*;
import com.peoplein.moiming.repository.jpa.MoimJoinRuleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.peoplein.moiming.exception.ExceptionValue.*;
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


    private final MoimCategoryLinkerRepository moimCategoryLinkerRepository;
    private final PostCommentRepository postCommentRepository;
    private final MoimPostRepository moimPostRepository;
    private final MoimJoinRuleJpaRepository moimJoinRuleRepository;


    // 모임 생성
    public Moim createMoim(MoimCreateReqDto requestDto, Member curMember) {

        if (requestDto == null || curMember == null || requestDto.getCategoryNameValues().size() != 2) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

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


    // 컨디션에 따른 해당 멤버의 모임 조회 결과 List<Moim> 으로 반환
    // 사용영역 > Home 화면 및 마이페이지 화면에서 사용될 예정
    public List<MoimMember> getMemberMoims(Long lastMoimId, boolean isActiveReq, boolean isManagerReq, int limit, Member curMember) {

        if (curMember == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        Moim lastMoim = null;
        if (lastMoimId != null) {
            lastMoim = moimRepository.findById(lastMoimId).orElse(null); // NULLABLE
        }

        return moimMemberRepository.findMemberMoimsWithRuleAndCategoriesByConditionsPaged(curMember.getId(), isActiveReq, isManagerReq, lastMoim, limit);

    }


    // 모임 세부 조회 - 정보 , 가입조건, 지역 및 카테고리 ALL -- 누구나 요청 가능
    public Moim getMoimDetail(Long moimId, Member member) {

        if (moimId == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        Moim moim = moimRepository.findWithJoinRuleAndCategoryById(moimId).orElseThrow(() ->
                new MoimingApiException(MOIM_NOT_FOUND)
        );

        return moim;
    }


    // 모임 수정하기
    public Moim updateMoim(MoimUpdateReqDto requestDto, Member curMember) {

        // 차피 Moim 존속 여부랑은 별개로, MoimMember 가 있으면 되는 것이니, MoimMember 로 그냥 join 해와버리자
        MoimMember moimMemberPs = moimMemberRepository.findByMemberAndMoimId(curMember.getId(), requestDto.getMoimId()).orElseThrow(() ->
                new MoimingApiException(MOIM_NOT_FOUND)
        );

        if (!moimMemberPs.hasPermissionOfManager()) {
            throw new MoimingApiException(MOIM_MEMBER_NOT_AUTHORIZED);
        }

        List<Category> requestCategories = categoryService.generateCategoryList(requestDto.getCategoryNameValues());
        moimMemberPs.getMoim().updateMoim(requestDto, requestCategories, curMember.getId()); // Moim 도 영속화됨

        return moimMemberPs.getMoim();

    }


    // TODO :: MVP 에선 삭제한다. 실제 데이터를
    //         원래 데이터 삭제는 드문 일임을 알아두자
    //         MoimPost 를 제외하고는 모두 조회는 필요 없음
    // 모임 삭제 (MANAGER 권한)
    public void deleteMoim(Long moimId, Member member) {

        if (moimId == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        Moim moim = moimRepository.findWithJoinRuleAndCategoryById(moimId).orElseThrow(() ->
                new MoimingApiException(MOIM_NOT_FOUND)
        );

        List<MoimPost> moimPosts = moimPostRepository.findByMoimId(moim.getId());

        // MEMO :: JPQL 을 발생시키는건 Flush 를 진행하게 된다 - 영컨을 이용하면 순서가 보장되지 않음, FK 오류 가능, OrphanRemoval, Cascade.REMOVE 옵션은 사용하지 않도록 하자
        // 1) 모든 Post Comment 삭제 - Moim Post 를 Iterate 하면서 진행
        //    IN 절을 써서 한번에 삭제하면 풀스캔을 때려서 일일이 하는 것보도 오래 걸릴 것으로 판단됨
        for (MoimPost moimPost : moimPosts) {
            postCommentRepository.removeAllByMoimPostId(moimPost.getId()); // 모든 댓글들을 실제로 삭제한다
        }

        // 2) 모든 MoimPost 삭제
        moimPostRepository.removeAllByMoimId(moim.getId());

        // 3) 모든 MoimMember 삭제
        moimMemberRepository.removeAllByMoimId(moim.getId());

        // 4) MoimCategoryLinker 삭제
        moimCategoryLinkerRepository.removeAllByMoimId(moim.getId());

        // 5) MoimJoinRule 삭제
        moimJoinRuleRepository.removeById(moim.getMoimJoinRule().getId());

        // 6) Moim 삭제
        moimRepository.remove(moim.getId());

    }


    // Entity 에 DTO 누수하지 않기 위함
    private MoimJoinRule generateJoinRule(JoinRuleCreateReqDto ruleDto) {
        return MoimJoinRule.createMoimJoinRule(ruleDto.getHasAgeRule(), ruleDto.getAgeMax(), ruleDto.getAgeMin(), ruleDto.getMemberGender());
    }

}