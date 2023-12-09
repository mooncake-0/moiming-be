package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.repository.PostFileRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.domain.QMoimPost.*;
import static com.peoplein.moiming.domain.QMember.*;
import static com.peoplein.moiming.domain.QMemberInfo.*;
import static com.peoplein.moiming.domain.moim.QMoim.*;

@Repository
@RequiredArgsConstructor
public class MoimPostJpaRepository implements MoimPostRepository {

    private final EntityManager em;

    private final JPAQueryFactory queryFactory;


    @Override
    public void removeAll(List<Long> moimPostIds) {

        queryFactory.delete(moimPost).where(moimPost.id.in(moimPostIds)).execute();
    }

    @Override
    public void remove(MoimPost moimPost) {
        em.remove(moimPost);
    }



    // IN_USE----------


    @Override
    public void save(MoimPost moimPost) {
        em.persist(moimPost);
    }



    @Override
    public Optional<MoimPost> findById(Long moimPostId) {
        /*
         Query : select * from moim_post mp where mp.moim_post_id = {moimPostId}
        */

        return Optional.ofNullable(queryFactory.selectFrom(moimPost)
                .where(moimPost.id.eq(moimPostId))
                .fetchOne());
    }


    @Override
    public Optional<MoimPost> findWithMoimById(Long moimPostId) {

        return Optional.ofNullable(
                queryFactory.selectFrom(moimPost)
                        .join(moimPost.moim, moim).fetchJoin()
                        .where(moimPost.id.eq(moimPostId))
                        .fetchOne());
    }


    @Override
    public Optional<MoimPost> findWithMoimAndMemberById(Long moimPostId) {

        return Optional.ofNullable(
                queryFactory.selectFrom(moimPost)
                        .join(moimPost.moim, moim).fetchJoin()
                        .join(moimPost.member, member).fetchJoin()
                        .where(moimPost.id.eq(moimPostId))
                        .fetchOne());

    }


    /*
     Query :
      select * from MoimPost mp
      where mp.moimId = :moimId
      and mp.category = :category
      and mp.hasPrivateVisibility = :hasPrivateVisibility

      // 동적 쿼리
      created_at < last_created_at OR created_at = last_created_at AND id < last_id
      ORDER BY created at DESC, id DESC LIMIT 10;

     */

    @Override
    public List<MoimPost> findByCategoryAndLastPostOrderByDateDesc(Long moimId, MoimPost lastPost,
                                                                   MoimPostCategory category, int limit,
                                                                   boolean moimMemberRequest) {

        BooleanBuilder dynamicBuilder = new BooleanBuilder();

        if (category != null) {
            dynamicBuilder.and(moimPost.moimPostCategory.eq(category));
        }

        if (!moimMemberRequest) { // 구성원에게만 공개인 것들은 제외한다 // 구성원의 요청일 경우는 다 보여줘도 됨
            dynamicBuilder.and(moimPost.hasPrivateVisibility.eq(false));
        }

        // lastPost 가 지정이 되어 있을 떄
        // where mp.created_at < cur_created_at // 현재 CREATED_AT 기준으로 정렬되어 있는데, 이 때, 그 이전에 생성된 애들을 요청하는 것이기 때문에 최신 CREATED_AT 을 기준 그 이후로 자를 것이다.
        //       단, 진짜 0.0001 초도 동일할 경우, created_at 이 같을 수 있다. 이 때는 순서대로 생성되었을 것이기 때문에 ID 를 기준으로 한 번 더 진행한다
        //       OR mp.created_at = cur_created_at AND id < last_id // 더 작은게 이전에 생성되었을 것이기 때문
        if (lastPost != null) {
            dynamicBuilder.and(moimPost.createdAt.before(lastPost.getCreatedAt())
                    .or(moimPost.createdAt.eq(lastPost.getCreatedAt()).and(moimPost.id.lt(lastPost.getId()))));
        }

        return queryFactory.selectFrom(moimPost)
                .where(moimPost.moim.id.eq(moimId), dynamicBuilder)
                .orderBy(moimPost.createdAt.desc(), moimPost.id.desc()) // 기본적으로 1차 소팅은 날짜 순, 같을 경우 2차 소팅은 ID로
                .limit(limit)
                .fetch();
    }

}
