package com.peoplein.moiming.controller;

import com.peoplein.moiming.NetworkSetting;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.model.ErrorResponse;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.domain.MoimPostDto;
import com.peoplein.moiming.model.dto.request.MoimPostRequestDto;
import com.peoplein.moiming.service.MoimPostService;
import com.peoplein.moiming.service.PostCommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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


    @Operation(summary = "게시물 생성 요청", description = "성공시 생성된 게시물에 대한 정보를 전달한다")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "게시물 생성 성공",
                            content = {
                                    @Content(mediaType = "application/json", schema = @Schema(implementation = MoimPostDto.class))
                            }),
                    @ApiResponse(responseCode = "400", description = "잘못된 변수 전달, 잘못된 JSON 형식",
                            content = {
                                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
                            }),
                    @ApiResponse(responseCode = "500", description = "내부 Null Pointer 발생, Response 형성 에러 발생 (Report Need)",
                            content = {
                                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
                            }),
            }
    )
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

        return ResponseModel.createResponse(moimPostService.createPost(moimPostRequestDto, curMember));
    }


    // 모임 모든 게시물 일반 조회
    @Operation(summary = "모임 모든 게시물 일반 조회", description = "해당 모임의 모든 게사물들에 대한 일반 조회 정보를 전달한다 (사진정보, 댓글정보 X)")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "모임 생성 성공",
                            content = {
                                    @Content(mediaType = "application/json", schema = @Schema(implementation = MoimPostDto.class))
                            }),
                    @ApiResponse(responseCode = "400", description = "잘못된 변수 전달, 잘못된 JSON 형식",
                            content = {
                                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
                            }),
                    @ApiResponse(responseCode = "500", description = "내부 Null Pointer 발생, Response 형성 에러 발생 (Report Need)",
                            content = {
                                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
                            }),
            }
    )
    @GetMapping("")
    public ResponseModel<List<MoimPostDto>> viewAllMoimPost(@RequestParam(name = "moimId") Long moimId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseModel.createResponse(moimPostService.viewAllMoimPost(moimId, curMember));
    }


    // 특정 게시물 전체 조회
    @Operation(summary = "게시물에 대한 전체 정보 조회", description = "게시물에 대한 모든 정보를 조회한다 (사진정보, 댓글정보 포함)")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "모임 생성 성공",
                            content = {
                                    @Content(mediaType = "application/json", schema = @Schema(implementation = MoimPostDto.class))
                            }),
                    @ApiResponse(responseCode = "400", description = "잘못된 변수 전달, 잘못된 JSON 형식",
                            content = {
                                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
                            }),
                    @ApiResponse(responseCode = "500", description = "내부 Null Pointer 발생, Response 형성 에러 발생 (Report Need)",
                            content = {
                                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
                            }),
            }
    )
    @GetMapping("/{moimPostId}")
    public ResponseModel<MoimPostDto> getMoimPostData(@PathVariable(name = "moimPostId") Long moimPostId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseModel.createResponse(moimPostService.getMoimPostData(moimPostId, curMember));
    }

    /*
     특정 게시물 수정
     */
    @PatchMapping("/update")
    public ResponseModel<MoimPostDto> updatePost(@RequestBody MoimPostRequestDto moimPostRequestDto, List<MultipartFile> file) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MoimPostDto moimPostDto = moimPostService.updatePost(moimPostRequestDto, curMember);
        return ResponseModel.createResponse(moimPostDto);
    }


    /*
     특정 게시물 삭제
     */
    @DeleteMapping("/{moimPostId}")
    public ResponseModel<String> deletePost(@PathVariable(name = "moimPostId") Long moimPostId) {
        Member curMember = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        moimPostService.deletePost(moimPostId, curMember);
        return ResponseModel.createResponse("OK");
    }
}
