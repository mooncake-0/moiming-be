package com.peoplein.moiming.security.provider.token;

import com.peoplein.moiming.security.token.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public interface MoimingTokenProvider {

    /*
     Authentication 에 필요한 Token 을 맞춰서 생성한다
     Current Token Type : JWT_AT, JWT_RT
     */
    String generateToken(MoimingTokenType tokenType, UserDetails userDetails);

    /*
     Token 을 전달받아 검증한다
     */
    boolean validateToken(String token, MoimingTokenType tokenType);

    /*
     Token 에 함유된 정보를 통해 Authentication 객체를 반환한다
     */
    String retrieveUid(String token, MoimingTokenType tokenType);

}
