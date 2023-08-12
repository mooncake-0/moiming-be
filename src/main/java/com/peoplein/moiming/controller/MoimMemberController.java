package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MemberMoimLinker;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.domain.MoimMemberInfoDto;
import com.peoplein.moiming.model.dto.domain.MyMoimLinkerDto;
import com.peoplein.moiming.model.dto.request.MoimJoinRequestDto;
import com.peoplein.moiming.model.dto.request.MoimMemberActionRequestDto;
import com.peoplein.moiming.service.MoimMemberService;
import com.peoplein.moiming.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Moim 내부 회원 관리 관련")
@RequiredArgsConstructor
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_MOIM_VER + NetworkSetting.API_MOIM + NetworkSetting.API_MEMBER)
public class MoimMemberController {

    private final MoimMemberService moimMemberService;

    /*
     모임 내 모든 회원 및 상태 조회
     */
    @GetMapping("/viewMoimMember/{moimId}")
    public ResponseModel<List<MoimMemberInfoDto>> viewMoimMember(@PathVariable(name = "moimId") Long moimId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<MoimMemberInfoDto> moimMemberInfoDto = moimMemberService.viewMoimMember(moimId, curMember);
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(moimMemberInfoDto);
    }

    /*
     Join 요청하기 (Rule Join 판별)
     */
    @PostMapping("/requestJoin")
    public ResponseModel<MyMoimLinkerDto> requestJoin(@RequestBody MoimJoinRequestDto moimJoinRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MemberMoimLinker memberMoimLinker = moimMemberService.requestJoin(moimJoinRequestDto, curMember);
        MyMoimLinkerDto myMoimLinkerDto = new MyMoimLinkerDto(memberMoimLinker);
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(myMoimLinkerDto);
    }

    /*
     MemberState 이 WAIT 인 회원의 Join Request 를 처리한다 - 모임장, 운영진의 요청
     */
    @PatchMapping("/decideJoin")
    public ResponseModel<MoimMemberInfoDto> decideJoin(@RequestBody MoimMemberActionRequestDto moimMemberActionRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MemberMoimLinker memberMoimLinker = moimMemberService.decideJoin(moimMemberActionRequestDto, curMember);
        MoimMemberInfoDto moimMemberInfoDto = new MoimMemberInfoDto(memberMoimLinker);
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(moimMemberInfoDto);
    }

    /*
     모임 나가기 or 강퇴하기
     */
    @PatchMapping("/exitMoim")
    public ResponseModel<MoimMemberInfoDto> exitMoim(@RequestBody MoimMemberActionRequestDto moimMemberActionRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MoimMemberInfoDto moimMemberInfoDto = moimMemberService.exitMoim(moimMemberActionRequestDto, curMember);
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(moimMemberInfoDto);
    }

    /*
     모임 내 권한 임명하기 (동일한 Request Model 을 받으나, 함유 정보가 다름)
     */
    @PatchMapping("/changeRole")
    public ResponseModel<MoimMemberInfoDto> changeRole(@RequestBody MoimMemberActionRequestDto moimMemberActionRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MoimMemberInfoDto moimMemberInfoDto = moimMemberService.changeRole(moimMemberActionRequestDto, curMember);
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(moimMemberInfoDto);
    }


}