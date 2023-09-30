package com.peoplein.moiming.controller;

import com.peoplein.moiming.config.AppUrlPath;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.dto.domain.PostCommentDto;
import com.peoplein.moiming.model.dto.request_b.PostCommentRequestDto;
import com.peoplein.moiming.service.PostCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(AppUrlPath.API_SERVER + AppUrlPath.API_MOIM_VER + AppUrlPath.API_MOIM + AppUrlPath.API_MOIM_POST + AppUrlPath.API_MOIM_POST_COMMENT)
public class PostCommentController {

    private final PostCommentService postCommentService;

    /*
     댓글 생성 요청 : 성공시 생성된 댓글에 대한 정보를 전달한다, 요청시 MoimPostId 와 댓글 내용 전달
    */
    @PostMapping("/create")
    public ResponseEntity<PostCommentDto> createPostComment(@RequestBody PostCommentRequestDto postCommentRequestDto) {

        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(postCommentService.createPostComment(postCommentRequestDto, curMember));
    }


    /*
     댓글 수정 :성공시 수정된 댓글에 대한 정보를 전달한다, 요청시 CommentId 와 수정 내용 전달
     */
    @PatchMapping("/update")
    public ResponseEntity<PostCommentDto> updatePostComment(@RequestBody PostCommentRequestDto postCommentRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(postCommentService.updatePostComment(postCommentRequestDto, curMember));
    }


    /*
     댓글 삭제 : 성공시 생성된 댓글을 삭제후 OK를 전달한다
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deletePostComment(@RequestParam(name = "commentId") Long commentId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        postCommentService.deletePostComment(commentId, curMember);
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse("OK");
    }
}