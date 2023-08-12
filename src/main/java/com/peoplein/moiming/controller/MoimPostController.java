package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.domain.MoimPostDto;
import com.peoplein.moiming.model.dto.request.MoimPostRequestDto;
import com.peoplein.moiming.service.MoimPostService;
import com.peoplein.moiming.service.PostCommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Moim 게시물 관련")
@RestController
@RequiredArgsConstructor
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_MOIM_VER + NetworkSetting.API_MOIM + NetworkSetting.API_MOIM_POST)
public class MoimPostController {

    private final MoimPostService moimPostService;
    private final PostCommentService postCommentService;


    /*
     게시물 생성 요청
     */
    @PostMapping("/create")
    public ResponseModel<MoimPostDto> createPost(@RequestBody MoimPostRequestDto moimPostRequestDto
            , List<MultipartFile> file) {

        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        /*
         TODO : 전송받는 이미지들은 List<MultipartFile> 에 담겨져 올 것이다.
                해당 파일들을 S3 에 저장하는 작업을 진행 후 URL 을 받아오고, 그 과정 이후
                MultipartFile 을 다시 사용 + 받아온 url 을 통해서 PostFile Entity 를 만든다
                그리고 게시물과의 연관관계를 매핑해준다
         */

        return ResponseModel.createResponse(HttpStatus.CREATED, "생성완료", moimPostService.createPost(moimPostRequestDto, curMember));
    }


    // 해당 모임의 모든 게사물들에 대한 일반 조회 정보를 전달한다 (사진정보, 댓글정보 X)
    @GetMapping("")
    public ResponseModel<List<MoimPostDto>> viewAllMoimPost(@RequestParam(name = "moimId") Long moimId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseModel.createResponse(HttpStatus.OK, "조회완료", moimPostService.viewAllMoimPost(moimId, curMember));
    }


    // 특정 게시물 일반조회 : 모든 정보를 조회한다 (사진정보, 댓글정보 포함)
    @GetMapping("/{moimPostId}")
    public ResponseModel<MoimPostDto> getMoimPostData(@PathVariable(name = "moimPostId") Long moimPostId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseModel.createResponse(HttpStatus.OK, "조회완료", moimPostService.getMoimPostData(moimPostId, curMember));
    }

    /*
     특정 게시물 수정
     */
    @PatchMapping("/update")
    public ResponseModel<MoimPostDto> updatePost(@RequestBody MoimPostRequestDto moimPostRequestDto, List<MultipartFile> file) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MoimPostDto moimPostDto = moimPostService.updatePost(moimPostRequestDto, curMember);
        return ResponseModel.createResponse(HttpStatus.OK, "수정완료", moimPostDto);
    }


    /*
     특정 게시물 삭제
     */
    @DeleteMapping("/{moimPostId}")
    public ResponseModel<String> deletePost(@PathVariable(name = "moimPostId") Long moimPostId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        moimPostService.deletePost(moimPostId, curMember);
        return ResponseModel.createResponse(HttpStatus.OK, "삭제완료", null);
    }

}