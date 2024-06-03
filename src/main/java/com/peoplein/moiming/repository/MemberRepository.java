package com.peoplein.moiming.repository;


import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberInfo;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    void save(Member member);
    void save(MemberInfo memberInfo);

    Optional<Member> findById(Long memberId);

    Optional<Member> findByMemberPhone(String memberPhone); // v0.1 - 회원 Id 조회

    Optional<Member> findWithMemberInfoById(Long memberId);

    Optional<Member> findByNickname(String nickname);

//    Optional<Member> findWithMemberInfoByPhoneNumber(String memberPhoneNumber);
//    List<Member> findMembersByEmailOrPhone(String memberEmail, String memberPhone);

}