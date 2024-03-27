package com.peoplein.moiming.controller;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.member.DeletedMember;
import com.peoplein.moiming.domain.member.DormantMember;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.inner.StateMapperDto;
import com.peoplein.moiming.model.dto.response.MoimPostDetailViewRespDto;
import com.peoplein.moiming.security.auth.model.SecurityMember;
import com.peoplein.moiming.service.MoimPostService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
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
import static com.peoplein.moiming.model.dto.inner.PostDetailsInnerDto.*;
import static com.peoplein.moiming.model.dto.request.MoimPostReqDto.*;
import static com.peoplein.moiming.model.dto.response.MoimPostRespDto.*;
import static com.peoplein.moiming.model.dto.response.MoimPostRespDto.MoimPostViewRespDto.*;

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
                , new MoimPostCreateRespDto(moimPost, principal.getMember())));

    }


    @ApiOperation("게시물 일반 조회 - 모임의 게시물 일반 조회 (기본 정보 응답), (최신 작성일 기준 내림차순, 페이징시 lastId 필요, 20개씩 전달됨)")
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
            , @RequestParam(required = false, value = "category") String category
            , @RequestParam(required = false, defaultValue = "20") int limit
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        MoimPostCategory moimPostCategory = null;
        if (StringUtils.hasText(category)) {
            moimPostCategory = MoimPostCategory.fromQueryParam(category);
        }
        StateMapperDto<MoimPost> stateMapper = moimPostService.getMoimPosts(moimId, lastPostId, moimPostCategory, limit, principal.getMember());
        List<MoimPost> moimPosts = stateMapper.getEntities();
        checkToChangePostCreatorInfo(moimPosts, stateMapper.getStateMapper());

        List<MoimPostViewRespDto> responseBody = moimPosts.stream().map(moimPost -> new MoimPostViewRespDto(moimPost
                , Objects.equals(moimPost.getMember().getId(), moimPost.getMoim().getCreatorId()) // Moim 과 Member 모두 Fetch Join 되어 영속화된 상태
        )).collect(Collectors.toList());

        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "모든 게시물 일반 조회 성공", responseBody));
    }


    @ApiOperation("게시물 세부 조회 - Post 의 모든 정보와 Comment 들이 전달 / ParentComment 는 일반 댓글 List 로, 최신순 정렬되어 있다")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "게시물 세부 조회 성공", response = MoimPostDetailViewRespDto.class),
            @ApiResponse(code = 400, message = "게시물 세부 조회 실패")
    })
    @GetMapping(PATH_MOIM_POST_GET_DETAIL)
    public ResponseEntity<?> getMoimPostDetail(@PathVariable(name = "moimId") Long moimId
            , @PathVariable Long moimPostId
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        PostDetailsDto moimPostDetail = moimPostService.getMoimPostDetail(moimPostId, principal.getMember());
        Map<Long, MoimMemberState> memberStates = moimPostDetail.getMemberStates();

        // Post Creator 의 것 먼저 확인
        MoimPost post = moimPostDetail.getMoimPost();
        checkToChangePostCreatorInfo(List.of(post), memberStates);

        // PostComment 쭉 확인
        List<PostComment> parents = moimPostDetail.getParentComments();
        checkToChangeCommentCreatorInfo(parents, memberStates);

        Map<Long, List<PostComment>> childsMap = moimPostDetail.getChildCommentsMap();
        for (List<PostComment> comments : childsMap.values()) { // 모든 Child 도 돌린다
            checkToChangeCommentCreatorInfo(comments, memberStates);
        }

        MoimPostDetailViewRespDto responseData = new MoimPostDetailViewRespDto(post, parents, childsMap);
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "모임 세부 조회 성공", responseData));
    }


    @ApiOperation("게시물 수정 - 수정 데이터만 전달")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "게시물 수정 성공", response = MoimPostUpdateRespDto.class),
            @ApiResponse(code = 400, message = "게시물 수정 실패")
    })
    @PatchMapping(PATH_MOIM_POST_UPDATE)
    public ResponseEntity<?> updatePost(
            @RequestBody @Valid MoimPostUpdateReqDto requestDto
            , BindingResult br
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        MoimPost moimPost = moimPostService.updateMoimPost(requestDto, principal.getMember());

        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "게시물 수정 성공", new MoimPostUpdateRespDto(moimPost)));
    }


    // 삭제
    @ApiOperation("게시물 삭제")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "게시물 삭제 성공"),
            @ApiResponse(code = 400, message = "게시물 삭제 실패")
    })
    @DeleteMapping(PATH_MOIM_POST_DELETE)
    public ResponseEntity<?> deletePost(@PathVariable Long moimId
            , @PathVariable Long moimPostId
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        moimPostService.deleteMoimPost(moimPostId, principal.getMember());
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "게시물 삭제 성공", null));

    }


    private void checkToChangePostCreatorInfo(List<MoimPost> posts, Map<Long, MoimMemberState> memberStates) {

        for (MoimPost post : posts) {
            MoimMemberState postCreatorState = memberStates.get(post.getMember().getId());

            if (postCreatorState.equals(MoimMemberState.NOTFOUND)) {
                post.changeMember(new DeletedMember(post.getMember()));
            }
            if (postCreatorState.equals(MoimMemberState.IBD)) {
                post.changeMember(new DormantMember(post.getMember()));
            }
        }

    }


    private void checkToChangeCommentCreatorInfo(List<PostComment> comments, Map<Long, MoimMemberState> memberStates) {

        for (PostComment comment : comments) {
            MoimMemberState commentCreatorState = memberStates.get(comment.getMember().getId());

            if (commentCreatorState.equals(MoimMemberState.NOTFOUND)) {
                comment.changeMember(new DeletedMember(comment.getMember()));
            }
            if (commentCreatorState.equals(MoimMemberState.IBD)) {
                comment.changeMember(new DormantMember(comment.getMember()));
            }
        }

    }
}