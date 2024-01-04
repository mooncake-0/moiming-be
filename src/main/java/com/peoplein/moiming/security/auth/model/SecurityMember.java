package com.peoplein.moiming.security.auth.model;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberRole;
import com.peoplein.moiming.domain.enums.RoleType;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/*
 DB 조회 완료된 Member 만을 통해서
 Build 되는 UserDetails 객체이다
 - Member 외 주입해줄 필요가 없음
 */
@Getter
public class SecurityMember implements UserDetails {


    private final Member member;
    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityMember(Member member) {
        this.member = member;
        this.authorities = convertRoles();
    }

    /*
     Security Member 도메인에서 필요한 부분이므로 여기서 초기화한다
     */
    private Collection<? extends GrantedAuthority> convertRoles() {

        Set<GrantedAuthority> authorities = new HashSet<>();

        for (MemberRole roleLinker : this.member.getRoles()) {
            RoleType roleType = roleLinker.getRole().getRoleType();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleType));
        }

        return authorities;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getMemberEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
