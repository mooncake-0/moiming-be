package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.PostComment;

import java.util.List;

public interface PostCommentRepository {

    Long save(PostComment postComment);

    PostComment findById(Long postCommentId);

    PostComment findWithMoimPostById(Long postCommentId);

    PostComment findWithMoimPostAndMoimById(Long postCommentId);

    void remove(PostComment postComment);

    Long removeAllByMoimPostId(Long moimPostId);

    void removeAllByMoimPostIds(List<Long> moimPostIds);
}
