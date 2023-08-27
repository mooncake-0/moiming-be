package com.peoplein.moiming.repository.jpa.query;

import com.peoplein.moiming.model.dto.domain.CategoryDto;
import com.peoplein.moiming.model.query.QueryJoinedMoimBasic;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import com.peoplein.moiming.model.dto.domain.RuleJoinDto;
import com.peoplein.moiming.model.dto.domain.RulePersistDto;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.peoplein.moiming.domain.QMoim.*;
import static com.peoplein.moiming.domain.fixed.QCategory.*;
import static com.peoplein.moiming.domain.QMoimCategoryLinker.*;
import static com.peoplein.moiming.domain.rules.QMoimRule.*;
import static com.peoplein.moiming.domain.rules.QRuleJoin.*;
import static com.peoplein.moiming.domain.rules.QRulePersist.*;
import static com.peoplein.moiming.domain.QMemberMoimLinker.*;

@Repository
@RequiredArgsConstructor
public class MoimJpaQueryRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public List<QueryJoinedMoimBasic> findQueryMoimBasicByMemberId(Long memberId) {
        return queryFactory.select(
                        Projections.constructor(QueryJoinedMoimBasic.class,
                                moim.id, moim.moimName, moim.moimInfo, moim.hasRuleJoin, moim.hasRulePersist, moim.curMemberCount
                                , moim.moimArea, moim.createdAt, moim.createdMemberId, moim.updatedAt, moim.updatedMemberId
                                , memberMoimLinker.moimRoleType, memberMoimLinker.memberState, memberMoimLinker.createdAt, memberMoimLinker.updatedAt
                        )
                ).distinct().from(moim)
                .join(memberMoimLinker).on(moim.id.eq(memberMoimLinker.moim.id))
                .where(memberMoimLinker.member.id.eq(memberId))
                .fetch()
                ;
    }

    public List<QueryJoinedMoimBasic> findQueryMoimBasicAndFetchCollections(Long memberId) {

        List<QueryJoinedMoimBasic> anw = findQueryMoimBasicByMemberId(memberId);

        List<Long> moimIds = anw.stream()
                .map(QueryJoinedMoimBasic::getMoimId).collect(Collectors.toList());

        /*
         curMember 의 모든 모임 Id 를 가지고 있는 모든 Category 를 Dto List 로 반환
         */
        List<CategoryDto> categoriesDtoList
                = queryFactory.select(
                        Projections.constructor(CategoryDto.class, moimCategoryLinker.moim.id, category.categoryDepth, category.categoryName)
                ).from(moimCategoryLinker)
                .join(category).on(moimCategoryLinker.category.id.eq(category.id))
                .where(moimCategoryLinker.moim.id.in(moimIds))
                .fetch();

        Map<Long, List<CategoryDto>> categoryTempMap = categoriesDtoList.stream().collect(Collectors.groupingBy(CategoryDto::getMoimId));

        List<RuleJoinDto> foundRuleJoinDto
                = queryFactory.select(
                        Projections.constructor(RuleJoinDto.class, moim.id, ruleJoin.birthMax, ruleJoin.birthMin, ruleJoin.gender, ruleJoin.moimMaxCount
                                , ruleJoin.dupLeaderAvailable, ruleJoin.dupManagerAvailable, ruleJoin.createdAt, ruleJoin.createdMemberId, ruleJoin.updatedAt, ruleJoin.updatedMemberId)
                ).from(ruleJoin)
                .join(moim).on(ruleJoin.moim.id.eq(moim.id))
                .where(ruleJoin.moim.id.in(moimIds))
                .fetch();

        // MEMO :: 해당 Id 에 속한 Rule* 을 모두 Collect 하지만, 모임당 Rule* 은 한개이므로, 존재한다면 List 내부에는 무조건 1개의 객체만 저장되어 있다
        Map<Long, List<RuleJoinDto>> ruleJoinTempMap = foundRuleJoinDto.stream().collect(Collectors.groupingBy(RuleJoinDto::getMoimId));

        List<RulePersistDto> foundRulePersistDto
                = queryFactory.select(
                        Projections.constructor(RulePersistDto.class, moim.id, rulePersist.doGreeting, rulePersist.attendMonthly, rulePersist.attendCount
                                , rulePersist.createdAt, rulePersist.createdMemberId, rulePersist.updatedAt, rulePersist.updatedMemberId)
                ).from(rulePersist)
                .join(moim).on(rulePersist.moim.id.eq(moim.id))
                .where(rulePersist.moim.id.in(moimIds))
                .fetch();

        Map<Long, List<RulePersistDto>> rulePersistTempMap = foundRulePersistDto.stream().collect(Collectors.groupingBy(RulePersistDto::getMoimId));

        for (QueryJoinedMoimBasic queryJoinedMoimBasic : anw) {
            // Category 는 필수 정보로 모든 moim 이 가지고 있음
            queryJoinedMoimBasic.setCategoriesDto(categoryTempMap.get(queryJoinedMoimBasic.getMoimId()));

            // 각 Rule 은 존재 확인시 첫번째 Rule 로 세팅
            if (queryJoinedMoimBasic.isHasRuleJoin()) {
                queryJoinedMoimBasic.setRuleJoinDto(ruleJoinTempMap.get(queryJoinedMoimBasic.getMoimId()).get(0));
            }

            if (queryJoinedMoimBasic.isHasRulePersist()) {
                queryJoinedMoimBasic.setRulePersistDto(rulePersistTempMap.get(queryJoinedMoimBasic.getMoimId()).get(0));
            }
        }

        return anw;
    }

}