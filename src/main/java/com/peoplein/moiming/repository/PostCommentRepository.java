package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.PostComment;

import java.util.List;
import java.util.Optional;

public interface PostCommentRepository {


    PostComment findWithMoimPostById(Long postCommentId);

    PostComment findWithMoimPostAndMoimById(Long postCommentId);

    List<PostComment> findWithMoimPostId(Long moimPostId);

    void remove(PostComment postComment);

    Long removeAllByMoimPostId(Long moimPostId);

    void removeAllByMoimPostIds(List<Long> moimPostIds);


    // -- IN USE
    void save(PostComment postComment);

    Optional<PostComment> findById(Long commentId);

    List<PostComment> findByMoimPostInHierarchyQuery(Long moimPostId);

}
