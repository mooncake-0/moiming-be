package com.peoplein.moiming.controller;


import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.dto.domain.MemberSessionLinkerDto;
import com.peoplein.moiming.model.dto.domain.MoimSessionDto;
import com.peoplein.moiming.model.dto.request.MemberSessionStateRequestDto;
import com.peoplein.moiming.model.dto.request.MoimSessionRequestDto;
import com.peoplein.moiming.model.dto.response.MoimSessionResponseDto;
import com.peoplein.moiming.service.MoimSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> createMoimSession(@RequestBody MoimSessionRequestDto moimSessionRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MoimSessionResponseDto moimSessionResponseDto = moimSessionService.createMoimSession(moimSessionRequestDto, curMember);
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(HttpStatus.CREATED, "생성완료", moimSessionResponseDto);
    }

    /*
     모임 내 모든 정산활동 기본 정보 조회 - moim/session?moimId={}
     */
    @GetMapping("")
    public ResponseEntity<?> getAllMoimSessions(@RequestParam(name = "moimId") Long moimId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(moimSessionService.getAllMoimSessions(moimId, curMember));
    }

    /*s
     특정 정산활동 세부조회 - moim/session/{sessionId}
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getMoimSession(@PathVariable(name = "sessionId") Long sessionId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return moimSessionService.getMoimSession(sessionId, curMember);
    }

    /*
     정산활동 수정
     */
    // Patch 가 아닌 PUT 으로 업데이트 진행
    @PutMapping("/update")
    public ResponseEntity<?>updateMoimSession(@RequestBody MoimSessionRequestDto moimSessionRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MoimSessionResponseDto moimSessionResponseDto = moimSessionService.updateMoimSession(moimSessionRequestDto, curMember);
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(moimSessionResponseDto);
    }

    /*
     정산활동 삭제
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> deleteMoimSession(@PathVariable(name = "sessionId") Long sessionId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        moimSessionService.deleteMoimSession(sessionId, curMember);
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse("OK");
    }

    /*
     정산활동 멤버 상태변경
     */
    @PatchMapping("/{sessionId}/member/{memberId}/status")
    public ResponseEntity<MemberSessionLinkerDto> changeSessionMemberStatus(@PathVariable(name = "sessionId") Long sessionId
            , @PathVariable(name = "memberId") Long memberId
            , @RequestParam(name = "moimId") Long moimId
            , @RequestBody MemberSessionStateRequestDto memberSessionStateRequestDto) {

        // 내가 나에 대한 변경을 요청하는 것일 수도, 관리자가 하는 것일 수도 있음
        // 일단은 "완료" / "미완료" 만 변경할 수 있으므로 only 관리자일 뿐인듯
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MemberSessionLinkerDto responseDto = moimSessionService.changeSessionMemberStatus(moimId, sessionId, memberId, memberSessionStateRequestDto, curMember);
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(responseDto);
    }

}