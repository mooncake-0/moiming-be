package com.peoplein.moiming.controller;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.model.dto.request_b.RuleRequestDto;
import com.peoplein.moiming.service.MoimRulesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
@RequiredArgsConstructor
@RequestMapping("/moimRule")
public class MoimRulesController {
    private final MoimRulesService moimRulesService;

    /*
     Join Rule 생성
     */
    @PostMapping("/join/create")
    public ResponseEntity<?> createJoinRule(@RequestBody RuleRequestDto ruleRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(moimRulesService.createJoinRule(ruleRequestDto, curMember));
    }


    /*
     Join Rule 조회
     */
    @GetMapping("/join/{moimId}")
    public ResponseEntity<?> getJoinRule(@PathVariable(name = "moimId") Long moimId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(moimRulesService.getJoinRule(moimId, curMember));
    }


    /*
     Join Rule 수정
     기존 값도 같이 담아야 함
     */
    @PatchMapping("/join/update")
    public ResponseEntity<?> changeJoinRule(@RequestBody RuleRequestDto ruleRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(moimRulesService.changeJoinRule(ruleRequestDto, curMember));
    }


    /*
     Join Rule 삭제
     */
    @DeleteMapping("/join/{moimId}")
    public ResponseEntity<?> deleteJoinRule(@PathVariable(name = "moimId") Long moimId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        moimRulesService.deleteJoinRule(moimId, curMember);
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse("OK");
    }


}