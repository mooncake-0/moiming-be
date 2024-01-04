package com.peoplein.moiming.controller;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.member.DeletedMember;
import com.peoplein.moiming.domain.member.DormantMember;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.inner.StateMapperDto;
import com.peoplein.moiming.security.auth.model.SecurityMember;
import com.peoplein.moiming.service.MoimPostService;
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
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.request.MoimPostReqDto.*;
import static com.peoplein.moiming.model.dto.response.MoimPostRespDto.*;

@Api(tags = "모임 게시물 관련")
@RestController
@RequiredArgsConstructor
public class MoimPostController {

    private final MoimPostService moimPostService;


    @ApiOperation("모임 게시물 생성")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "유저 게시물 생성 성공", response = MoimPostCreateRespDto.class),
            @ApiResponse(code = 400, message = "유저 게시물 생성 실패, ERR MSG 확인")
    })
    @PostMapping(PATH_MOIM_POST_CREATE)
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
        MoimPost moimPost = moimPostService.createMoimPost(requestDto, principal.getMember());
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "모임 게시물 생성 성공"
                , new MoimPostCreateRespDto(moimPost, true)));

    }


    @ApiOperation("게시물 일반 조회 - 모임의 게시물 일반 조회 (기본 정보 응답), (최신 작성일 기준 내림차순, lastId 필요, 10개씩 전달됨)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "게시물 일반 조회 성공", response = MoimPostViewRespDto.class),
            @ApiResponse(code = 400, message = "게시물 일반 조회 실패")
    })
    @GetMapping(PATH_MOIM_POST_GET_VIEW)
    public ResponseEntity<?> getMoimPosts(@PathVariable(name = "moimId") Long moimId
            , @RequestParam(required = false, value = "lastPostId") Long lastPostId
            , @RequestParam(required = false, value = "category") MoimPostCategory category
            , @RequestParam(required = false, defaultValue = "10") int limit
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        StateMapperDto<MoimPost> stateMapper = moimPostService.getMoimPosts(moimId, lastPostId, category, limit, principal.getMember());
        List<MoimPost> moimPosts = stateMapper.getEntities();
        Map<Long, MoimMemberState> stateMap = stateMapper.getStateMapper();
        for (MoimPost moimPost : moimPosts) {
            Long postCreatorId = moimPost.getMember().getId();
            MoimMemberState memberState = stateMap.get(postCreatorId);
            if (memberState.equals(MoimMemberState.NOTFOUND)) {
                moimPost.changeMember(new DeletedMember(postCreatorId));
            }
            if (memberState.equals(MoimMemberState.IBD)) {
                moimPost.changeMember(new DormantMember(postCreatorId));
            }
        }

        List<MoimPostViewRespDto> responseBody = moimPosts.stream().map(moimPost -> new MoimPostViewRespDto(moimPost
                , Objects.equals(moimPost.getMember().getId(), moimPost.getMoim().getCreatorId()) // Moim 과 Member 모두 Fetch Join 되어 영속화된 상태
        )).collect(Collectors.toList());

        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "모든 게시물 일반 조회 성공", responseBody));
    }
}