package com.peoplein.moiming.controller;

import com.peoplein.moiming.config.AppUrlPath;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.domain.MoimPostDto;
import com.peoplein.moiming.model.dto.request_b.MoimPostRequestDto;
import com.peoplein.moiming.service.MoimPostService;
import com.peoplein.moiming.service.PostCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppUrlPath.API_SERVER + AppUrlPath.API_MOIM_VER + AppUrlPath.API_MOIM + AppUrlPath.API_MOIM_POST)
public class MoimPostController {

    private final MoimPostService moimPostService;
    private final PostCommentService postCommentService;


    /*
     게시물 생성 요청
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPost(@RequestBody MoimPostRequestDto moimPostRequestDto
            , List<MultipartFile> file) {

        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        /*
         TODO : 전송받는 이미지들은 List<MultipartFile> 에 담겨져 올 것이다.
                해당 파일들을 S3 에 저장하는 작업을 진행 후 URL 을 받아오고, 그 과정 이후
                MultipartFile 을 다시 사용 + 받아온 url 을 통해서 PostFile Entity 를 만든다
                그리고 게시물과의 연관관계를 매핑해준다
         */
        MoimPostDto responseData = moimPostService.createPost(moimPostRequestDto, curMember);
        return new ResponseEntity<>(ResponseBodyDto.createResponse(1, "게시물 생성 완료", responseData), HttpStatus.CREATED);
    }


    // 해당 모임의 모든 게사물들에 대한 일반 조회 정보를 전달한다 (사진정보, 댓글정보 X)
    @GetMapping("")
    public ResponseEntity<?> viewAllMoimPost(@RequestParam(name = "moimId") Long moimId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<MoimPostDto> responseData = moimPostService.viewAllMoimPost(moimId, curMember);
        return ResponseEntity.ok().body(ResponseBodyDto.createResponse(1, "모든 게시물 조회 완료", responseData));
    }


    // 특정 게시물 일반조회 : 모든 정보를 조회한다 (사진정보, 댓글정보 포함)
    @GetMapping("/{moimPostId}")
    public ResponseEntity<?> getMoimPostData(@PathVariable(name = "moimPostId") Long moimPostId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MoimPostDto responseData = moimPostService.getMoimPostData(moimPostId, curMember);
        return ResponseEntity.ok().body(ResponseBodyDto.createResponse(1, "게시물 일반 조회 완료 ", responseData));
    }

    /*
     특정 게시물 수정
     */
    @PatchMapping("/update")
    public ResponseEntity<?> updatePost(@RequestBody MoimPostRequestDto moimPostRequestDto, List<MultipartFile> file) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MoimPostDto responseData = moimPostService.updatePost(moimPostRequestDto, curMember);
        return ResponseEntity.ok().body(ResponseBodyDto.createResponse(1, "게시물 정보 수정 완료", responseData));
    }


    /*
     특정 게시물 삭제
     */
    @DeleteMapping("/{moimPostId}")
    public ResponseEntity<?> deletePost(@PathVariable(name = "moimPostId") Long moimPostId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        moimPostService.deletePost(moimPostId, curMember);
        return ResponseEntity.ok().body(ResponseBodyDto.createResponse(1, "게시물 삭제 완료", null));
    }

}