package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class RoleJpaRepository implements RoleRepository {

    private final EntityManager em;

    @Override
    public Role findById(Long id) {
        return em.find(Role.class, id);
    }

    @Override
    public Role findByRoleType(RoleType roleType) {
        return em.createQuery("select r from Role r where r.roleType = :roleType", Role.class)
                .setParameter("roleType", roleType)
                .getSingleResult();
    }

    @Override
    public Long save(Role role) {
        em.persist(role);
        return role.getId();
    }
}
