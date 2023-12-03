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

        // 정합성 검증 된 상태를 보장하기 위해 moimId 를 보내지 않고, 직접불러와서 확인한다
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

        // 수정할 때는 외래키를 건들지 않고, commentId 만 바로 조회 후 수정한다
        PostComment comment = postCommentRepository.findById(requestDto.getCommentId()).orElseThrow(() ->
                new MoimingApiException(MOIM_POST_COMMENT_NOT_FOUND)
        );

        // MEMO :: 어차피 MoimPost 를 새로 조회해서 가져오나, comment.getMoimPost 를 해서 가져오나 동일한 쿼리가 나감
        //         comment.getMp 를 통해 가져오면 그 대신 정합성 검증은 자동으로 된 상태라고 판단한다, delete 동일
        checkActivePermission(member.getId(), comment.getMoimPost().getMoim().getId());

        comment.updateComment(requestDto, member.getId());

        return comment;

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

    @Autowired
    private EntityManager em;


    // PK 로 각각 조회하며 진행
    // 장 : 제일 직관적, 변경 사항에 유동적, 역할이 분할되어 있는 느낌. PK 조회라서 빠름
    // 단 : 전달해야할 데이터가 많아지고, 코드가 길어짐
    public void deleteCommentV1(Long commentId, Long postId, Long moimId, Member reqMember) {
        System.out.println("V1 START");
        System.out.println("V1 START");
        System.out.println("V1 START");
        System.out.println("V1 START");

        PostComment comment = em.find(PostComment.class, commentId);
        MoimPost post = em.find(MoimPost.class, postId);
        Moim moim = em.find(Moim.class, moimId);

        System.out.println("=== 추가적 쿼리 발생하지 않음 확인 === ");

        if (!Objects.equals(comment.getMoimPost().getId(), post.getId())
                || !Objects.equals(post.getMoim().getId(), moim.getId())) {
            throw new RuntimeException("정합성 오류");
        }

        // 이후 로직 진행
        MoimMember groupMember = moimMemberRepository.findByMemberAndMoimId(reqMember.getId(), moimId).orElseThrow(() -> new RuntimeException(""));
        if (!groupMember.hasActivePermission()) {
            throw new RuntimeException("유저가 활동중이지 않습니다");
        }

        Long reqId = reqMember.getId();
        if (!Objects.equals(comment.getMember().getId(), reqId)
                || !Objects.equals(moim.getCreatorId(), reqId)) {
            throw new RuntimeException("댓글을 삭제할 권한이 없습니다");
        }

        System.out.println("V1 FINISHED");

    }


    // 객체 Map 탐색
    // 장 : PK 쿼리가 필요할 때 계속 나가므로 빠름.
    // 단 : 연관관계 변경, 추가 등에 사항이 발생시 refactoring 필요
    public void deleteCommentV2(Long commentId, Member reqMember) {

        System.out.println("V2 START");
        System.out.println("V2 START");
        System.out.println("V2 START");
        System.out.println("V2 START");


        PostComment comment = em.find(PostComment.class, commentId);

        System.out.println("=========== 쿼리 분기 점 ========== ");

        // MoimPost -> getMoim 으로 인한 조회 발생
        MoimMember groupMember = moimMemberRepository.findByMemberAndMoimId(reqMember.getId(), comment.getMoimPost().getMoim().getId()).orElseThrow(() -> new RuntimeException(""));
        if (!groupMember.hasActivePermission()) {
            throw new RuntimeException("유저가 활동중이지 않습니다");
        }

        System.out.println("=========== 쿼리 분기 점 ========== ");

        // Moim -> getCreatorId 로 인한 조회 발생
        Long reqId = reqMember.getId();
        if (!Objects.equals(comment.getMember().getId(), reqId)
                || !Objects.equals(comment.getMoimPost().getMoim().getCreatorId(), reqId)) {
            throw new RuntimeException("댓글을 삭제할 권한이 없습니다");
        }

        System.out.println("V2 FINISHED");
    }


    // 장 : 한방쿼리로 해결 가능, DB 와의 통신이 한 번으로 종료된다
    // 단 : JOIN 이 두번 필요하다

    // Fetch Join
    public void deleteCommentV3(Long commentId, Member reqMember) {


        System.out.println("V3 START");
        System.out.println("V3 START");
        System.out.println("V3 START");
        System.out.println("V3 START");


        String jpql =
                "select c from PostComment c " +
                        "join fetch c.moimPost mp " +
                        "join fetch mp.moim m " +
                        "where c.id = :commentId";

        PostComment comment = em.createQuery(jpql, PostComment.class)
                .setParameter("commentId", commentId)
                .getSingleResult();

        System.out.println("=========== 쿼리 분기 점 : 더이상 쿼리가 발생하지 않음 ========== ");

        MoimPost moimPost = comment.getMoimPost();
        Moim moim = moimPost.getMoim();

        // MoimPost -> getMoim 으로 인한 조회 발생
        MoimMember groupMember = moimMemberRepository.findByMemberAndMoimId(reqMember.getId(), moim.getId()).orElseThrow(() -> new RuntimeException(""));
        if (!groupMember.hasActivePermission()) {
            throw new RuntimeException("유저가 활동중이지 않습니다");
        }

        System.out.println("=========== 쿼리 분기 점 ========== ");

        // Moim -> getCreatorId 로 인한 조회 발생
        Long reqId = reqMember.getId();
        if (!Objects.equals(comment.getMember().getId(), reqId)
                || !Objects.equals(moim.getCreatorId(), reqId)) {
            throw new RuntimeException("댓글을 삭제할 권한이 없습니다");
        }

        System.out.println("V3 FINISHED");
    }
}
