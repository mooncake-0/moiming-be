package com.peoplein.moiming.controller;

import com.peoplein.moiming.config.AppUrlPath;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.response.AuthRespDto;
import com.peoplein.moiming.service.NiceService;
import com.peoplein.moiming.service.util.nice.NiceParamsDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.service.util.nice.NiceParamsDto.*;

@Slf4j
@ApiIgnore
@RequiredArgsConstructor
public class NiceController {


    private final NiceService niceService;


    @ApiOperation("NICE 표준창 호출을 위한 보안 Params 요청")
    @ApiResponses({
            @ApiResponse(code = 200, message = "보안 Params 전달 성공"),
            @ApiResponse(code = 400, message = "보안 Params 전달 실패, ERR MSG 확인")
    })
    @GetMapping(PATH_AUTH_API_NICE_AUTHORIZATION)
    public String getNiceReqParams(Model model) {

        NiceStandardFormParamsDto niceReqParams = niceService.getNiceReqParams();

        model.addAttribute("token_version_id", niceReqParams.getTokenVersionId());
        model.addAttribute("enc_data", niceReqParams.getEncData());
        model.addAttribute("integrity_value", niceReqParams.getIntegrityValue());

        return "call_nice";
    }


    // 서버 단만 알면 됨
    @ApiIgnore
    @ResponseBody
    @GetMapping(PATH_AUTH_API_NICE_RETURN_URL)
    public String niceReturUrl(@RequestParam(required = false, value = "token_version_id") String tokenVersionId
            , @RequestParam(required = false, value = "enc_data") String encData
            , @RequestParam(required = false, value = "integrity_value") String integrityValue) {

        if (!StringUtils.hasText(tokenVersionId) || !StringUtils.hasText(encData) || !StringUtils.hasText(integrityValue)) {
            throw new MoimingApiException(ExceptionValue.COMMON_INVALID_REQUEST_PARAM);
        }

        return "";
    }


}

