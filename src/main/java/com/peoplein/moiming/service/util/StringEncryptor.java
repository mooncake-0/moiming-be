package com.peoplein.moiming.service.util;

import com.peoplein.moiming.exception.MoimingAuthApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static com.peoplein.moiming.security.exception.AuthExceptionValue.AUTH_COMMON_HMAC_ENCRYPTING_FAIL;


@Slf4j
public class StringEncryptor {

    private final String SIGNATURE_ALGO = "HmacSHA256";

    public byte[] encryptByAlgo(String secretKey, String value) {

        try {
            Mac hmacSha256 = Mac.getInstance(SIGNATURE_ALGO);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), SIGNATURE_ALGO);
            hmacSha256.init(secretKeySpec);

            return hmacSha256.doFinal(value.getBytes());
        } catch (InvalidKeyException | NoSuchAlgorithmException exception) {
            log.error("{}, HmacSHA256 Encrypting 도중 전달된 Key 혹은 Algorithm 오류 발생 :: {}", this.getClass().getName(), exception.getMessage());
            throw new MoimingAuthApiException(AUTH_COMMON_HMAC_ENCRYPTING_FAIL, exception);
        }
    }

}
