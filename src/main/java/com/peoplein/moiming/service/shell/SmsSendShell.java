package com.peoplein.moiming.service.shell;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.model.SmsMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/*
 OkHttpClient 를 주입받아
 Naver API 와의 직접적인 통신 담당
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SmsSendShell {

    private final OkHttpClient okHttpClient;


    /*
     실패에 대한 별도의 action 처리는 필요하지 않음 - 미수신시 Client 단에서 재요청 필요
     */
    public void sendMessage(Request request) {

        // .execute() 함수는 동기처리 함수인듯
        // Response response = okHttpClient.newCall(request).execute();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

                assert request.body() != null;
                log.error("NAVER SMS API :: SMS 문자 실패 - {}, {}", request.body().toString(), e.getMessage());

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                log.info("NAVER SMS API :: SMS 문자 시도 - {}", response.body().string());
            }
        });


    }
}
