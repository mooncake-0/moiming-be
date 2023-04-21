package com.peoplein.moiming.service.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.peoplein.moiming.model.FcmMessageDto;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

/*
 모든 알림 송신을 담당
 */

/*

 각 요청 도메인에서 Notification Entity 관리 또한 담당
 생성할 Notification 을 토대로 보낼 알림의 Title, Body 을 형성하여 전달
 대상의 Receiver Token 또한 같이 전달

 */
@Service
@Slf4j
public class FcmService {

    // 초기화 이후 앱 내에서 지속 사용 가능
    private String appAccessToken;
    private String API_URL = "https://fcm.googleapis.com/v1/projects/moiming-b2ae3/messages:send";

    private final ObjectMapper om = new ObjectMapper();

    private void initAccessToken() throws IOException {
        String firebaseConfigPath = "src/main/resources/fcm/moiming-b2ae3-firebase-adminsdk-21zjr-11c77c69f7.json";
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/firebase.messaging"));
        googleCredentials.refreshIfExpired();
        appAccessToken = googleCredentials.getAccessToken().getTokenValue();
    }


    public String buildMessage(String receiverToken, String title, String body) throws IOException {

        FcmMessageDto.Notification notification = FcmMessageDto.Notification.builder()
                .title(title)
                .body(body)
                .build();

        FcmMessageDto.Message message = FcmMessageDto.Message.builder()
                .token(receiverToken)
                .notification(notification)
                .build();

        FcmMessageDto fcmMessageDto = FcmMessageDto.builder()
                .message(message)
                .build();

        return om.writeValueAsString(fcmMessageDto);

    }

    public void sendSingleMessageTo(String receiverToken, String title, String body) throws IOException {

        if (!StringUtils.hasText(appAccessToken)) {
            initAccessToken();
        }

        String message = buildMessage(receiverToken, title, body);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + appAccessToken)
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        Response response = client.newCall(request).execute();

        log.info("FCM SENT:: RESPONSE:: {}", response.body().string());

    }

    public void sendBatchMessageTo(List<String> receiverToken, String title, String body) throws IOException {

    }

}
