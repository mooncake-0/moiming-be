package com.peoplein.moiming.controller;

import com.peoplein.moiming.config.AppUrlPath;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.request.MemberReqDto.*;
import static com.peoplein.moiming.model.dto.response.MemberRespDto.*;


/*
 SWAGGER 명시를 위해서 등재되어 있는 Controller (실제로 동작하지 않는다)
 Login 관련은 Security Filter 단에서 모두 처리가 된다
 */
@Api(tags = "로그인")
@RestController
public class LoginDocController {


    @ApiOperation("로그인")
    @ApiResponses({
            @ApiResponse(code = 200, message = "로그인 성공", response = MemberLoginRespDto.class,
                    responseHeaders = {@ResponseHeader(name = "Authorization", description = "Bearer {JWT ACCESS TOKEN}", response = String.class)}),
            @ApiResponse(code = 400, message = "로그인 실패, Err Msg 확인")
    })
    @PostMapping(PATH_AUTH_LOGIN)
    public void processLogin(@RequestBody MemberLoginReqDto request) {

    }
}