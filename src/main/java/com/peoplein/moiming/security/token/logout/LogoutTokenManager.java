package com.peoplein.moiming.security.token.logout;

import java.util.Date;

public interface LogoutTokenManager {

    // 제공하는 DB 에 저장할 수 있다
    void saveLogoutToken(String accessToken, Date expireAt);

    // 로그아웃 된 토큰임을 판별한다
    boolean isUnusableToken(String accessToken);

    // 저장된 객체를 모두 지운다
    void clearManager();
}
