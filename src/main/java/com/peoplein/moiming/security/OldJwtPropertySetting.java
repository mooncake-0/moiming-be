package com.peoplein.moiming.security;

/*
 Secured Info
 */
public interface OldJwtPropertySetting {

    /*
     보안 전략
     1. JWT SECRET KEY 는 지속 변경 (운영시 Rule 만들기)
     2. Member UID 외에 정보는 없을 것
     3. RefreshToken 을 사용시  Refresh Token 재발급
     */
//    String JWT_SECRET = "MOIMING_SAMPLE_SECRET";
    String MOIMING_JWT_ACCESS_TOKEN_SECRET = "MOIMING_SAMPLE_ACCESS_SECRET";
    String MOIMING_JWT_REFRESH_TOKEN_SECRET = "MOIMING_SAMPLE_REFRESH_SECRET";

    // ACCESS TOKEN PROPERTY
    int EXPIRATION_TIME_AT = 60000 * 30; // 30 분
    String HEADER_AT = "ACCESS_TOKEN";
    String SUBJECT_AT = "Moiming Jwt Access Token";

    // REFRESH TOKEN PROPERTY
    int EXPIRATION_TIME_RT = 60000 * 20160; // 2주
    String HEADER_RT = "REFRESH_TOKEN";
    String SUBJECT_RT = "Moiming Jwt Refresh Token";

    // TEST EXPIRED DATE
    int TEST_EXPIRED_DATE = 6000 * 5; // 30초
    int TEST_EXPIRED_DATE_RT = 60000 * 2; // 1 분

    // CLAIM CATEGORY
    String MEMBER_ID = "id";
    String MEMBER_EMAIL = "email";
    String MEMBER_ROLES = "roles";

}