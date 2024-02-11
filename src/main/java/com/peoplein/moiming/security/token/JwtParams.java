package com.peoplein.moiming.security.token;

public abstract class JwtParams {

    public static String TEST_JWT_SECRET = "SAMPLE_SECRET";
    public static String TEST_JWT_SUBJECT = "SAMPLE_SUBJECT";
    public static int AT_TEST_EXPIRATION_TIME = 1000 * 60 * 30; // 30 분
    public static int RT_TEST_EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7 * 2; // 2주


    // CLAIM 정보
    public static String CLAIM_KEY_MEMBER_EMAIL = "EMAIL";
    public static String CLAIM_KEY_MEMBER_ID = "ID";
    public static String CLAIM_KEY_MEMBER_ROLE = "ROLES";

    public static String HEADER = "Authorization";
    public static String PREFIX = "Bearer ";

}
