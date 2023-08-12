package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.domain.PostCommentDto;
import com.peoplein.moiming.model.dto.request.PostCommentRequestDto;
import com.peoplein.moiming.service.PostCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Moim 게시물 댓글 관련")
@RequiredArgsConstructor
@RequestMapping(NetworkSetting.API_SERVER + NetworkSetting.API_MOIM_VER + NetworkSetting.API_MOIM + NetworkSetting.API_MOIM_POST + NetworkSetting.API_MOIM_POST_COMMENT)
public class PostCommentController {

    private final PostCommentService postCommentService;

    /*
     댓글 생성 요청 : 성공시 생성된 댓글에 대한 정보를 전달한다, 요청시 MoimPostId 와 댓글 내용 전달
    */
    @PostMapping("/create")
    public ResponseModel<PostCommentDto> createPostComment(@RequestBody PostCommentRequestDto postCommentRequestDto) {

        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(postCommentService.createPostComment(postCommentRequestDto, curMember));
    }


    /*
     댓글 수정 :성공시 수정된 댓글에 대한 정보를 전달한다, 요청시 CommentId 와 수정 내용 전달
     */
    @PatchMapping("/update")
    public ResponseModel<PostCommentDto> updatePostComment(@RequestBody PostCommentRequestDto postCommentRequestDto) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse(postCommentService.updatePostComment(postCommentRequestDto, curMember));
    }


    /*
     댓글 삭제 : 성공시 생성된 댓글을 삭제후 OK를 전달한다
     */
    @DeleteMapping("/{commentId}")
    public ResponseModel<String> deletePostComment(@RequestParam(name = "commentId") Long commentId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        postCommentService.deletePostComment(commentId, curMember);
        // TODO :: ResponseEntity 로 변환 예정
        return null;
//        return ResponseModel.createResponse("OK");
    }
}