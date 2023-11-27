package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.domain.PostCommentDto;
import com.peoplein.moiming.model.dto.request.PostCommentReqDto;
import com.peoplein.moiming.model.dto.request_b.PostCommentRequestDto;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.repository.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.peoplein.moiming.domain.enums.MoimMemberState.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.PostCommentReqDto.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PostCommentService {

    private final MoimPostRepository moimPostRepository;
    private final PostCommentRepository postCommentRepository;
    private final MoimMemberRepository moimMemberRepository;

    public void createComment(PostCommentCreateReqDto requestDto, Member member) {

        if (requestDto == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM_NULL);
        }

        MoimPost moimPost = moimPostRepository.findById(requestDto.getPostId()).orElseThrow(() ->
                new MoimingApiException(MOIM_POST_NOT_FOUND)
        );

        // 댓글을 달 수 있는 권한이 존재하는지 확인 (moimId 를 통해서) // 없으면 NULL
        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(member.getId(), requestDto.getMoimId()).orElseThrow(() ->
                new MoimingApiException(MOIM_MEMBER_NOT_FOUND)
        );

        if (!moimMember.hasActivePermission()) {
            throw new MoimingApiException(MOIM_MEMBER_NOT_ACTIVE);
        }

        PostComment comment = PostComment.createPostComment(requestDto.getContent(), member, moimPost,
                requestDto.getDepth(), getParentCommentByReqDto(requestDto.getDepth(), requestDto.getParentId()));

        postCommentRepository.save(comment);

    }


    public void updateComment(PostCommentUpdateReqDto requestDto, Member member) {

        // 수정할 때는 외래키를 건들지 않고, commentId 만 바로 조회 후 수정한다
        PostComment postComment = postCommentRepository.findById(requestDto.getPostCommentId()).orElseThrow(() ->
                new MoimingApiException(MOIM_POST_COMMENT_NOT_FOUND)
        );


        postComment.updateComment(requestDto, member.getId());

    }



    public void deletePostComment(Long commentId, Member curMember) {

        // 현재 멤버가 삭제할 권한이 있는지 확인 필요
        // 모임장, 운영진, 작성자만 삭제 가능
        PostComment postComment = postCommentRepository.findWithMoimPostAndMoimById(commentId);

        if (Objects.isNull(postComment)) {
            log.error("해당 PK 의 댓글을 찾을 수 없습니다");
            throw new RuntimeException("해당 PK 의 댓글을 찾을 수 없습니다");
        } else {

            MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(curMember.getId(), postComment.getMoimPost().getMoim().getId()).orElseThrow();

            if (!postComment.getMember().getId().equals(curMember.getId())) {
                // 작성자가 아니라면, 관리자인가?
                if (!moimMember.getMemberRoleType().equals(MoimMemberRoleType.MANAGER)) {
                    log.error("삭제할 권한이 없는 유저의 요청입니다");
                    throw new RuntimeException("삭제할 권한이 없는 유저의 요청입니다");
                }
            }

            postComment.getMoimPost().removePostComment(postComment);
            postCommentRepository.remove(postComment);
        }
    }

    private PostComment getParentCommentByReqDto(int depth, Long parentId) {

        PostComment parentComment = null;

        if (depth != 0 && parentId != null) { // 답글임

            parentComment = postCommentRepository.findById(parentId)
                    .orElseThrow(() -> new MoimingApiException(MOIM_POST_COMMENT_NOT_FOUND)); //답글로 전달되었으나 부모 댓글을 찾을 수 없음 (리소스를 찾을 수 없다)

        } else if (depth != 0 || parentId != null) {

            throw new MoimingApiException(COMMON_INVALID_PARAM); // 부모 & 자식 관계 매핑 오류, 잘못된 요청
        }

        return parentComment; // 나머진 Null 로 배치된다
    }

}
