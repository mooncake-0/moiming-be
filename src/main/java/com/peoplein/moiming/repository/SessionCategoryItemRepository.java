package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.session.SessionCategoryItem;

public interface SessionCategoryItemRepository {

    Long save(SessionCategoryItem sessionCategoryItem);

    void removeAll(Long moimSessionId);
}
