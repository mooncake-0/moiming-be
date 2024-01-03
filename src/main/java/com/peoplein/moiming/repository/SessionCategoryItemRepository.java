package com.peoplein.moiming.repository;

import com.peoplein.moiming.temp.session.SessionCategoryItem;

public interface SessionCategoryItemRepository {

    Long save(SessionCategoryItem sessionCategoryItem);

    void removeAll(Long moimSessionId);
}
