package com.peoplein.moiming.service.util.sms;

import com.peoplein.moiming.domain.SmsVerification;
import okhttp3.Request;

public interface SmsRequestBuilder {
    Request getHttpRequest(SmsVerification verification);
}
