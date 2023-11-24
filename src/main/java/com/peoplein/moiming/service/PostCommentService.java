package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
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
import static com.peoplein.moiming.model.dto.request.PostCommentReqDto.*;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PostCommentService {

    private final MoimPostRepository moimPostRepository;
    private final PostCommentRepository postCommentRepository;
    private final MoimMemberRepository moimMemberRepository;

    public PostComment fetchAndCheckPostComment(Long postCommentId) {

        PostComment postComment = postCommentRepository.findWithMoimPostById(postCommentId);

        if (Objects.isNull(postComment)) {
            log.error("해당 PK 의 댓글을 찾을 수 없습니다");
            throw new RuntimeException("해당 PK 의 댓글을 찾을 수 없습니다");
        }

        return postComment;
    }


    public void createComment(PostCommentCreateReqDto requestDto, Member member) {

        if (requestDto == null || member == null) {
            throw new RuntimeException(); // TODO :: NULL 변수 전달 예외
        }

        MoimPost moimPost = moimPostRepository.findById(requestDto.getPostId()).orElseThrow(() ->
                new RuntimeException("")  // TODO :: 댓글 달려는 게시판을 찾을 수 없음 -> Base Domain 을 찾을 수 없는 예외
        );

        // 댓글을 달 수 있는 권한이 존재하는지 확인 (moimId 를 통해서) // 없으면 NULL
        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(member.getId(), requestDto.getMoimId())
                .orElse(null);

        if (moimMember == null || !moimMember.getMemberState().equals(ACTIVE)) {
            throw new RuntimeException("");  // TODO :: 모임 내에서 특정 ACTION 을 할 권한이 없는 예외
        }

        PostComment parentComment = null;
        if (requestDto.getDepth() != 0 && requestDto.getParentId() != null) { // 답글임
            parentComment = postCommentRepository.findById(requestDto.getParentId())
                    .orElseThrow(()-> new RuntimeException("")); // TODO :: 답글로 전달되었으나 부모 댓글을 찾을 수 없음 (리소스를 찾을 수 없다)
        }

        PostComment comment = PostComment.createPostComment(requestDto.getContent(), member, moimPost,
                requestDto.getDepth(), parentComment);

        // PERSIST
        postCommentRepository.save(comment);

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

}
