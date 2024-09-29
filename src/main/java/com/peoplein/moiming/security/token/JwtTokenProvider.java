package com.peoplein.moiming.security.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.security.auth.model.SecurityMember;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider implements MoimingTokenProvider {


    @Override
    public TokenDto generateToken(MoimingTokenType tokenType, Member member) {

        long expiresAt = System.currentTimeMillis();

        if (tokenType == MoimingTokenType.JWT_AT) {

            expiresAt += JwtParams.AT_TEST_EXPIRATION_TIME;

        } else if (tokenType == MoimingTokenType.JWT_RT) {

            expiresAt += JwtParams.RT_TEST_EXPIRATION_TIME;

        }

        Date expDate = new Date(expiresAt);

        List<String> roles = member.getRoles().stream().map(memberRole -> memberRole.getRole().getRoleType().toString()).collect(Collectors.toList());

        // Token 을 통해 인증시 DB 에 조회할 것이기 때문에, role 도 담을 필요가 없음
        String jwtToken = JWT.create()
                .withSubject(JwtParams.TEST_JWT_SUBJECT)
                .withExpiresAt(expDate)
                .withClaim(JwtParams.CLAIM_KEY_MEMBER_ID_KEY, member.getId())
                .withClaim(JwtParams.CLAIM_KEY_MEMBER_ROLE, roles)
//                .withClaim(JwtParams.CLAIM_KEY_MEMBER_ID_KEY, member.getMemberPhone()) // Member Phone 은 사용되지 않는다
                .sign(Algorithm.HMAC512(JwtParams.TEST_JWT_SECRET));

        return new TokenDto(jwtToken, expDate.getTime());

    }

    @Override
    public UserDetails verifyAndCreateAuthUser(MoimingTokenType tokenType, String token) {

        DecodedJWT decoded = JWT.require(Algorithm.HMAC512(JwtParams.TEST_JWT_SECRET))
                .build()
                .verify(token);

        Long memberId = decoded.getClaim(JwtParams.CLAIM_KEY_MEMBER_ID_KEY).asLong();
        List<String> roles = decoded.getClaim(JwtParams.CLAIM_KEY_MEMBER_ROLE).asList(String.class);
        Member fakeAuthMember = Member.createTokenAuthenticationMember(memberId);

        return new SecurityMember(fakeAuthMember, roles);
    }


    @Override
    public Long verifyMemberId(MoimingTokenType tokenType, String token) {

        DecodedJWT decoded = JWT.require(Algorithm.HMAC512(JwtParams.TEST_JWT_SECRET))
                .build()
                .verify(token);


        return decoded.getClaim(JwtParams.CLAIM_KEY_MEMBER_ID_KEY).asLong();
    }


    @Override
    public Date verifyExpireAt(MoimingTokenType tokenType, String token) {

        DecodedJWT decoded = JWT.require(Algorithm.HMAC512(JwtParams.TEST_JWT_SECRET))
                .build()
                .verify(token);

        return decoded.getExpiresAt();
    }
}
