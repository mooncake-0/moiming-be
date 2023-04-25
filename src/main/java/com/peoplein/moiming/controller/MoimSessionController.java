package com.peoplein.moiming.controller;


import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.domain.MoimSessionDto;
import com.peoplein.moiming.model.dto.request.MoimSessionRequestDto;
import com.peoplein.moiming.model.dto.response.MoimSessionResponseDto;
import com.peoplein.moiming.service.MoimSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     모임 내 모든 정산활동 기본 정보 조회 - moim/session?moimId={}
     */
    @GetMapping("")
    public List<MoimSessionDto> getAllMoimSessions(@RequestParam(name = "moimId") Long moimId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return moimSessionService.getAllMoimSessions(moimId, curMember);
    }

    /*s
     특정 정산활동 세부조회 - moim/session/{sessionId}
     */
    @GetMapping("/{sessionId}")
    public MoimSessionResponseDto getMoimSession(@PathVariable(name = "sessionId") Long sessionId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return moimSessionService.getMoimSession(sessionId, curMember);
    }

    /*
     정산활동 수정
     */
    @PatchMapping("/update")
    public void updateMoimSession(@RequestBody MoimSessionRequestDto moimSessionRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        moimSessionService.updateMoimSession(moimSessionRequestDto, curMember);
    }

    /*
     정산활동 삭제
     */
    @DeleteMapping("/{sessionId}")
    public ResponseModel<String> deleteMoimSession(@PathVariable(name = "sessionId") Long sessionId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        moimSessionService.deleteMoimSession(sessionId, curMember);
        return ResponseModel.createResponse("OK");
    }


    /*
     FCM : 송금 요청
     */

}
