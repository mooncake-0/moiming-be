package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.model.dto.domain.PostCommentDto;
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

    public PostCommentDto createPostComment(PostCommentRequestDto postCommentRequestDto, Member curMember) {

        MoimPost moimPost = moimPostRepository.findById(postCommentRequestDto.getMoimPostId());

        PostComment postComment = PostComment.createPostComment(postCommentRequestDto.getCommentContent(), curMember, moimPost);

        postCommentRepository.save(postComment);

        /*
         작성자 정보이기 때문에 따로 member 정보를 담지 않는다
         */
        PostCommentDto postCommentDto = new PostCommentDto(postComment.getId(), postComment.getCommentContent(), postComment.getCreatedAt(), postComment.getUpdatedAt(), true, null);
        return postCommentDto;
    }


    public PostCommentDto updatePostComment(PostCommentRequestDto postCommentRequestDto, Member curMember) {

        PostComment postComment = fetchAndCheckPostComment(postCommentRequestDto.getCommentId());

        if (!postComment.getMember().getId().equals(curMember.getId())) {
            log.error("작성자가 아닌데 수정하려 함");
            throw new RuntimeException("작성자가 아닌데 수정하려 함");
        }

        String changedContent = postCommentRequestDto.getCommentContent();

        boolean isAnyUpdated = false;

        if (!postComment.getCommentContent().equals(changedContent)) {

            isAnyUpdated = true;
            postComment.setCommentContent(changedContent);

        }

        if (isAnyUpdated) {

            postComment.setUpdatedAt(LocalDateTime.now());

            /*
             작성자 정보이기 때문에 따로 member 정보를 담지 않는다
            */
            return new PostCommentDto(
                    postComment.getId(), postComment.getCommentContent(), postComment.getCreatedAt()
                    , postComment.getUpdatedAt(), true, null);

        } else {

            // 수정요청이 들어왔으나 수정된 사항이 없음
            log.error("수정된 사항이 없는 경우");
            throw new RuntimeException("수정된 사항이 없는 경우");
        }
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
                if (!moimMember.getMoimMemberRoleType().equals(MoimMemberRoleType.MANAGER)) {
                    log.error("삭제할 권한이 없는 유저의 요청입니다");
                    throw new RuntimeException("삭제할 권한이 없는 유저의 요청입니다");
                }
            }

            postComment.getMoimPost().removePostComment(postComment);
            postCommentRepository.remove(postComment);
        }
    }

}
