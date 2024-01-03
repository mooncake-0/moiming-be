package com.peoplein.moiming.service.util;

import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.security.token.MoimingTokenProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.peoplein.moiming.exception.ExceptionValue.*;

@Slf4j
@Getter
public class LogoutTokenDb implements LogoutTokenManager {


    private static final Map<String, Date> logoutTokenDb = new ConcurrentHashMap<>();


    @Override
    public void saveLogoutToken(String accessToken, Date expireAt) {
        if (logoutTokenDb.containsKey(accessToken)) {
            String errMsg = COMMON_INVALID_SITUATION.getErrMsg() + " :: 이미 로그아웃 관리중인 Access Token 을 다시 등록하려는 시도";
            log.info("Class {} : {}", getClass().getName(), errMsg);
            throw new MoimingApiException(COMMON_INVALID_SITUATION);
        }
        logoutTokenDb.put(accessToken, expireAt);
    }


    @Override
    public boolean isUnusableToken(String accessToken) {
        return logoutTokenDb.containsKey(accessToken);
    }


    @Override
    public void clearManager() {
        logoutTokenDb.clear();
    }


    @Scheduled(fixedRate = 60 * 1000 * 60 * 24) // 일단 하루에 한 번씩
    public int deleteExpiredTokens() {
        log.info("{}", "만료된 로그아웃 관리 토큰 확인 중 .. ");
        int cnt = 0;
        for (String accessToken : logoutTokenDb.keySet()) {
            Date current = new Date();
            Date expireDate = logoutTokenDb.get(accessToken);
            if (current.after(expireDate)) {
                logoutTokenDb.remove(accessToken);
                cnt++;
            }
        }
        log.info("{} : {}", cnt, "개의 만료된 로그아웃 액세스 토큰이 삭제되었습니다");
        return cnt;
    }

}
