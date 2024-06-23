package com.peoplein.moiming.service.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.*;
import com.peoplein.moiming.domain.enums.NotificationSubCategory;
import com.peoplein.moiming.domain.enums.NotificationTopCategory;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.model.FcmMessageDto;
import com.peoplein.moiming.repository.jpa.MemberSettingRepository;
import com.peoplein.moiming.service.MemberSettingService;
import com.peoplein.moiming.service.external.ExternalReqSender;
import com.peoplein.moiming.service.util.DeepLinkManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/*
 모든 알림 송신을 담당
 */

/*

 각 요청 도메인에서 Notification Entity 관리 또한 담당
 생성할 Notification 을 토대로 보낼 알림의 Title, Body 을 형성하여 전달
 대상의 Receiver Token 또한 같이 전달

 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private static final String API_URL = "https://fcm.googleapis.com/v1/projects/moiming-ac87b/messages:send";

    @Value("${app_files.fcm_path}")
    private String fcmFilePath;

    private final ObjectMapper om;
    private final ExternalReqSender reqSender;
    private final DeepLinkManager deepLinkManager;
    private final MemberSettingService memberSettingService;


    private String initAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(fcmFilePath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/firebase.messaging"));
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }


    private String buildMessage(NotificationTopCategory topCategory, NotificationSubCategory subCategory, String receiverToken, String title, String body, String deepLink) throws JsonProcessingException {

        FcmMessageDto.Notification notification = FcmMessageDto.Notification.builder()
                .title(title)
                .body(body)
                .build();

        FcmMessageDto.Data data = FcmMessageDto.Data.builder()
                .topCategory(topCategory.toString().toLowerCase())
                .subCategory(subCategory.toString().toLowerCase())
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


    public void sendSingleMessage(Member receiver
            , NotificationTopCategory topCategory, NotificationSubCategory subCategory
            , String receiverFcmToken, String title, String body
            , Long topCategoryId, Long subCategoryId) {

        System.out.println("FCM LOG:: PASSED 1");

        if (!memberSettingService.isMemberNotifyAllowed(receiver, topCategory)) { // 허용되어 있지 않다면 보내지 않는다
            System.out.println("FCM LOG:: PASSED 2");
            return;
        }

        System.out.println("FCM LOG:: PASSED 3");
        // fcmToken 이 없을 경우엔 처리하지 않는다
        if (!StringUtils.hasText(receiverFcmToken)) {
            System.out.println("FCM LOG:: PASSED 4");
            return;
        }

        System.out.println("FCM LOG:: PASSED 5");

        try {

            String accessToken = initAccessToken();
            System.out.println("FCM LOG:: PASSED 6");

            String deepLink = createDeepLink(topCategoryId, subCategoryId);
            System.out.println("FCM LOG:: PASSED 7");

            String message = buildMessage(topCategory, subCategory, receiverFcmToken, title, body, deepLink);
            System.out.println("FCM LOG:: PASSED 8");

            System.out.println(message);

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));

            System.out.println("FCM LOG:: PASSED 9");


            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(requestBody)
                    .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                    .build();

            System.out.println("FCM LOG:: PASSED 10");

            reqSender.sendAsynchronousMessage(request);

            System.out.println("FCM LOG:: PASSED 11");

        } catch (IOException exception) {

            log.error("CREATE FCM ERROR :: {}", exception.getMessage());
            System.out.println("FCM LOG:: PASSED 12");

        }
    }


    public void sendMultiMessage(List<Member> receivers
            , NotificationTopCategory topCategory, NotificationSubCategory subCategory
            , String title, String body
            , Long topCategoryId, Long subCategoryId) {


        List<String> receiversToken = memberSettingService.getAllowedReceiverFcmTokens(receivers, topCategory);
        if (receiversToken.isEmpty()) {
            return;
        }

        String deepLink = createDeepLink(topCategoryId, subCategoryId);
        Notification notification = Notification.builder().setTitle(title).setBody(body).build();

        MulticastMessage multicastMessage = MulticastMessage.builder()
                .setNotification(notification)
                .putData("topCategory", topCategory.getValue().toLowerCase())
                .putData("subCategory", subCategory.getValue().toLowerCase())
                .putData("deepLink", deepLink)
                .addAllTokens(receiversToken)
                .build();

        ApiFuture<BatchResponse> response = FirebaseMessaging.getInstance().sendEachForMulticastAsync(multicastMessage);

        try {
            if (response.get().getFailureCount() < 0) {
                log.warn("{}, sendMultiMessage :: {}", this.getClass().getName(), "[" + body + "] 의 Multi Message 전송 실패 내역이 존재합니다" );
            }

            List<SendResponse> responses = response.get().getResponses();
            for (SendResponse resp : responses) {
                log.warn("{}, sendMultiMessage :: {}", this.getClass().getName(), "[" + body + "] 의 메세지 전송 내역이" +
                        "[" + resp.getException().getMessage() + "] 의 사유로 전송에 실패했습니다");
            }

        } catch (InterruptedException | ExecutionException e) {
            log.warn("{}, sendMultiMessage :: {}", this.getClass().getName(), "[" + e.getMessage() + "] 의 사유로 BatchResponse 해독에 실패했습니다" );
        }

    }


    private String createDeepLink(Long topCategoryId, Long subCategoryId) {
        return deepLinkManager.createDeepLink(topCategoryId, subCategoryId);
    }

}
