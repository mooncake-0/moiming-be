package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.moim.Moim;
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
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.repository.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

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


    public PostComment createComment(PostCommentCreateReqDto requestDto, Member member) {

        if (requestDto == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM_NULL);
        }

        // TODO :: MoimFetch Join VS Moim PK 조회
        MoimPost moimPost = moimPostRepository.findById(requestDto.getPostId()).orElseThrow(() ->
                new MoimingApiException(MOIM_POST_NOT_FOUND)
        );

        checkActivePermission(member.getId(), moimPost.getMoim().getId());

        PostComment comment = PostComment.createPostComment(requestDto.getContent(), member, moimPost,
                requestDto.getDepth(), getParentCommentByReqDto(requestDto.getDepth(), requestDto.getParentId()));

        postCommentRepository.save(comment);

        return comment;
    }


    public PostComment updateComment(PostCommentUpdateReqDto requestDto, Member member) {

        if (requestDto == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM_NULL);
        }

        // PK 조회 후 조인될 애들 FK 인덱스 조회
        PostComment comment = postCommentRepository.findWithMoimPostAndMoimById(requestDto.getCommentId()).orElseThrow(() ->
                new MoimingApiException(MOIM_POST_COMMENT_NOT_FOUND)
        );

        checkActivePermission(member.getId(), comment.getMoimPost().getMoim().getId());

        comment.updateComment(requestDto, member.getId());

        return comment;
    }


    public void deleteComment(Long postCommentId, Member member) {

        if (postCommentId == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM_NULL);
        }

        PostComment comment = postCommentRepository.findWithMoimPostAndMoimById(postCommentId).orElseThrow(() ->
                new MoimingApiException(MOIM_POST_COMMENT_NOT_FOUND)
        );

        checkActivePermission(member.getId(), comment.getMoimPost().getMoim().getId());

        comment.deleteComment(member.getId());
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
