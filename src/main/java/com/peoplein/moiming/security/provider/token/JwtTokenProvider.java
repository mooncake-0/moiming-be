package com.peoplein.moiming.security.provider.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.peoplein.moiming.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class JwtTokenProvider implements MoimingTokenProvider {


    @Override
    public String generateToken(MoimingTokenType tokenType, Member member) {

        long expiresAt = System.currentTimeMillis();


        log.info(tokenType.toString() + " TOKEN 생성 요청 : 토큰 생성합니다");

        if (tokenType == MoimingTokenType.JWT_AT) {

            expiresAt += JwtParams.AT_TEST_EXPIRATION_TIME;

        } else if (tokenType == MoimingTokenType.JWT_RT) {

            expiresAt += JwtParams.RT_TEST_EXPIRATION_TIME;

        }

        // Token 을 통해 인증시 DB 에 조회할 것이기 때문에, role 도 담을 필요가 없음

        String jwtToken = JWT.create()
                .withSubject(JwtParams.TEST_JWT_SUBJECT)
                .withExpiresAt(new Date(expiresAt))
                .withClaim(JwtParams.CLAIM_KEY_MEMBER_EMAIL, member.getMemberEmail())
                .sign(Algorithm.HMAC512(JwtParams.TEST_JWT_SECRET));


        return jwtToken;

    }

    @Override
    public String verifyMemberEmail(MoimingTokenType tokenType, String token) {

        DecodedJWT decoded = JWT.require(Algorithm.HMAC512(JwtParams.TEST_JWT_SECRET))
                .build()
                .verify(token);

        return decoded.getClaim(JwtParams.CLAIM_KEY_MEMBER_EMAIL).asString();
    }
}
