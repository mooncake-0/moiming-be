package com.peoplein.moiming.service.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.peoplein.moiming.model.FcmMessageDto;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
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

    private static final String API_URL = "https://fcm.googleapis.com/v1/projects/moiming-ac87b/messages:send";

    @Value("${app_files.fcm_path}")
    private String fcmFilePath;

    private final ObjectMapper om = new ObjectMapper();

    private String initAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(fcmFilePath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/firebase.messaging"));
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }


    private String buildMessage(String receiverToken, String title, String body, String deepLink) throws JsonProcessingException {

        FcmMessageDto.Notification notification = FcmMessageDto.Notification.builder()
                .title(title)
                .body(body)
                .build();

        FcmMessageDto.Data data = FcmMessageDto.Data.builder()
                .deepLink(deepLink)
                .build();

        FcmMessageDto.Message message = FcmMessageDto.Message.builder()
                .token(receiverToken)
                .notification(notification)
                .data(data)
                .build();

        FcmMessageDto fcmMessageDto = FcmMessageDto.builder()
                .message(message)
                .build();

        return om.writeValueAsString(fcmMessageDto);

    }


    public void sendSingleMessage(String receiverToken, String title, String body, String deepLink) {

        try {

            String accessToken = initAccessToken();
            String message = buildMessage(receiverToken, title, body, deepLink);

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(requestBody)
                    .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                    .build();

            Response response = client.newCall(request).execute();

            log.info("FCM SENT:: RESPONSE:: {}", response.body().string());

        } catch (IOException exception) {

            // TODO : FCM SEND FAILURE HANDLING NEEDED
            log.error("CREATE FCM ERROR :: {}", exception.getMessage());

        }
    }

    public void sendMultiMessage(List<String> receiverToken, String title, String body) throws IOException {

    }

}
