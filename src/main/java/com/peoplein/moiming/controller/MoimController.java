package com.peoplein.moiming.controller;


import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.request_b.MoimRequestDto;
import com.peoplein.moiming.model.dto.response_b.MoimResponseDto;
import com.peoplein.moiming.service.MoimService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_MOIM_VER + NetworkSetting.API_MOIM)
public class MoimController {

    private final MoimService moimService;

    /*
     모임 생성 요청 수신
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @PostMapping("/create")
    public ResponseEntity<?> createMoim(@RequestBody MoimRequestDto requestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MoimResponseDto moimResponseDto = moimService.createMoim(curMember, requestDto);
        return new ResponseEntity<>(ResponseBodyDto.createResponse(1, "모임 생성 성공", moimResponseDto), HttpStatus.CREATED);
    }

    // TODO :: 현재 유저의 구독권 여부에 따라서 RULE_JOIN 을 형성할 수 있을지 여부를 판별한다
    //         쓸데없이 Transactional 에 들어가는 것을 방지할 수 있도록 RuleJoin 이 있을시 Role 을 여기서 1차 판단해준다


    /*
     현 유저가 속한 모든 모임 기본 정보 영역 조회
     */
    @GetMapping("/viewMemberMoim")
    public ResponseEntity<?> viewMemberMoim() {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<MoimResponseDto> responseData = moimService.viewMemberMoim(curMember);
        return ResponseEntity.ok().body(ResponseBodyDto.createResponse(1, "유저 모임 조회 완료", responseData));
    }


    /*
     특정 Id 의 모임 조회
     */
    @GetMapping("/{moimId}")
    public ResponseEntity<?> getMoim(@PathVariable(name = "moimId") Long moimId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MoimResponseDto responseDto = moimService.getMoim(moimId, curMember);
        return ResponseEntity.ok().body(ResponseBodyDto.createResponse(1, "모임 일반 조회 완료", responseDto));
    }

    /*
     모임 기본 정보 수정
     */
    @PatchMapping("/update")
    public ResponseEntity<?> updateMoim(@RequestBody MoimRequestDto moimRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MoimResponseDto responseDto = moimService.updateMoim(moimRequestDto, curMember);
        return ResponseEntity.ok().body(ResponseBodyDto.createResponse(1, "모임 정보 수정 완료", responseDto));
    }

    /*
     모임 삭제
     */
    @DeleteMapping("/{moimId}")
    public ResponseEntity<?> deleteMoim(@PathVariable(name = "moimId") Long moimId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        moimService.deleteMoim(moimId, curMember);
        return ResponseEntity.ok().body(ResponseBodyDto.createResponse(1, "모임 삭제 완료", null));
    }

}