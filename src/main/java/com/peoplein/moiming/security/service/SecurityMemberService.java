package com.peoplein.moiming.security.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MemberRoleLinker;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.security.provider.token.MoimingTokenProvider;
import com.peoplein.moiming.security.provider.token.MoimingTokenType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.util.*;

@Slf4j
@Transactional
public class SecurityMemberService implements UserDetailsService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MoimingTokenProvider moimingTokenProvider;

    /*
     전달받은 UID 를 통해 해당 UID 의 유저가 존재하는지 확인한다
     조회 후 Encrypt 되어 있는 Password 와 함께 반환한다
     */
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String uid) throws UsernameNotFoundException {

        Member memberByUid = memberRepository.findMemberWithRolesByUid(uid);

        // Query 확인용: 제거 필요
        System.out.println("memberService.loadUserByName 에서 findMemberWithRolesByUid 호출되었습니다===========================");

        if (Objects.isNull(memberByUid)) {
            String msg = "[" + uid + "]의 유저를 찾을 수 없습니다";
            log.error(msg);
            throw new UsernameNotFoundException(msg);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        for (MemberRoleLinker roleLinker : memberByUid.getRoles()) {
            RoleType roleType = roleLinker.getRole().getRoleType();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleType));
        }

        return new SecurityMember(memberByUid, authorities);
    }

    /*
     REFRESH_TOKEN 검증 중 전달 받은 UID 의 유저가 존재하는지 확인한다
     해당 유저 DB에 저장되어 있는 REFRESH_TOKEN 과 일치하는지 확인한다
     */
    @Transactional
    public UserDetails loadUserAndValidateRefreshToken(String uid, String refreshToken) throws UsernameNotFoundException {

        Member memberByUid = memberRepository.findMemberWithRolesByUid(uid);

        if (Objects.isNull(memberByUid)) {
            String msg = "[" + uid + "]의 유저를 찾을 수 없습니다";
            log.error(msg);
            throw new UsernameNotFoundException(msg);
        }

        if (!refreshToken.equals(memberByUid.getRefreshToken())) {
            String msg = "[" + uid + "]의 REFRESH TOKEN 이 일치하지 않습니다";
            log.error(msg);
            throw new InvalidParameterException(msg);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        for (MemberRoleLinker roleLinker : memberByUid.getRoles()) {
            RoleType roleType = roleLinker.getRole().getRoleType();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleType));
        }

        SecurityMember securityMember = new SecurityMember(memberByUid, authorities);

        String resetRefreshToken = moimingTokenProvider.generateToken(MoimingTokenType.JWT_RT, securityMember);
        securityMember.getMember().changeRefreshToken(resetRefreshToken);


        return securityMember;
    }

    /*
     Login 성공시 ResponseModel SU
     1) Member (With Role-전송 필요) 및 MemberInfo 영속화
     2) AccessToken 및 RefreshToken 재발급 및 Update
     3) 전달을 위해 준비된 항목 전달
     */
    public Map<String, Object> prepareLoginResponseModel(SecurityMember securityMember) {

        Member member = memberRepository.findMemberAndMemberInfoWithRolesById(securityMember.getMember().getId());

        String accessToken = moimingTokenProvider.generateToken(MoimingTokenType.JWT_AT, securityMember);
        String refreshToken = moimingTokenProvider.generateToken(MoimingTokenType.JWT_RT, securityMember);

        member.changeRefreshToken(refreshToken);

        Map<String, Object> valueMap = new HashMap<>();

        valueMap.put("member", member);
        valueMap.put(MoimingTokenType.JWT_AT.name(), accessToken);
        valueMap.put(MoimingTokenType.JWT_RT.name(), refreshToken);

        return valueMap;
    }

}
