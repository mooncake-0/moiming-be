package com.peoplein.moiming.service.util;

import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.security.token.MoimingTokenProvider;
import lombok.Getter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.peoplein.moiming.exception.ExceptionValue.*;

@Getter
public class LogoutTokenDb implements LogoutTokenManager {

    private static final Map<String, Date> logoutTokenDb = new ConcurrentHashMap<>();

    @Override
    public void saveLogoutToken(String accessToken, Date expireAt) {
        if (logoutTokenDb.containsKey(accessToken)) {
            throw new MoimingApiException(MEMBER_LOGOUT_AT_DUPLICATE);
        }
        logoutTokenDb.put(accessToken, expireAt);
    }

    @Override
    public boolean isUnusableToken(String accessToken) {
        return logoutTokenDb.containsKey(accessToken);
    }


    @Scheduled(fixedRate = 60 * 1000 * 60) // 1시간에 한 번씩
    public void deleteExpiredTokens() {
        System.out.println("Remove Logout Token Scheduler Activated .. ");
        int cnt = 0;
        for (String accessToken : logoutTokenDb.keySet()) {
            Date currentDate = new Date();
            Date expireDate = logoutTokenDb.get(accessToken);
            boolean isAfter = currentDate.after(expireDate); // 지났음
            if (isAfter) {
                logoutTokenDb.remove(accessToken);
                cnt++;
            }
        }
        System.out.println(cnt + " : 개의 만료된 로그아웃 액세스 토큰이 삭제되었습니다");
    }


    public void printCurrent() {
        System.out.println("Printing Current Logout Token DB ============================");
        for (String s : logoutTokenDb.keySet()) {
            System.out.println(s);
        }
    }
}
