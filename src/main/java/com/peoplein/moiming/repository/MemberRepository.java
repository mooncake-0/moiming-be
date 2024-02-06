package com.peoplein.moiming.repository;


import com.peoplein.moiming.domain.member.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    void save(Member member);

    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String memberEmail);

    Optional<Member> findByNickname(String nickname);

    Optional<Member> findWithMemberInfoByPhoneNumber(String memberPhoneNumber);

    List<Member> findMembersByIds(List<Long> memberIds);

    List<Member> findMembersByEmailOrPhoneOrCi(String memberEmail, String memberPhone, String ci);

    void updateRefreshTokenById(Long id, String refreshToken);

}