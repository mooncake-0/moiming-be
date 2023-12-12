package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.member.MemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRoleLinkerRepository extends JpaRepository<MemberRole, Long> {
}
