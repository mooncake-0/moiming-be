package com.peoplein.moiming.repository;


import com.peoplein.moiming.domain.MoimPost;

import java.util.List;

public interface MoimPostRepository {

    Long save(MoimPost moimPost);
    MoimPost findById(Long moimPostId);

    MoimPost findWithMemberById(Long moimPostId);

    MoimPost findWithMemberId(Long moimPostId, Long memberId);

    MoimPost findWithMoimAndMemberById(Long moimPostId);
    MoimPost findWithMoimAndMemberInfoById(Long moimPostId);

    List<MoimPost> findByMoimId(Long moimId);

    List<MoimPost> findWithMemberInfoByMoimId(Long moimId);

    void removeAll(List<Long> scheduleIds);

    void remove(MoimPost moimPost);

}
