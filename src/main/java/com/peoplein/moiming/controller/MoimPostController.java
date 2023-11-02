package com.peoplein.moiming.controller;

import com.peoplein.moiming.config.AppUrlPath;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.response.MoimPostRespDto;
import com.peoplein.moiming.security.domain.SecurityMember;
import com.peoplein.moiming.service.MoimPostService;
import com.peoplein.moiming.service.PostCommentService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.request.MoimPostReqDto.*;
import static com.peoplein.moiming.model.dto.response.MoimPostRespDto.*;

@Api(tags = "모임 게시물 관련")
@RestController
@RequiredArgsConstructor
@RequestMapping(API_SERVER + API_MOIM_VER + API_MOIM)
public class MoimPostController {

    private final MoimPostService moimPostService;


    @ApiOperation("모임 게시물 생성")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "유저 게시물 생성 성공"),
            @ApiResponse(code = 400, message = "유저 게시물 생성 실패, ERR MSG 확인")
    })
    @PostMapping(API_MOIM_POST + "/create")
    public ResponseEntity<?> createPost(@RequestBody @Valid MoimPostCreateReqDto requestDto
            , BindingResult br
            , List<MultipartFile> file
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        /*
         TODO : 전송받는 이미지들은 List<MultipartFile> 에 담겨져 올 것이다.
                해당 파일들을 S3 에 저장하는 작업을 진행 후 URL 을 받아오고, 그 과정 이후
                MultipartFile 을 다시 사용 + 받아온 url 을 통해서 PostFile Entity 를 만든다
                그리고 게시물과의 연관관계를 매핑해준다
         */
        moimPostService.createMoimPost(requestDto, principal.getMember());
        return ResponseEntity.ok(ResponseBodyDto.createResponse(1, "모임 게시물 생성 성공", null));

    }


    @ApiOperation("게시물 일반 조회 - 모임의 게시물 일반 조회 (기본 정보 응답), (최신 작성일 기준 내림차순, lastId 필요, 10개씩 전달됨)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "게시물 일반 조회 성공"),
            @ApiResponse(code = 400, message = "게시물 일반 조회 실패")
    })
    @GetMapping("/{moimId}" + API_MOIM_POST)
    public ResponseEntity<?> getMoimPosts(@PathVariable(name = "moimId") Long moimId
            , @RequestParam(required = false, value = "lastPostId") Long lastPostId
            , @RequestParam(required = false, value = "category") MoimPostCategory category
            , @RequestParam(required = false, defaultValue = "10") int limit
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {


        List<MoimPost> moimPosts = moimPostService.getMoimPosts(moimId, lastPostId, category, limit, principal.getMember());
        List<MoimPostViewRespDto> responseBody = moimPosts.stream().map(MoimPostViewRespDto::new).collect(Collectors.toList());

        return ResponseEntity.ok(ResponseBodyDto.createResponse(1, "모든 게시물 일반 조회 성공", responseBody));
    }


}