package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.domain.RuleJoinDto;
import com.peoplein.moiming.model.dto.domain.RulePersistDto;
import com.peoplein.moiming.model.dto.request.RuleRequestDto;
import com.peoplein.moiming.service.MoimRulesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Moim 규칙 관리 관련")
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_MOIM_VER + NetworkSetting.API_MOIM + NetworkSetting.API_MOIM_RULES)
public class MoimRulesController {
    private final MoimRulesService moimRulesService;

    /*
     Join Rule 생성
     */
    @PostMapping("/join/create")
    public ResponseModel<RuleJoinDto> createJoinRule(@RequestBody RuleRequestDto ruleRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseModel.createResponse(moimRulesService.createJoinRule(ruleRequestDto, curMember));
    }

    /*
     Persist Rule 생성
     */
    @PostMapping("/persist/create")
    public ResponseModel<RulePersistDto> createPersistRule(@RequestBody RuleRequestDto ruleRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseModel.createResponse(moimRulesService.createPersistRule(ruleRequestDto, curMember));
    }

    /*
     Join Rule 조회
     */
    @GetMapping("/join/{moimId}")
    public ResponseModel<RuleJoinDto> getJoinRule(@PathVariable(name = "moimId") Long moimId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseModel.createResponse(moimRulesService.getJoinRule(moimId, curMember));
    }

    /*
     Persist Rule 조회
     */
    @GetMapping("/persist/{moimId}")
    public ResponseModel<RulePersistDto> getPersistRule(@PathVariable(name = "moimId") Long moimId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseModel.createResponse(moimRulesService.getPersistRule(moimId, curMember));
    }

    /*
     Join Rule 수정
     기존 값도 같이 담아야 함
     */
    @PatchMapping("/join/update")
    public ResponseModel<RuleJoinDto> changeJoinRule(@RequestBody RuleRequestDto ruleRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseModel.createResponse(moimRulesService.changeJoinRule(ruleRequestDto, curMember));
    }

    /*
     Persist Rule 수정
     */
    @PatchMapping("/persist/update")
    public ResponseModel<RulePersistDto> changePersistRule(@RequestBody RuleRequestDto ruleRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseModel.createResponse(moimRulesService.changePersistRule(ruleRequestDto, curMember));
    }

    /*
     Join Rule 삭제
     */
    @DeleteMapping("/join/{moimId}")
    public ResponseModel<String> deleteJoinRule(@PathVariable(name = "moimId") Long moimId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        moimRulesService.deleteJoinRule(moimId, curMember);
        return ResponseModel.createResponse("OK");
    }

    /*
     Persist Rule 삭제
     */
    @DeleteMapping("/persist/{moimId}")
    public ResponseModel<String> deletePersistRule(@PathVariable(name = "moimId") Long moimId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        moimRulesService.deletePersistRule(moimId, curMember);
        return ResponseModel.createResponse("OK");
    }

}