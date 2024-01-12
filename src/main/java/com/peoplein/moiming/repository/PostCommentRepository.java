package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.PostComment;

import java.util.List;
import java.util.Optional;

public interface PostCommentRepository {


    void save(PostComment postComment);

    Optional<PostComment> findById(Long commentId);

    Optional<PostComment> findWithMoimPostAndMoimById(Long commentId);

    List<PostComment> findByMoimPostInHierarchyQuery(Long moimPostId);

    void removeAllByMoimPostId(Long moimPostId);

}
