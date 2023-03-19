package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.enums.RoleType;

public interface RoleRepository {

    Role findById(Long id);

    Role findByRoleType(RoleType roleType);

    Long save(Role role);

}
