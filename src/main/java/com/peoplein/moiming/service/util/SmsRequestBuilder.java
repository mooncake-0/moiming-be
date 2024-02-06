package com.peoplein.moiming.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.SmsVerification;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.exception.MoimingAuthApiException;
import com.peoplein.moiming.security.exception.AuthExceptionValue;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.peoplein.moiming.exception.ExceptionValue.COMMON_INVALID_PARAM;
import static com.peoplein.moiming.security.exception.AuthExceptionValue.*;

/*
 네이버 Cloud Platform SENS SMS API 를 사용하는 빈
 */
@Component
@Slf4j
public class SmsRequestBuilder {

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


    public Request getHttpRequest(SmsVerification verification) {

        if (verification == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        String messageContent = buildContent(verification.getVerificationNumber());
        SmsMessageTemplate messageBody = new SmsMessageTemplate(messageContent, verification.getMemberPhoneNumber());

        return createSmsRequest(messageBody);
    }


    private Request createSmsRequest(SmsMessageTemplate messageBody) {

        try {
            // HEADER DATA 준비
            String requestUrl = NAVER_SMS_API_URL + serviceId + "/messages";
            String contentType = "application/json; charset=utf-8";
            String requestTimeStamp = createTimeStamp();
            String requestSignature = createSignature(requestUrl, requestTimeStamp);

            // BODY DATA 준비
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    om.writeValueAsString(messageBody));


            // Request Builder
            return new Request.Builder()
                    .url(requestUrl)
                    .addHeader(HEADER_CONTENT_TYPE, contentType)
                    .addHeader(HEADER_TIME_STAMP, requestTimeStamp)
                    .addHeader(HEADER_ACCESS_KEY, accessKey)
                    .addHeader(HEADER_SIGNATURE, requestSignature)
                    .post(requestBody)
                    .build();

        } catch (JsonProcessingException exception) {
            log.error("{}, SMS 생성 중 Json Process 오류 :: {}", this.getClass().getName(), exception.getMessage());
            throw new MoimingAuthApiException(AUTH_SMS_REQUEST_BUILDING_JSON_FAIL);
        } catch (UnsupportedEncodingException | InvalidKeyException | NoSuchAlgorithmException exception) {
            log.error("{}, SMS 생성 중 Signature 형성 오류 :: {}", this.getClass().getName(), exception.getMessage());
            throw new MoimingAuthApiException(AUTH_SMS_REQUEST_BUILDING_SIGNATURE_FAIL);
        }
    }


    /*
     문자 내용을 생성한다
    */
    private String buildContent(String verificationCode) {
        return String.format("인증번호 [%s]를 입력하여 주세요", verificationCode);
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

        String message = method +
                space +
                requestUrl +
                newLine +
                requestTimeStamp +
                newLine +
                accessKey;

        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));

        return Base64.encodeBase64String(rawHmac);
    }


}
