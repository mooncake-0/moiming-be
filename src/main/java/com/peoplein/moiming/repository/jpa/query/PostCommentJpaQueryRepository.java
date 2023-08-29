package com.peoplein.moiming.repository.jpa.query;

import com.peoplein.moiming.model.dto.domain.MoimMemberInfoDto;
import com.peoplein.moiming.model.query.QueryPostCommentDetails;
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
import static com.peoplein.moiming.domain.QPostComment.*;

@Repository
@RequiredArgsConstructor
public class PostCommentJpaQueryRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public List<QueryPostCommentDetails> findCommentDetailsAndFetchCollections(Long moimPostId) {

        List<QueryPostCommentDetails> queryDetails = findCommentDetailsByMoimPostId(moimPostId);
        List<Long> commentCreatorMemberIds = new ArrayList<>();

        for (QueryPostCommentDetails pcDetail : queryDetails) {
            commentCreatorMemberIds.add(pcDetail.getCommentCreatorId());
        }

        List<MoimMemberInfoDto> dataList =
                queryFactory.select(Projections.constructor(MoimMemberInfoDto.class,
                                member.id, memberInfo.memberName, member.memberEmail, memberInfo.memberGender
                                , moimMember.moimMemberRoleType, moimMember.memberState)
                        ).from(moimMember)
                        .join(moimMember.member, member)
                        .join(moimMember.moim, moim)
                        .join(member.memberInfo, memberInfo)
                        .where(member.id.in(commentCreatorMemberIds))
                        .fetch();

        Map<Long, List<MoimMemberInfoDto>> listMap = dataList.stream().collect(Collectors.groupingBy(MoimMemberInfoDto::getMemberId));
        queryDetails.forEach(queryPostCommentDetails -> queryPostCommentDetails.setCommentCreatorInfoDto(listMap.get(queryPostCommentDetails.getCommentCreatorId()).get(0)));

        return queryDetails;
    }


    public List<QueryPostCommentDetails> findCommentDetailsByMoimPostId(Long moimPostId) {
        return queryFactory.select(
                        Projections.constructor(QueryPostCommentDetails.class
                                , postComment.id, postComment.commentContent, postComment.createdAt, postComment.updatedAt
                                , member.id)
                )
                .from(moimPost)
                .join(moimPost.postComments, postComment)
                .join(postComment.member, member)
                .where(moimPost.id.eq(moimPostId))
                .fetch();
    }
}