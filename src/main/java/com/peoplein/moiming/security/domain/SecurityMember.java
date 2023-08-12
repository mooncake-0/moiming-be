package com.peoplein.moiming.security.domain;

import com.peoplein.moiming.domain.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*
 Spring Security Authentication 과 앱 Member Entity를
 연동해주는 객체
 */
public class SecurityMember extends User {

    private final Member member;

    public SecurityMember(Member member, Collection<? extends GrantedAuthority> authorities) {
        super(member.getMemberEmail(), member.getPassword(), authorities); // 여기서 User 객체로 Authority 가 들어간다
        this.member = member;
    }

    public Member getMember() {
        return this.member;
    }
}