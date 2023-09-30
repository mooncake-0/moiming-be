//package com.peoplein.moiming.repository.jpa;
//
//import com.peoplein.moiming.domain.rules.MoimRule;
//
//
//import com.peoplein.moiming.repository.MoimRuleRepository;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Repository;
//
//import javax.persistence.EntityManager;
//import java.util.List;
//
//import static com.peoplein.moiming.domain.rules.QMoimRule.*;
//
//@Repository
//@RequiredArgsConstructor
//public class MoimRuleJpaRepository implements MoimRuleRepository {
//
//    private final JPAQueryFactory queryFactory;
//    private final EntityManager em;
//
//
//    @Override
//    public Long save(MoimRule moimRule) {
//        em.persist(moimRule);
//        return moimRule.getId();
//    }
//
//    @Override
//    public List<MoimRule> findByMoim(Long moimId) {
//        /*
//         Query : select * from moim_rule where moim_rule.moim_id = {moim.moim_id}
//         JPQL : select mr from MoimRule mr
//                where mr.moim = :{moim}
//         */
//
//        return queryFactory.selectFrom(moimRule)
//                .where(moimRule.moim.id.eq(moimId))
//                .fetch();
//    }
//
//    @Override
//    public MoimRule findByMoimAndType(Long moimId, String ruleType) {
//
//        /*
//         Query : select * from moim_rule mr where mr.moim_id = {moim.id}
//                        and mr.rule_type = {type}
//         */
//
//        return queryFactory.selectFrom(moimRule)
//                .where(moimRule.moim.id.eq(moimId)
//                        , moimRule.ruleType.eq(ruleType))
//                .fetchOne();
//    }
//
//    @Override
//    public void remove(MoimRule moimRule) {
//        em.remove(moimRule);
//    }
//}
