package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;

import static com.peoplein.moiming.domain.QMoimPost.*;
import static com.peoplein.moiming.domain.QMember.*;
import static com.peoplein.moiming.domain.QMemberInfo.*;
import static com.peoplein.moiming.domain.QMoim.*;

@Repository
@RequiredArgsConstructor
public class MoimPostJpaRepository implements MoimPostRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;

    @Override
    public Long save(MoimPost moimPost) {
        em.persist(moimPost);
        return moimPost.getId();
    }

    @Override
    public MoimPost findById(Long moimPostId) {
        /*
         Query : select * from moim_post mp where mp.moim_post_id = {moimPostId}
        */

        return queryFactory.selectFrom(moimPost)
                .where(moimPost.id.eq(moimPostId))
                .fetchOne();
    }

    @Override
    public MoimPost findWithMemberById(Long moimPostId) {


        return queryFactory.selectFrom(moimPost)
                .join(moimPost.member, member).fetchJoin()
                .where(moimPost.id.eq(moimPostId))
                .fetchOne();
    }

    @Override
    public MoimPost findWithMemberId(Long moimPostId, Long memberId) {
        return queryFactory
                .selectFrom(moimPost)
                .where(moimPost.id.eq(moimPostId).and(moimPost.member.id.eq(memberId)))
                .fetchOne();
    }

    @Override
    public MoimPost findWithMoimAndMemberById(Long moimPostId) {
        /*
         JPQL Query : select mp from MoimPost mp
                        join fetch mp.member m
                        join fetch mp.moim m
                        where mp.id = :moimPostId
        */

        return queryFactory.selectFrom(moimPost)
                .join(moimPost.member, member).fetchJoin()
                .join(moimPost.moim, moim).fetchJoin()
                .where(moimPost.id.eq(moimPostId))
                .fetchOne();
    }

    @Override
    public MoimPost findWithMoimAndMemberInfoById(Long moimPostId) {
        /*
         JPQL Query : select mp from MoimPost mp
                        join fetch mp.moim m
                        join fetch mp.member mem
                        join fetch mem.memberInfo mi
                        where mp.id = :moimPostId
         */

        return queryFactory.selectFrom(moimPost)
                .join(moimPost.member, member).fetchJoin()
                .join(moimPost.moim, moim).fetchJoin()
                .join(member.memberInfo, memberInfo).fetchJoin()
                .where(moimPost.id.eq(moimPostId))
                .fetchOne();
    }

    @Override
    public List<MoimPost> findByMoimId(Long moimId) {
        return queryFactory.selectFrom(moimPost)
                .where(moimPost.moim.id.eq(moimId))
                .fetch();
    }

    @Override
    public List<MoimPost> findWithMemberInfoByMoimId(Long moimId) {
        // Post 의 멤버와, 그 멤버와의 정보까지 join 필요
        /*
         JPQL Query : select mp from MoimPost mp
                        join fetch mp.member m
                        join fetch m.memberInfo mi
                        where mp.moim.id = :moimId
         */

        return queryFactory.selectFrom(moimPost)
                .join(moimPost.member, member).fetchJoin()
                .join(member.memberInfo, memberInfo).fetchJoin()
                .where(moimPost.moim.id.eq(moimId))
                .fetch();
    }

    @Override
    public void removeAll(List<Long> moimPostIds) {

        queryFactory.delete(moimPost).where(moimPost.id.in(moimPostIds)).execute();
    }

    @Override
    public void remove(MoimPost moimPost) {
        em.remove(moimPost);
    }

}
