package com.peoplein.moiming.service.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.model.SmsMessageDto;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/*
 네이버 Cloud Platform SENS SMS API 를 사용하는 빈
 */
@Component
@Slf4j
public class SmsVerificationCore {

    /*
     NAVER SMS Request Header
     POST https://sens.apigw.ntruss.com/sms/v2/services/{serviceId}/messages
        Content-Type: application/json; charset=utf-8
        x-ncp-apigw-timestamp: {Timestamp}
        x-ncp-iam-access-key: {Sub Account Access Key}
        x-ncp-apigw-signature-v2: {API Gateway Signature}
     */

    private final String NAVER_SMS_API_URL = "https://sens.apigw.ntruss.com/sms/v2/";
    private final String HEADER_CONTENT_TYPE = "Content-Type";
    private final String HEADER_TIME_STAMP = "x-ncp-apigw-timestamp";
    private final String HEADER_ACCESS_KEY = "x-ncp-iam-access-key";
    private final String HEADER_SIGNATURE = "x-ncp-apigw-signature-v2";
    private final ObjectMapper om = new ObjectMapper();

    // APP 등록 정보
    @Value("${open_api_keys.naver_sens_sms}")
    private String serviceId;
    @Value("${open_api_keys.naver_access_key_id}")
    private String accessKey;
    @Value("${open_api_keys.naver_secret_key_id}")
    private String secretKey;

    /*
     문자의 최종 Response 를 만든다
     */
    public Request buildResponse(String verificationCode, String phoneNumber) {

        String content = buildContent(verificationCode);

        SmsMessageDto messageBody = SmsMessageDto.createSmsMessageDto(content, phoneNumber);

        try {

            RequestBody requestBody = RequestBody.create(om.writeValueAsString(messageBody)
                    , MediaType.get("application/json; charset=utf-8"));

            return buildRequest(requestBody);

        } catch (NoSuchAlgorithmException | InvalidKeyException | IOException e) {

            log.error("SIGNATURE 형성중 에러 :: {}", e.getMessage());
            throw new RuntimeException(e);

        }
    }

    /*
     문자 내용을 생성한다
    */
    private String buildContent(String verificationCode) {
        return String.format("인증번호 [%s]를 입력하여 주세요", verificationCode);
    }


    /*
     HEADER 정보를 생성하고
     PARAMS 로 받은 Body 를 통해 Request 를 생성한다
     */
    private Request buildRequest(RequestBody requestBody) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {

        // HEADER DATA 준비
        String requestUrl = NAVER_SMS_API_URL + serviceId + "/messages";
        String contentType = "application/json; charset=utf-8";
        String requestTimeStamp = createTimeStamp();
        String requestSignature = createSignature(requestUrl, requestTimeStamp);

        return new Request.Builder()
                .url(requestUrl)
                .addHeader(HEADER_CONTENT_TYPE, contentType)
                .addHeader(HEADER_TIME_STAMP, requestTimeStamp)
                .addHeader(HEADER_ACCESS_KEY, accessKey)
                .addHeader(HEADER_SIGNATURE, requestSignature)
                .post(requestBody)
                .build();
    }

    /*
     NAVER API HEADER 양식대로 TIMESTAMP 를 형성후 반환한다
     */
    private String createTimeStamp() {
        long timestamp = System.currentTimeMillis();
        return Long.toString(timestamp);
    }

    /*
     NAVER API HEADER 양식대로 Signature Key 값을 형성후 반환한다
     */
    private String createSignature(String requestUrl, String requestTimeStamp) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {

        String space = " ";                    // one space
        String newLine = "\n";                    // new line
        String method = "POST";                    // method

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(requestUrl)
                .append(newLine)
                .append(requestTimeStamp)
                .append(newLine)
                .append(accessKey)
                .toString();

        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));

        return Base64.encodeBase64String(rawHmac);
    }


}
