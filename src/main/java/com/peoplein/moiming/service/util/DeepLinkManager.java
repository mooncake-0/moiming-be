package com.peoplein.moiming.service.util;

import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import org.springframework.stereotype.Component;

@Component
public class DeepLinkManager {

    public String createDeepLink(Long topCategoryId, Long subCategoryId) {
        if (topCategoryId == null) {
            throw new MoimingApiException(ExceptionValue.COMMON_INVALID_PARAM);
        }
        String PREFIX = "moiming://";
        String anw = PREFIX + "topCategory/" + topCategoryId;
        if (subCategoryId != null && subCategoryId != 0L) {
            anw += "subCategory/" + subCategoryId;
        }
        return anw;
    }
}