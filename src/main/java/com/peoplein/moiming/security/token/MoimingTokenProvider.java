package com.peoplein.moiming.security.token;

import com.peoplein.moiming.domain.member.Member;

public interface MoimingTokenProvider {

    /*
     Authentication 에 필요한 Token 을 맞춰서 생성한다
     Current Token Type : JWT_AT, JWT_RT
    */
    String generateToken(MoimingTokenType tokenType, Member member);


    /*
     Token 에 함유된 정보인 username (userEmail) 을 반환한다
     우리 앱에서는 JWT Token 만 가지고 Authentication 객체를 형성하지 않고, DB 조회를 진행하여 검증된
     Authentication 객체를 실어줄 것임
     - 차피 인앱에서 필요시 Query 때려야 하는건 똑같음
     */
    String verifyMemberEmail(MoimingTokenType tokenType, String token);

}
