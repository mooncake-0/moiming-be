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
    private final MoimRepository moimRepository;

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

        if (requestDto == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM_NULL);
        }

        // 수정할 때는 외래키를 건들지 않고, commentId 만 바로 조회 후 수정한다
        PostComment postComment = postCommentRepository.findById(requestDto.getPostCommentId()).orElseThrow(() ->
                new MoimingApiException(MOIM_POST_COMMENT_NOT_FOUND)
        );


        postComment.updateComment(requestDto, member.getId());

    }


    public void deleteComment(Long postCommentId, Member member) {

        if (postCommentId == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM_NULL);
        }


        /*
         일단 다 PK 조회로 타게끔 각각 한다 >> 근데 이렇게 하니까 정합성 검증을 못함 (각각 가져오면 PostComment 의 모임이 맞는지 확인필요하기 때문)
         >> 1. Moim 을 Fetch Join 하려면 이중 Fetch..? 필수?
         >> 2. 단일 Fetch Join 으로 가능하다고 해도 뭐가 더 이득일까?
         >> 차피 정합검증 하려면 FETCH JOIN 해야하니까 단일 FETCH 로 생각해봄

         >> 여러가지 최적화를 고민해 봤으나, 어쨌든 두 번 JOIN 이 불가피하다면 데이터 더 불러오는거 가지고는 큰 성능차이는 보기 어려울 듯
         >> 그냥 다 불러오고 두번 Join 하는걸로.. 정합성 검증 포기하면 데이터 많을 때 빠르긴 하겠지만.. 차라리 그러면 PostComment 에 확인용 moim_id 칼럼을
         >> 두는 반정규화를 하는게 나을 듯
         */

        PostComment postComment = postCommentRepository.findWithMoimPostAndMoimById(postCommentId).orElseThrow(() ->
                new MoimingApiException(MOIM_POST_COMMENT_NOT_FOUND)
        );

        postComment.deleteComment(member.getId());


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
