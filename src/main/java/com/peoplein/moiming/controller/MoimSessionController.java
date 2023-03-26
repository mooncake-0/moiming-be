package com.peoplein.moiming.controller;


import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.request.MoimSessionRequestDto;
import com.peoplein.moiming.model.dto.response.MoimSessionResponseDto;
import com.peoplein.moiming.service.MoimSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Moim 정산 관련")
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_MOIM_VER + NetworkSetting.API_MOIM + NetworkSetting.API_MOIM_SESSION)
public class MoimSessionController {

    private final MoimSessionService moimSessionService;

    /*
     정산활동 생성 - create
     */
    @PostMapping("/create")
    public ResponseModel<MoimSessionResponseDto> createMoimSession(@RequestBody MoimSessionRequestDto moimSessionRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MoimSessionResponseDto moimSessionResponseDto = moimSessionService.createMoimSession(moimSessionRequestDto, curMember);
        return ResponseModel.createResponse(moimSessionResponseDto);
    }

    /*
     모임 내 모든 정산활동 기본 정보 조회 - moim/session/moimId={}
     */

    /*
     특정 정산활동 세부조회 - moim/session/{sessionId}
     */

    /*
     정산활동 수정
     */

    /*
     정산활동 삭제
     */

    /*

     */


    /*
     FCM : 송금 요청
     */

}
