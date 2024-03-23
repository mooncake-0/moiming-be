package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.enums.NotificationSubCategory;
import com.peoplein.moiming.domain.enums.NotificationTopCategory;
import com.peoplein.moiming.domain.enums.NotificationType;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.inner.PostDetailsInnerDto;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.repository.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.stream.Collectors;

import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.inner.PostDetailsInnerDto.*;
import static com.peoplein.moiming.model.dto.request.PostCommentReqDto.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PostCommentService {

    private final MoimPostRepository moimPostRepository;
    private final PostCommentRepository postCommentRepository;
    private final MoimMemberRepository moimMemberRepository;
    private final NotificationService notificationService;


    public PostComment createComment(PostCommentCreateReqDto requestDto, Member member) {

        if (requestDto == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        MoimPost moimPost = moimPostRepository.findById(requestDto.getPostId()).orElseThrow(() ->
                new MoimingApiException(MOIM_POST_NOT_FOUND)
        );

        checkActivePermission(member.getId(), moimPost.getMoim().getId());

        PostComment comment = PostComment.createPostComment(requestDto.getContent(), member, moimPost,
                requestDto.getDepth(), getParentCommentByReqDto(requestDto.getDepth(), requestDto.getParentId()));

        // Comment 종류에 따른 알림 생성
        NotificationSubCategory subCategory = NotificationSubCategory.COMMENT_CREATE;
        Member notificationReceiver = moimPost.getMember();
        String notiBody = moimPost.getPostTitle() + "에 새로운 댓글이 등록되었습니다";
        if (comment.getDepth() == 1) {
            subCategory = NotificationSubCategory.CHILD_COMMENT_CREATE;
            notificationReceiver = comment.getParent().getMember();
            notiBody = moimPost.getPostTitle() + "에 남긴 댓글에 새로운 답글이 등록되었습니다";
        }

        postCommentRepository.save(comment);

        // ACTIVE 한 구성원일시 알림을 전달한다
        MoimMember receiverStatus = moimMemberRepository.findByMemberAndMoimId(notificationReceiver.getId(), moimPost.getMoim().getId()).orElseThrow(() -> {
                    log.error("{}, createComment :: {}", this.getClass().getName(), "알림 대상자의 모임 이력을 찾을 수 없음, 발생하지 않는 상황 : C999");
                    return new MoimingApiException(COMMON_INVALID_SITUATION);
        });

        if (receiverStatus.hasActivePermission()) { // 현재 활동중인 모임원 아니면 알림 안보냄
            notificationService.createNotification(NotificationTopCategory.MOIM, subCategory, NotificationType.INFORM
                    , notificationReceiver.getId(), "", notiBody, moimPost.getMoim().getId(), moimPost.getId());
        }

        return comment;
    }


    public PostComment updateComment(PostCommentUpdateReqDto requestDto, Member member) {

        if (requestDto == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        PostComment comment = postCommentRepository.findWithMemberAndMoimPostById(requestDto.getCommentId()).orElseThrow(() ->
                new MoimingApiException(MOIM_POST_COMMENT_NOT_FOUND)
        );

        checkActivePermission(member.getId(), comment.getMoimPost().getMoim().getId());

        comment.updateComment(requestDto, member.getId());

        return comment;
    }


    public void deleteComment(Long postCommentId, Member member) {

        if (postCommentId == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        PostComment comment = postCommentRepository.findWithMemberAndMoimPostById(postCommentId).orElseThrow(() ->
                new MoimingApiException(MOIM_POST_COMMENT_NOT_FOUND)
        );

        checkActivePermission(member.getId(), comment.getMoimPost().getMoim().getId());

        comment.deleteComment(member.getId());
    }


    public PostCommentDetailsDto getSortedPostComments(Long postId) {

        if (postId == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        List<PostComment> postComments = postCommentRepository.findWithMemberAndInfoByMoimPostInDepthAndCreatedOrder(postId);
        List<PostComment> parentComments = new ArrayList<>();
        Map<Long, List<PostComment>> childCommentsMap = new HashMap<>();

        // List 들은 현재 Order 가 보장된다
        for (PostComment postComment : postComments) {
            if (postComment.getDepth() == 0) {
                parentComments.add(postComment);
                childCommentsMap.put(postComment.getId(), new ArrayList<>());
            } else {
                List<PostComment> childComments = childCommentsMap.get(postComment.getParent().getId());
                childComments.add(postComment);
            }
        }

        // 여기서 MoimMemberState 을 준비한다
        Set<Long> commentCreatorIds = postComments.stream().map(pc -> pc.getMember().getId()).collect(Collectors.toSet());

        return new PostCommentDetailsDto(commentCreatorIds, parentComments, childCommentsMap);
    }


    private PostComment getParentCommentByReqDto(int depth, Long parentId) {

        PostComment parentComment = null;

        // 답글이 아니라 댓글이라 parent 가 없을 경우 0, null 로 전달됨 // if 문을 타지 않고 NULL 로 반환된다
        if (depth != 0 && parentId != null) { // 답글임

            parentComment = postCommentRepository.findById(parentId)
                    .orElseThrow(() -> new MoimingApiException(MOIM_POST_COMMENT_NOT_FOUND)); //답글로 전달되었으나 부모 댓글을 찾을 수 없음 (리소스를 찾을 수 없다)

            // 부모가 depth 가 0 이 아닌경우
            if (parentComment.getDepth() != 0) {
                log.info("{}, getParentCommentByReqDto :: {}", this.getClass().getName(), "답글에 답글 생성 시도");
                throw new MoimingApiException(MOIM_POST_COMMENT_NOT_PARENT);
            }

        } else if (depth != 0 || parentId != null) {

            throw new MoimingApiException(COMMON_INVALID_SITUATION); // 부모 & 자식 관계 매핑 오류, 잘못된 요청
        }

        return parentComment; // 나머진 Null 로 배치된다
    }


    // 요청하는 모임원이 ACTIVE 한지 확인
    private void checkActivePermission(Long memberId, Long moimId) {

        // 댓글을 달 수 있는 권한이 존재하는지 확인 (moimId 를 통해서) // 없으면 NULL
        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(memberId, moimId).orElseThrow(() ->
                new MoimingApiException(MOIM_MEMBER_NOT_FOUND)
        );

        if (!moimMember.hasActivePermission()) {
            throw new MoimingApiException(MOIM_MEMBER_NOT_ACTIVE);
        }
    }
}
