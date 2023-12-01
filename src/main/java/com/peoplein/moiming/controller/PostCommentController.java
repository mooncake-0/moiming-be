package com.peoplein.moiming.controller;

import com.peoplein.moiming.config.AppUrlPath;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.domain.PostCommentDto;
import com.peoplein.moiming.model.dto.request.PostCommentReqDto;
import com.peoplein.moiming.model.dto.request_b.PostCommentRequestDto;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.service.PostCommentService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.request.PostCommentReqDto.*;

@Api(tags = "모임 게시물 댓글 관련")
@RestController
@RequiredArgsConstructor
public class PostCommentController {

    private final PostCommentService postCommentService;

    // 생성 - POST :: api/v0/moim/post/comment - moimId, postId, 생성 내용, depth 등을 전달
    // 수정 - PATCH :: api/v0/moim/post/comment - commentId, 내용 변경
    // 삭제 - DELETE :: api/v0/moim/{moimId}/post/{postId}/commment/{commentId}
    // 읽기 - 일괄 POST 에서 진행할 것으로 보임 Post 에 현재 댓글 갯수 필요 ㅇㅇ

    /*
     댓글 생성 요청 : 댓글을 생성한다
    */
    @ApiOperation("게시물 댓글 달기")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "댓글 생성 성공"),
            @ApiResponse(code = 400, message = "댓글 생성 실패, ERR MSG 확인")
    })
    @PostMapping(PATH_POST_COMMENT_CREATE)
    public ResponseEntity<?> createComment(@RequestBody @Valid PostCommentCreateReqDto requestDto
            , BindingResult br
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        postCommentService.createComment(requestDto, principal.getMember());
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "댓글 생성 성공", null));
    }


    /*
     댓글 수정 :성공시 수정된 댓글에 대한 정보를 전달한다, 요청시 CommentId 와 수정 내용 전달
     */
    @ApiOperation("게시물 댓글 수정")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "댓글 수정 성공"),
            @ApiResponse(code = 400, message = "댓글 수정 실패, ERR MSG 확인")
    })
    @PatchMapping(PATH_POST_COMMENT_UPDATE)
    public ResponseEntity<?> updateComment(@RequestBody @Valid PostCommentUpdateReqDto requestDto
            , BindingResult br
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {


        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "댓글 수정 성공", null));
    }


    /*
     댓글 삭제 : 성공시 생성된 댓글을 삭제후 OK를 전달한다
     */
    @ApiOperation("게시물 댓글 삭제")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "댓글 수정 성공"),
            @ApiResponse(code = 400, message = "댓글 수정 실패, ERR MSG 확인")
    })
    @DeleteMapping(PATH_POST_COMMENT_DELETE)
    public ResponseEntity<?> deleteComment(@PathVariable(name = "moimId") Long moimId
            , @PathVariable(name = "moimPostId") Long moimPostId
            , @PathVariable(name = "postCommentId") Long postCommentId
            , BindingResult br
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        postCommentService.deleteComment(postCommentId, principal.getMember());
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "댓글 삭제 성공", null));
    }

}