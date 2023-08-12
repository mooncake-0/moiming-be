package com.peoplein.moiming.controller;


import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.request.MoimRequestDto;
import com.peoplein.moiming.model.dto.response.MoimResponseDto;
import com.peoplein.moiming.service.MoimService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Moim(모임) 관련")
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_MOIM_VER + NetworkSetting.API_MOIM)
public class MoimController {

    private final MoimService moimService;

    /*
     모임 생성 요청 수신
     */
    @PostMapping("/create")
    public ResponseModel<MoimResponseDto> createMoim(@RequestBody MoimRequestDto requestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MoimResponseDto moimResponseDto = moimService.createMoim(curMember, requestDto);
        return ResponseModel.createResponse(HttpStatus.CREATED, "생성", moimResponseDto);
    }

    // TODO :: 현재 유저의 구독권 여부에 따라서 RULE_JOIN 을 형성할 수 있을지 여부를 판별한다
    //         쓸데없이 Transactional 에 들어가는 것을 방지할 수 있도록 RuleJoin 이 있을시 Role 을 여기서 1차 판단해준다


    /*
     현 유저가 속한 모든 모임 기본 정보 영역 조회
     */
    @GetMapping("/viewMemberMoim")
    public ResponseModel<List<MoimResponseDto>> viewMemberMoim() {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return moimService.viewMemberMoim(curMember);
    }


    /*
     특정 Id 의 모임 조회
     */
    @GetMapping("/{moimId}")
    public ResponseModel<MoimResponseDto> getMoim(@PathVariable(name = "moimId") Long moimId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MoimResponseDto moimResponseDto = moimService.getMoim(moimId, curMember);
        return ResponseModel.createResponse(HttpStatus.OK, "조회완료", moimResponseDto);
    }

    /*
     모임 기본 정보 수정
     */
    @PatchMapping("/update")
    public ResponseModel<MoimResponseDto> updateMoim(@RequestBody MoimRequestDto moimRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MoimResponseDto moimResponseDto = moimService.updateMoim(moimRequestDto, curMember);
        return ResponseModel.createResponse(HttpStatus.OK, "수정완료", moimResponseDto);
    }

    /*
     모임 삭제
     */
    @DeleteMapping("/{moimId}")
    public ResponseModel<String> deleteMoim(@PathVariable(name = "moimId") Long moimId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        moimService.deleteMoim(moimId, curMember);
        return ResponseModel.createResponse(HttpStatus.OK, "삭제완료", null);
    }

}