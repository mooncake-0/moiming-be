package com.peoplein.moiming.service.util.nice;

import com.peoplein.moiming.exception.MoimingAuthApiException;
import com.peoplein.moiming.security.exception.AuthExceptionValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import static com.peoplein.moiming.security.exception.AuthExceptionValue.*;

@Slf4j
public class NiceParamsDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NiceKeyParams {
        private String key;
        private String hmacKey;
        private String iv;

        public NiceKeyParams(String key, String hmacKey, String iv) {
            if (!StringUtils.hasText(key) || !StringUtils.hasText(hmacKey) || !StringUtils.hasText(iv)) {
                log.error("{}, NICE KEY Parameter 중 Null 값이 전달 :: {}", "NiceParamsDto.NiceKeyParams 생성자", AUTH_COMMON_INVALID_PARAM_NULL.getErrMsg());
                throw new MoimingAuthApiException(AUTH_COMMON_INVALID_PARAM_NULL);
            }
            this.key = key;
            this.hmacKey = hmacKey;
            this.iv = iv;
        }
    }


    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NiceStandardFormParamsDto {

        private String tokenVersionId;
        private String encData;
        private String integrityValue;
        public NiceStandardFormParamsDto(String tokenVersionId, String encData, String integrityValue) {
            if (!StringUtils.hasText(tokenVersionId) || !StringUtils.hasText(encData) || !StringUtils.hasText(integrityValue)) {
                log.error("{}, NICE Standard Form Parameter 중 Null 값이 전달 :: {}", "NiceParamsDto.NiceStandardFormParamsDto 생성자", AUTH_COMMON_INVALID_PARAM_NULL.getErrMsg());
                throw new MoimingAuthApiException(AUTH_COMMON_INVALID_PARAM_NULL);
            }
            this.tokenVersionId = tokenVersionId;
            this.encData = encData;
            this.integrityValue = integrityValue;
        }
    }

}
