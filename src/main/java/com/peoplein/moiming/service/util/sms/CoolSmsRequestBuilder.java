package com.peoplein.moiming.service.util.sms;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.SmsVerification;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.exception.MoimingAuthApiException;
import com.peoplein.moiming.service.util.sms.body.CoolSmsBodyTemplate;
import com.peoplein.moiming.service.util.sms.body.NaverSmsBodyTemplate;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;

import static com.peoplein.moiming.exception.ExceptionValue.COMMON_INVALID_PARAM;
import static com.peoplein.moiming.security.exception.AuthExceptionValue.AUTH_SMS_REQUEST_BUILDING_JSON_FAIL;
import static com.peoplein.moiming.security.exception.AuthExceptionValue.AUTH_SMS_REQUEST_BUILDING_SIGNATURE_FAIL;


/*
  Time 과 Hex 추가는 cool-sms git 참고함
  https://github.com/coolsms/examples/blob/master/java/src/APIInit.java#L27-L37
 */
@Component
@Slf4j
public class CoolSmsRequestBuilder implements SmsRequestBuilder {

    private final ObjectMapper om = new ObjectMapper();
    private final String AUTHORIZATION_HEADER = "Authorization";
    private String moimingSender = "01062338556";
    private String SIGNATURE_ALGO = "HmacSHA256";
    private String AUTHENTICATION_METHOD = "HMAC-SHA256";

    @Value("${open_api.cool_sms_api_key}")
    private String apiKey;
    @Value("${open_api.cool_sms_secret_key}")
    private String secretKey;

    @Override
    public Request getHttpRequest(SmsVerification verification) {

        if (verification == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        String messageContent = buildContent(verification.getVerificationNumber());
        CoolSmsBodyTemplate messageBody = new CoolSmsBodyTemplate(verification.getMemberPhoneNumber(), moimingSender, messageContent);

        return createSmsRequest(messageBody);
    }


    private Request createSmsRequest(CoolSmsBodyTemplate messageBody) {
        try {

            String requestUrl = "https://api.coolsms.co.kr/messages/v4/send";
            String contentType = "application/json; charset=utf-8";
            String salt = createSalt();
            String date = createDate();
            String signature = createSignature(date, salt);

            // BODY DATA 준비
            RequestBody requestBody = RequestBody.create(MediaType.parse(contentType),
                    om.writeValueAsString(messageBody));

            String authorizationHeaderVal = AUTHENTICATION_METHOD + " apikey=" + apiKey + ", date=" + date
                    + ", salt=" + salt + ", signature=" + signature;

            // Request Builder
            return new Request.Builder()
                    .url(requestUrl)
                    .addHeader(AUTHORIZATION_HEADER, authorizationHeaderVal)
                    .post(requestBody)
                    .build();

        } catch (JsonProcessingException exception) {
            log.error("{}, SMS 생성 중 Json Process 오류 :: {}", this.getClass().getName(), exception.getMessage());
            throw new MoimingAuthApiException(AUTH_SMS_REQUEST_BUILDING_JSON_FAIL, exception);
        } catch (InvalidKeyException | NoSuchAlgorithmException exception) {
            log.error("{}, SMS 생성 중 Signature 형성 오류 :: {}", this.getClass().getName(), exception.getMessage());
            throw new MoimingAuthApiException(AUTH_SMS_REQUEST_BUILDING_SIGNATURE_FAIL, exception);
        }
    }


    /*
     문자 내용을 생성한다
    */
    private String buildContent(String verificationCode) {
        return String.format("인증번호 [%s]를 입력하여 주세요", verificationCode);
    }


    /*
     TIMESTAMP 를 형성후 반환한다
     Cool SMS 는 ISO-8601 형식으로 반환
     */
    private String createDate() {
         return ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString().split("\\[")[0];
    }


    private String createSignature(String date, String salt) throws NoSuchAlgorithmException, InvalidKeyException {

        String target = date + salt;

        Mac hmacSha256 = Mac.getInstance(SIGNATURE_ALGO);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), SIGNATURE_ALGO);
        hmacSha256.init(secretKeySpec);

        byte[] hmacBytes = hmacSha256.doFinal(target.getBytes());

//        return Base64.getEncoder().encodeToString(hmacBytes);
        return new String(Hex.encodeHex(hmacBytes));

    }


    // 12 ~ 64 바이트의 불규칙적이고 랜덤한 문자열을 생성하여 반환한다
    private String createSalt() {
        SaltCreator saltCreator = new SaltCreator();
        return saltCreator.generateRandomString(32);
    }


}
