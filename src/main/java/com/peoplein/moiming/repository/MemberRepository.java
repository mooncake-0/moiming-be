package com.peoplein.moiming.repository;


import com.peoplein.moiming.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    void save(Member member);

    Member findMemberById(Long id);

    Member findMemberAndMemberInfoById(Long id);

    Optional<Member> findMemberByEmail(String memberEmail);

    Optional<Member> findByNickname(String nickname);

    //    Member findMemberWithRolesByUid(String uid);
    Member findMemberWithRolesByEmail(String memberEmail);

    Member findMemberAndMemberInfoWithRolesById(Long id);

    List<Member> findMembersByIds(List<Long> memberIds);

    Optional<Member> findOptionalByPhoneNumber(String memberPhoneNumber);

    List<Member> findByEmailOrPhone(String memberEmail, String memberPhone);

    void updateRefreshTokenByEmail(Long id, String refreshToken);
}