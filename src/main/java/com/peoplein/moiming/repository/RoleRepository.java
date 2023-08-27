package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.enums.RoleType;
import org.springframework.transaction.annotation.Transactional;

public interface RoleRepository {

    Role findById(Long id);

    Role findByRoleType(RoleType roleType);

    @Transactional
    Long save(Role role);

}
