package com.peoplein.moiming.controller;


import com.peoplein.moiming.NetworkSetting;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Moim 정산 관련")
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_MOIM_VER + NetworkSetting.API_MOIM + NetworkSetting.API_MOIM_SESSION)
public class MoimSessionController {

    /*
     정산활동 생성 - create
     */

    /*
     모임 내 모든 정산활동 조회 - moim/session/moimId={}
     */

    /*
     특정 정산활동 세부조회 - moim/session/{sessionId}
     */

    /*

     */

}
