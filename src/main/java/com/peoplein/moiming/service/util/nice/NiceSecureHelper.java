package com.peoplein.moiming.service.util.nice;

import com.peoplein.moiming.exception.MoimingAuthApiException;
import com.peoplein.moiming.security.exception.AuthExceptionValue;
import com.peoplein.moiming.service.util.StringEncryptor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static com.peoplein.moiming.service.util.nice.NiceParamsDto.*;

@Slf4j
@Component
public class NiceSecureHelper {

    public NiceKeyParams getNiceKeyParams(String reqDtim, String reqNo, String tokenVal) {
        try {
            String resultValue = reqDtim.trim() + reqNo.trim() + tokenVal.trim();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(resultValue.getBytes());
            byte[] arrHashValue = md.digest();

            String encodedValue = Base64.getEncoder().encodeToString(arrHashValue);
            String key = encodedValue.substring(0, 16);
            String hmacKey = encodedValue.substring(0, 32);
            String iv = encodedValue.substring(encodedValue.length() - 16);

            return new NiceKeyParams(key, hmacKey, iv);

        } catch (NoSuchAlgorithmException exception) {
            log.error("{}, Nice 암호화 토큰을 통한 요청 정보 암호화 key 생성 중 오류:: {}", this.getClass().getName(), exception.getMessage());
            throw new MoimingAuthApiException(AuthExceptionValue.AUTH_NICE_SECURE_TRANSFER_EXCEPTION, exception);

        }
    }


    /*
     요청하려는 정보를 암호화한다
     */
    public String getSecureEncData(String reqData, String key, String iv) {

        try {
            SecretKey secureKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(iv.getBytes()));
            byte[] encryptedBytes = c.doFinal(reqData.trim().getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException exception) {
            log.error("{}, HmacSHA256 Encrypting 도중 전달된 Key 혹은 Algorithm 오류 발생 :: {}", this.getClass().getName(), exception.getMessage());
            throw new MoimingAuthApiException(AuthExceptionValue.AUTH_NICE_SECURE_TRANSFER_EXCEPTION, exception);
        }

    }


    /*
     암호화한 정보를 이중 암호화하여 전달하여 NICE 측에서 무결성을 확인한다
     */
    public String getSecureIntegrityValue(String encData, String hmacKey) {
        StringEncryptor encryptor = new StringEncryptor();
        byte[] encryptedBytes = encryptor.encryptByAlgo(hmacKey, encData);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

}
