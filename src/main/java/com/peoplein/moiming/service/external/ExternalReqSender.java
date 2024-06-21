package com.peoplein.moiming.service.external;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class ExternalReqSender {

    // 전역으로 해당 빈 안에서 계속 사용할 수 있도록 한다
    private final OkHttpClient okHttpClient = new OkHttpClient();


    /*
     성공, 실패에 대한 별도의 action 처리는 필요하지 않음 - 미수신시 Client 단에서 재요청 필요 - 미수신시 다시 보내기
     */
    public void sendAsynchronousMessage(Request request) {

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

                assert request.body() != null;
                log.error("비동기 외부 요청 송신 실패 - {}, {}", request.body().toString(), e.getMessage());

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                log.info("비동기 외부 요청 송신 성공 - {}", response.body().string()); // 굳이?
            }
        });
    }


    public Response sendSynchronousMessage(Request request) {
        try {
            return okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
