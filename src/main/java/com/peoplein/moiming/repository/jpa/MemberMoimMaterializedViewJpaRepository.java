package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.MemberMoimMaterializedView;
import com.peoplein.moiming.repository.MemberMoimMaterializedViewRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.peoplein.moiming.domain.QMemberMoimMaterializedView.memberMoimMaterializedView;

@Repository
@RequiredArgsConstructor
public class MemberMoimMaterializedViewJpaRepository implements MemberMoimMaterializedViewRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;


    @Override
    public Long save(MemberMoimMaterializedView memberMoimMaterializedView) {
        em.persist(memberMoimMaterializedView);
        return memberMoimMaterializedView.getId();
    }

    @Override
    public List<MemberMoimMaterializedView> findByMoimIds(List<Long> moimIds) {
        return query.selectFrom(memberMoimMaterializedView)
                .where(memberMoimMaterializedView.moimId.in(moimIds))
                .fetch();
    }

    @Override
    public List<MemberMoimMaterializedView> findAll() {
        return query.selectFrom(memberMoimMaterializedView).fetch();
    }

    @Override
    public void deleteMViews(List<Long> moimIds) {
        query.delete(memberMoimMaterializedView)
                .where(memberMoimMaterializedView.moimId.in(moimIds))
                .execute();
    }
}
