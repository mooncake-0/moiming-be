package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.member.MemberRole;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.enums.RoleType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRoleDto {

    private RoleType roleType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MemberRoleDto(MemberRole roleLinker) {

        Role role = roleLinker.getRole();
        this.roleType = role.getRoleType();
        this.createdAt = roleLinker.getCreatedAt();
        this.updatedAt = roleLinker.getUpdatedAt();

    }
}
