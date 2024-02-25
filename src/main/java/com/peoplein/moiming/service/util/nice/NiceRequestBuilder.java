package com.peoplein.moiming.service.util.nice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.exception.MoimingAuthApiException;
import com.peoplein.moiming.service.util.sms.body.CoolSmsBodyTemplate;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

import static com.peoplein.moiming.security.exception.AuthExceptionValue.AUTH_COMMON_JSON_IMPL_FAIL;
import static com.peoplein.moiming.service.util.nice.body.NiceBodyTemplate.*;

@Slf4j
@Component
public class NiceRequestBuilder {

    private final ObjectMapper om = new ObjectMapper();
    private final String PATH_CRYPTO_TOKEN_REQ = "https://svc.niceapi.co.kr:22001/digital/niceid/api/v1.0/common/crypto/token";
    private final String AUTHORIZATION_HEADER = "Authorization";
    private final String PRODUCT_ID_HEADER = "ProductId";

//    @Value("${open_api.nice_api_client_id}")
    private String niceClientId;
//    @Value("${open_api.nice_api_client_secret}")
    private String niceClientSecret;
//    @Value("${open_api.nice_api_agency_token}")
    private String niceAgencyToken;
//    @Value("${open_api.nice_api_agency_product_id}")
    private String niceAgencyProductId;


    public Request getCryptoTokenReq(String reqDtim, String reqNo) {
        String cryptoTokenReqHeader = buildCryptoTokenReqHeader();
        NiceCryptoTokenReqDto requestBodyData = new NiceCryptoTokenReqDto(reqDtim, reqNo);
        return createCryptoTokenReq(cryptoTokenReqHeader, requestBodyData);
    }


    private String buildCryptoTokenReqHeader() {

        Date curDate = new Date();
        long currentTimeStamp = curDate.getTime() / 1000;

        String rawAuthToken = niceAgencyToken + ":" + currentTimeStamp + ":" + niceClientId;
        String encodedAuthToken = Base64.getEncoder().encodeToString(rawAuthToken.getBytes());

        return "bearer " + encodedAuthToken;

    }

    private Request createCryptoTokenReq(String header, NiceCryptoTokenReqDto requestBodyData) {
        try {

            String contentType = "application/json";

            // BODY DATA 준비
            RequestBody requestBody = RequestBody.create(MediaType.parse(contentType),
                    om.writeValueAsString(requestBodyData));

            // Request Builder
            return new Request.Builder()
                    .url(PATH_CRYPTO_TOKEN_REQ)
                    .addHeader(AUTHORIZATION_HEADER, header)
                    .addHeader(PRODUCT_ID_HEADER, niceAgencyProductId)
                    .post(requestBody)
                    .build();

        } catch (JsonProcessingException exception) {
            log.error("{}, NICE 요청 JSON 생성 중 Json Process 오류 :: {}", this.getClass().getName(), exception.getMessage());
            throw new MoimingAuthApiException(AUTH_COMMON_JSON_IMPL_FAIL, exception);
        }
    }

}
