package com.peoplein.moiming.repository.jpa.query;

import com.peoplein.moiming.model.dto.domain.MoimMemberInfoDto;
import com.peoplein.moiming.model.dto.domain.PostCommentDto;
import com.peoplein.moiming.model.query.QueryMoimPostDetails;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.peoplein.moiming.domain.QMember.*;
import static com.peoplein.moiming.domain.QMemberInfo.*;
import static com.peoplein.moiming.domain.moim.QMoim.*;
import static com.peoplein.moiming.domain.moim.QMoimMember.*;
import static com.peoplein.moiming.domain.QMoimPost.*;


@Repository
@RequiredArgsConstructor
public class MoimPostJpaQueryRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public List<QueryMoimPostDetails> findMoimPostDetailsAndFetchCollections(Long moimId) {

        List<QueryMoimPostDetails> queryDetails = findMoimPostDetailsByMoimId(moimId);

        List<Long> postCreatorIds = new ArrayList<>();

        for (QueryMoimPostDetails queryDetail : queryDetails) {
            postCreatorIds.add(queryDetail.getPostCreatorId());
        }

        List<MoimMemberInfoDto> dataList =
                queryFactory.select(Projections.constructor(MoimMemberInfoDto.class,
                                member.id, memberInfo.memberName, member.memberEmail, memberInfo.memberGender
                                , moimMember.memberRoleType, moimMember.memberState)
                        ).from(moimMember)
                        .join(moimMember.member, member)
                        .join(moimMember.moim, moim)
                        .join(member.memberInfo, memberInfo)
                        .where(member.id.in(postCreatorIds))
                        .fetch();

        Map<Long, List<MoimMemberInfoDto>> listMap = dataList.stream().collect(Collectors.groupingBy(MoimMemberInfoDto::getMemberId));
        queryDetails.forEach(queryMoimPostDetail -> queryMoimPostDetail.setPostCreatorInfoDto(listMap.get(queryMoimPostDetail.getPostCreatorId()).get(0)));

        return queryDetails;
    }

    public List<QueryMoimPostDetails> findMoimPostDetailsByMoimId(Long moimId) {
        return queryFactory.select(
                        Projections.constructor(QueryMoimPostDetails.class
                                , moimPost.id, moimPost.postTitle, moimPost.postContent, moimPost.moimPostCategory, moimPost.isNotice, moimPost.hasFiles
                                , moimPost.createdAt, moimPost.updatedAt, moimPost.updatedMemberId, member.id)
                )
                .from(moimPost)
                .join(moimPost.moim, moim)
                .join(moimPost.member, member)
                .where(moimPost.moim.id.eq(moimId))
                .fetch();
    }

}