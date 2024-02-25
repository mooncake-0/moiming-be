package com.peoplein.moiming.service.util.nice.body;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.exception.MoimingAuthApiException;
import com.peoplein.moiming.security.exception.AuthExceptionValue;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class NiceBodyTemplate {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NiceCryptoTokenReqDto {

        private ReqHeader dataHeader;
        private ReqBody dataBody;

        public NiceCryptoTokenReqDto(String reqDtim, String reqNo) {
            this.dataHeader = new ReqHeader();
            this.dataBody = new ReqBody(reqDtim, reqNo);
        }

        @Getter
        public static class ReqHeader {
            @JsonProperty("CNTY_CD")
            private String cntyCd;

            public ReqHeader() {
                this.cntyCd = "ko";
            }
        }

        @Getter
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class ReqBody {

            @JsonProperty("req_dtim")
            private String reqDtim;
            @JsonProperty("req_no")
            private String reqNo; // 해당 요청의 고유값 (우리가 구분하기 위함)
            @JsonProperty("enc_mode")
            private String encMode;

            public ReqBody(String reqDtim, String reqNo) {
                this.reqDtim = reqDtim;
                this.reqNo = reqNo;
                this.encMode = "1";
            }
        }
    }


    @Getter
    @Setter
    @NoArgsConstructor
    public static class NiceCryptoTokenRespDto {

        private RespHeader dataHeader;
        private RespBody dataBody;

        @NoArgsConstructor
        @Getter
        @Setter
        public static class RespHeader {
            @JsonProperty("GW_RESLT_CD")
            private String gwResultCd;
            @JsonProperty("GW_RESLT_MSG")
            private String gwResultMsg;
        }

        @NoArgsConstructor
        @Getter
        @Setter
        public static class RespBody {
            @JsonProperty("rsp_cd")
            private String rspCd;
            @JsonProperty("site_code")
            private String siteCode;
            @JsonProperty("result_cd")
            private String resultCd;
            @JsonProperty("token_version_id")
            private String tokenVersionId;
            @JsonProperty("token_val")
            private String tokenVal;
            @JsonProperty("period")
            private Integer period;
        }


        public void validateResponse() {
            if (this.dataHeader == null || this.dataBody == null) {
                log.error("{}, NICE 암호화 Token 요청 실패 :: {}", this.getClass().getName(), "암호화 Token 응답에 dataHeader / dataBody 가 없습니다");
                throw new MoimingAuthApiException(AuthExceptionValue.AUTH_NICE_RESPONSE_EXCEPTION);
            }

            String gwResultCd = this.dataHeader.getGwResultCd();

            if (!gwResultCd.equals("1200")) {
                String rspCd = this.dataBody.getRspCd();
                log.error("{}, NICE 암호화 Token 요청 실패 :: {}, {}", this.getClass().getName(), "[GW_RESULT_CD : " + gwResultCd + "] 명세서로 에러 확인 필요", this.dataHeader.getGwResultMsg());
                log.error("{} :: {}", "Data Body 내 RespCD 값", rspCd);
                throw new MoimingAuthApiException(AuthExceptionValue.AUTH_NICE_RESPONSE_EXCEPTION);
            }
        }
    }
}
