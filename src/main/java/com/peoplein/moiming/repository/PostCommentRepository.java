package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.PostComment;

import java.util.List;
import java.util.Optional;

public interface PostCommentRepository {


    void save(PostComment postComment);

    Optional<PostComment> findById(Long commentId);

    Optional<PostComment> findWithMemberAndMoimPostById(Long commentId);

    List<PostComment> findByMoimPostInHierarchyQuery(Long moimPostId); // MEMO :: 계층형 쿼리처럼 한 번에 정렬된 채로 가져오는건 좀 어려울 듯 하다 (오라클에선 네이티브로면 가능할텐데)

    List<PostComment> findWithMemberAndInfoByMoimPostInDepthAndCreatedOrder(Long moimPostId);

    void removeAllByMoimPostId(Long moimPostId);

}
