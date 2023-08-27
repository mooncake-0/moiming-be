package com.peoplein.moiming.service.util;

import java.util.Random;

public class MemberNicknameCreator {

    private final String[] nicknameHead = {"moimer", "ballooner", "balloons", "moiming", "moimlove", "hiballoon", "wemoim"};

    public String createNickname() {

        StringBuilder nickname = new StringBuilder();
        Random random = new Random();

        int randomHead = random.nextInt(7); // 0~6 사이의 정수
        nickname.append(nicknameHead[randomHead]);

        // 5자리의 숫자 문자열 생성
        for (int i = 0; i < 5; i++) {
            int randomNumber = random.nextInt(10); // 0 ~ 9 사이의 정수
            nickname.append(randomNumber);
        }

        return nickname.toString();
    }

}
