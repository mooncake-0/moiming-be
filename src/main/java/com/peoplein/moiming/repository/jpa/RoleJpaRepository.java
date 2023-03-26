package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.fixed.QRole;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.repository.RoleRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import static com.peoplein.moiming.domain.fixed.QRole.role;

@Repository
@RequiredArgsConstructor
public class RoleJpaRepository implements RoleRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    @Override
    public Role findById(Long id) {
        return queryFactory
                .selectFrom(role)
                .where(role.id.eq(id))
                .fetchOne();
    }

    @Override
    public Role findByRoleType(RoleType roleType) {
        return queryFactory
                .selectFrom(role)
                .where(role.roleType.eq(roleType))
                .fetchOne();
    }

    @Override
    public Long save(Role role) {
        em.persist(role);
        return role.getId();
    }
}
