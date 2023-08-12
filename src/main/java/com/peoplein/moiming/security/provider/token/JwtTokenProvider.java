package com.peoplein.moiming.security.provider.token;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.peoplein.moiming.security.JwtPropertySetting;
import com.peoplein.moiming.security.domain.SecurityMember;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

@Slf4j
public class JwtTokenProvider implements MoimingTokenProvider {

    @Override
    public String generateToken(MoimingTokenType tokenType, UserDetails authentication) {

        String subject = "";
        long expiresAt = System.currentTimeMillis();
        String secretKey = "";

        SecurityMember securityMember = (SecurityMember) authentication;

        if (tokenType == MoimingTokenType.JWT_AT) {

            System.out.println("Creating Jwt Access Token ... ");
            subject = JwtPropertySetting.SUBJECT_AT;
            expiresAt += JwtPropertySetting.EXPIRATION_TIME_AT;
//            expiresAt += JwtPropertySetting.TEST_EXPIRED_DATE;
            secretKey = JwtPropertySetting.MOIMING_JWT_ACCESS_TOKEN_SECRET;

        } else if (tokenType == MoimingTokenType.JWT_RT) {

            System.out.println("Creating Jwt Refresh Token ... ");
            subject = JwtPropertySetting.SUBJECT_RT;
            expiresAt += JwtPropertySetting.EXPIRATION_TIME_RT;
//            expiresAt += JwtPropertySetting.TEST_EXPIRED_DATE_RT;
            secretKey = JwtPropertySetting.MOIMING_JWT_REFRESH_TOKEN_SECRET;

        }

        String jwtToken = JWT.create()
                .withSubject(subject)
                .withExpiresAt(new Date(expiresAt))
                .withClaim(JwtPropertySetting.MEMBER_EMAIL, securityMember.getMember().getMemberEmail())
                .sign(Algorithm.HMAC512(secretKey));

        return jwtToken;
    }

    /*
     JWT Token Provider 에서는 통과 로직
     에러 발생시 다 throw Exception 하고, 해당 Exception 을 전달한다
    */
    @Override
    public boolean validateToken(String jwtToken, MoimingTokenType tokenType) {

        try {

            String secret = "";

            if (tokenType.equals(MoimingTokenType.JWT_AT)) {
                secret = JwtPropertySetting.MOIMING_JWT_ACCESS_TOKEN_SECRET;
            } else if (tokenType.equals(MoimingTokenType.JWT_RT)) {
                secret = JwtPropertySetting.MOIMING_JWT_REFRESH_TOKEN_SECRET;
            } else {
                throw new JWTVerificationException("토큰의 종류를 알 수 없습니다");
            }

            JWT.require(Algorithm.HMAC512(secret))
                    .build()
                    .verify(jwtToken);

            return true;

        } catch (TokenExpiredException exception) {
            log.error("Token 이 만료되었습니다", exception.getLocalizedMessage());
            throw exception;
        } catch (JWTVerificationException exception) {
            log.error("Token 검증에 실패하였습니다", exception.getLocalizedMessage());
            throw exception;
        } catch (Exception exception) {
            log.error("Token 검증 중 알 수 없는 오류가 발생하였습니다", exception.getLocalizedMessage());
            throw exception;
        }
    }

    @Override
    public String retrieveEmail(String jwtToken, MoimingTokenType tokenType) {

        try {
            String secret = "";

            if (tokenType.equals(MoimingTokenType.JWT_AT)) {
                secret = JwtPropertySetting.MOIMING_JWT_ACCESS_TOKEN_SECRET;
            } else if (tokenType.equals(MoimingTokenType.JWT_RT)) {
                secret = JwtPropertySetting.MOIMING_JWT_REFRESH_TOKEN_SECRET;
            } else {
                return "";
            }

            return JWT.require(Algorithm.HMAC512(secret))
                    .build()
                    .verify(jwtToken)
                    .getClaim(JwtPropertySetting.MEMBER_EMAIL)
                    .asString();

        } catch (Exception exception) {

            log.error("JWT 인증 중 UID RETRIEVE 오류 발생:: {}", exception.getMessage());
            throw exception;
        }
    }
}
