package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.dto.request.MoimReviewRequestDto;
import com.peoplein.moiming.model.dto.response.MoimReviewResponseDto;
import com.peoplein.moiming.model.dto.response.ReviewQuestionResponseDto;
import com.peoplein.moiming.service.MoimReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Moim 후기 관련")
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_MOIM_VER + NetworkSetting.API_MOIM + NetworkSetting.API_MOIM_REVIEW)
public class MoimReviewController {

    private final MoimReviewService moimReviewService;

    // TODO: DTO 들에 대한 Validation 진행 필요
    @PostMapping("/create")
    public ResponseEntity<?> createReview(@RequestBody MoimReviewRequestDto moimReviewRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(moimReviewService.createReview(moimReviewRequestDto, curMember));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReview(@PathVariable(name = "reviewId") Long reviewId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(moimReviewService.getReview(reviewId, curMember));
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateReview(@RequestBody MoimReviewRequestDto moimReviewRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return moimReviewService.updateReview(moimReviewRequestDto, curMember);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable(name = "reviewId") Long reviewId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        moimReviewService.deleteReview(reviewId, curMember);
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse("OK");
    }

    @GetMapping("")
    public ResponseEntity<?> viewAllMoimReview(@RequestParam(name = "moimId") Long moimId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(moimReviewService.viewAllMoimReview(moimId, curMember));
    }

    /*
    질문과 선택지를 모두 호출한다
     */
    @GetMapping("/questions")
    public ResponseEntity<?> getReviewQuestions() {
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(moimReviewService.getReviewQuestions());
    }
}