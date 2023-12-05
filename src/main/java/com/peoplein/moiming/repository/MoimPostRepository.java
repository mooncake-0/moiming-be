package com.peoplein.moiming.repository;


import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.MoimPostCategory;

import java.util.List;
import java.util.Optional;

public interface MoimPostRepository {

    void removeAll(List<Long> scheduleIds);

    void remove(MoimPost moimPost);

    // IN _ USE
    void save(MoimPost moimPost);

    Optional<MoimPost> findById(Long moimPostId);

    Optional<MoimPost> findWithMoimById(Long moimPostId);

    List<MoimPost> findByCategoryAndLastPostOrderByDateDesc(Long moimId, MoimPost moimPost, MoimPostCategory category, int limit, boolean hasPrivateVisibility);

}
