package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.inner.PostDetailsInnerDto;
import com.peoplein.moiming.model.dto.inner.StateMapperDto;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.repository.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.inner.PostDetailsInnerDto.*;
import static com.peoplein.moiming.model.dto.request.MoimPostReqDto.*;


/*

해당 게시글을 작성한 사람은 수정/삭제가 가능하다.

운영자가 작성할 수 있는 카테고리는 공지, 가입인사, 모임후기, 자유글

참여자가 작성할 수 있는 카테고리는 가입인사, 모임후기, 자유글

모이밍 서비스를 탈퇴한 멤버가 작성한 글은 user명이 --> 탈퇴한 사용자으로 남겨진다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MoimPostService {


    private final PostCommentService postCommentService;
    private final MoimMemberService moimMemberService;
    private final MoimMemberRepository moimMemberRepository;
    private final MoimPostRepository moimPostRepository;
    private final PostCommentRepository postCommentRepository;

    @Transactional
    public MoimPost createMoimPost(MoimPostCreateReqDto requestDto, Member member) {

        if (requestDto == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(member.getId(), requestDto.getMoimId())
                .orElseThrow(() -> new MoimingApiException(MOIM_MEMBER_NOT_FOUND));

        if (!moimMember.getMemberState().equals(MoimMemberState.ACTIVE)) {
            throw new MoimingApiException(MOIM_MEMBER_NOT_AUTHORIZED);
        }

        MoimPost post = MoimPost.createMoimPost(requestDto.getPostTitle(), requestDto.getPostContent()
                , MoimPostCategory.fromValue(requestDto.getMoimPostCategory())
                , requestDto.getHasPrivateVisibility(), requestDto.getHasFiles()
                , moimMember.getMoim(), moimMember.getMember());

        moimPostRepository.save(post);

        return post;
    }


    // 모임의 모든 Post 전달 (필요 정보는 사실상 File 빼고 전부 다)
    public StateMapperDto<MoimPost> getMoimPosts(Long moimId, Long lastPostId, MoimPostCategory category, int limit, Member member) {

        if (moimId == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        /*
         TODO : 1_오직 모임 생성자_id 만을 위해 Moim Fetch Join 을 해줘야한다 --> JPA 에서 객체 조회 말고 큰 다른 방법은 없을까?
                2_각 게시물 생성자 정보를 같이 전달하기 위핸 Post Creator Member Fetch Join
         */
        MoimPost lastPost = null;
        if (lastPostId != null) {
            lastPost = moimPostRepository.findById(lastPostId).orElseThrow(() ->
                    new MoimingApiException(MOIM_POST_NOT_FOUND)
            );
        }

        // 멤버가 구성원인지 확인 필요 - 자체 필드에 대한 제어 로직 - 도메인단에 둘 수가 없음
        boolean moimMemberRequest = true;
        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(member.getId(), moimId).orElse(null);
        if (moimMember == null || !moimMember.getMemberState().equals(MoimMemberState.ACTIVE)) {
            moimMemberRequest = false;
        }

        // Sort 됨, 중요!
        List<MoimPost> moimPosts = moimPostRepository.findWithMemberByCategoryAndLastPostOrderByDateDesc(moimId, lastPost, category, limit, moimMemberRequest);

        // Post 들의 작성자들 상태
        Set<Long> postCreatorIds = moimPosts.stream().map(moimPost -> moimPost.getMember().getId()).collect(Collectors.toSet());
        Map<Member, MoimMemberState> postCreatorStates = moimMemberService.getMoimMemberStates(moimId, postCreatorIds);

        return new StateMapperDto<>(moimPosts, postCreatorStates);
    }


    // 특정 Post 의 모든 Data 전달
    // 게시물 정보 반환, 게시물 댓글들 반환,
    public PostDetailsDto getMoimPostDetail(Long postId, Member member) {

        if (postId == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        // post 존재성 확인
        MoimPost moimPost = moimPostRepository.findWithMemberById(postId).orElseThrow(() ->
                new MoimingApiException(MOIM_POST_NOT_FOUND)
        );

        Optional<MoimMember> moimMemberOp = moimMemberRepository.findByMemberAndMoimId(member.getId(), moimPost.getMoim().getId());

        // moimMember 가 Active 가 아닐경우 + moimPost 가 비공개일 경우 > 거른다
        // moimMember 가 없을 경우 + moimPost 가 비공개일 경우 > 거른다
        if (moimPost.isHasPrivateVisibility()) {
            if (moimMemberOp.isEmpty() || !moimMemberOp.get().hasActivePermission()) {
                throw new MoimingApiException(MOIM_ACT_NOT_AUTHORIZED);
            }
        }

        PostCommentDetailsDto commentsDto = postCommentService.getSortedPostComments(postId);
        commentsDto.getCommentCreatorIds().add(moimPost.getMember().getId()); // 게시물 생성자가 없을 수도 있음, 추가해준다
        // moimMemberService 한테 이 MoimMember 들에 대한 상태를 조회한다
        Map<Member, MoimMemberState> memberStates = moimMemberService.getMoimMemberStates(moimPost.getMoim().getId(), commentsDto.getCommentCreatorIds());

        return new PostDetailsDto(moimPost, memberStates, commentsDto.getParentComments(), commentsDto.getChildCommentsMap());

    }


    // Post 수정
    public MoimPost updateMoimPost(MoimPostUpdateReqDto requestDto, Member member) {

        if (requestDto == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        // post 존재성 확인
        MoimPost moimPost = moimPostRepository.findById(requestDto.getMoimPostId()).orElseThrow(() ->
                new MoimingApiException(MOIM_POST_NOT_FOUND)
        );

        // post 에 저장된 moimId + member 로 moimMember 조회
        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(member.getId(), moimPost.getMoim().getId()).orElseThrow(() ->
                new MoimingApiException(MOIM_MEMBER_NOT_FOUND)
        );

        // MoimMember 권한 확인 (활동중이여야 하며, 작성자여야 한다)
        if (!moimMember.hasActivePermission() || !moimPost.getMember().getId().equals(member.getId())) {
            throw new MoimingApiException(MOIM_MEMBER_NOT_AUTHORIZED);
        }

        moimPost.changeMoimPostInfo(requestDto.getPostTitle(), requestDto.getPostContent(), MoimPostCategory.fromValue(requestDto.getMoimPostCategory())
                , requestDto.getHasFiles(), requestDto.getHasPrivateVisibility(), member.getId());

        return moimPost;
    }


    // Post 삭제 (댓글 / 답글에 대한 로직 처리 후 최종 진행)
    public void deleteMoimPost(Long postId, Member member) {

        if (postId == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        // post 존재성 확인
        MoimPost moimPost = moimPostRepository.findById(postId).orElseThrow(() ->
                new MoimingApiException(MOIM_POST_NOT_FOUND)
        );

        // post 에 저장된 moimId + member 로 moimMember 조회
        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(member.getId(), moimPost.getMoim().getId()).orElseThrow(() ->
                new MoimingApiException(MOIM_MEMBER_NOT_FOUND)
        );

        // ACTIVE 아니면 무조건 안됨
        // ACTIVE 안에서, MoimMember 권한 확인 (운영자 or 작성자) > 역은 둘다 동시에 아닌 것
        if (!moimMember.hasActivePermission() ||
                (!moimMember.hasPermissionOfManager() && !moimPost.getMember().getId().equals(member.getId()))) {
            throw new MoimingApiException(MOIM_MEMBER_NOT_AUTHORIZED);
        }

        // 댓글 삭제 진행
        postCommentRepository.removeAllByMoimPostId(postId);

        // 게시물 삭제 진행
        moimPostRepository.remove(moimPost);

        // TODO:: 파일 삭제 진행
    }

}
