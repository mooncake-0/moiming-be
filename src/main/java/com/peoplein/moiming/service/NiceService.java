package com.peoplein.moiming.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.config.AppUrlPath;
import com.peoplein.moiming.exception.MoimingAuthApiException;
import com.peoplein.moiming.security.exception.AuthExceptionValue;
import com.peoplein.moiming.service.external.ExternalReqSender;
import com.peoplein.moiming.service.util.nice.NiceParamsDto;
import com.peoplein.moiming.service.util.nice.NiceRequestBuilder;
import com.peoplein.moiming.service.util.nice.NiceSecureHelper;
import com.peoplein.moiming.service.util.nice.body.NiceBodyTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.peoplein.moiming.security.exception.AuthExceptionValue.*;
import static com.peoplein.moiming.service.util.nice.NiceParamsDto.*;
import static com.peoplein.moiming.service.util.nice.body.NiceBodyTemplate.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NiceService {

    private final ObjectMapper om = new ObjectMapper();
    private final NiceRequestBuilder niceRequestBuilder;
    private final ExternalReqSender externalReqSender;
    private final NiceSecureHelper niceSecureHelper;

    /*
     진행 순서
     1) 1시간 유효한 토큰을 NICE 측으로부터 우선 받아온다
     2) 위에서 받은 값으로 대칭키를 생성한다 - API 참고
     3) 내가 요청할 데이터에 대한 암호화를 진행한다 - API 참고
     4) NICE 인증에 필요한 정보들을 form 에 담은 http 를 client 에게 전달한다
     */

    public NiceStandardFormParamsDto getNiceReqParams() {

        // TODO :: 어떤 종류의 간단한 인증 같은 부분이 필요하긴 할 듯
        String reqDtim = createReqDtim();
        String reqNo = "SOMETHING";
        Request request = niceRequestBuilder.getCryptoTokenReq(reqDtim, reqNo);
        Response response = externalReqSender.sendSynchronousMessage(request);


        if (response == null || !response.isSuccessful()) {
            log.error("{}, Nice 인증 처리 중 에러 발생 :: {}", this.getClass().getName(), "NICE 로 부터 수신한 응답이 존재하지 않거나 수신 실패");
            throw new MoimingAuthApiException(AUTH_NICE_RESPONSE_EXCEPTION);
        }

        try {

            NiceCryptoTokenRespDto responseBody = om.readValue(response.body().string(), NiceCryptoTokenRespDto.class);
            responseBody.validateResponse();

            NiceSecureHelper secureHelper = new NiceSecureHelper();
            NiceKeyParams keyParams = secureHelper.getNiceKeyParams(reqDtim, reqNo, responseBody.getDataBody().getTokenVal());

            String reqData = buildReqData(reqNo, responseBody.getDataBody().getSiteCode());
            String encData = secureHelper.getSecureEncData(reqData, keyParams.getKey(), keyParams.getIv());
            String integrityValue = secureHelper.getSecureIntegrityValue(encData, keyParams.getHmacKey());

            return new NiceStandardFormParamsDto(responseBody.getDataBody().getTokenVersionId(), encData, integrityValue);

        } catch (IOException | NullPointerException exception) {

            log.error("{}, Nice 인증 처리 중 에러 발생 :: {}", this.getClass().getName(), "JSON 활용 중 Exception, 혹은 인증 처리 중 Null Exception 발생");
            throw new MoimingAuthApiException(AUTH_COMMON_JSON_IMPL_FAIL);
        }

    }


    /*
     굳이 Request 객체 하나 더 만들지 말고, JSON 경로가 하나이므로 그냥 String 으로 만든다
     추후 receiveData 필요시 넣을 수 있다
     */
    private String buildReqData(String reqNo, String siteCode) {

        StringBuilder sbuilder = new StringBuilder();
        String returnUrl = AppUrlPath.PATH_AUTH_API_NICE_RETURN_URL;

        sbuilder.append("{");
        sbuilder.append("\"returnurl\"");
        sbuilder.append(":\"");
        sbuilder.append(returnUrl);
        sbuilder.append("\",");
        sbuilder.append("\"sitecode\"");
        sbuilder.append(":\"");
        sbuilder.append(siteCode);
        sbuilder.append("\",");
        sbuilder.append("\"requestno\"");
        sbuilder.append(":\"");
        sbuilder.append(reqNo);
        sbuilder.append("\",");
        sbuilder.append("\"authtype\":\"M\",\"methodtype\":\"get\"");
        sbuilder.append("}");

        return sbuilder.toString();
    }


    private String createReqDtim() {
        Date curDtim = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        return formatter.format(curDtim);
    }

}
