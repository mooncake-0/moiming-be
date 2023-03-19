package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role findById(Long roleId) {
        return roleRepository.findById(roleId);
    }

    public Role findByRoleType(RoleType roleType) {
        return roleRepository.findByRoleType(roleType);
    }
}
