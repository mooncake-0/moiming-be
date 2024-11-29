package com.peoplein.moiming.repository;


import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberInfo;
import com.peoplein.moiming.domain.member.MemberSetting;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    void save(Member member);
    void saveMemberInfo(MemberInfo memberInfo);

    void saveMemberSetting(MemberSetting memberSetting);

    Optional<Member> findById(Long memberId);

    Optional<Member> findWithRolesById(Long memberId);

    Optional<Member> findByMemberPhone(String memberPhone); // v0.1 - 회원 Id 조회

    Optional<Member> findWithMemberInfoById(Long memberId);

    Optional<Member> findByNickname(String nickname);

}