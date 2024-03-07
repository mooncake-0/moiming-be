package com.peoplein.moiming.domain.enums;

import lombok.Getter;

import static com.peoplein.moiming.domain.enums.NotificationTopCategory.*;

@Getter
public enum NotificationSubCategory {

    // MOIM 에 대한
    MOIM_JOIN(MOIM,"모임 가입"),
    MOIM_IBW(MOIM,"모임 탈퇴"),
    MOIM_IBF(MOIM,"강제 탈퇴"),
    POST_CREATE(MOIM,"게시글 등록"),
    COMMENT_CREATE(MOIM,"댓글 등록"),
    CHILD_COMMENT_CREATE(MOIM,"답글 등록"),
    DEFAULT(NotificationTopCategory.DEFAULT, "기타"),
    ;

    private final NotificationTopCategory topCategory;
    private final String value;

    NotificationSubCategory(NotificationTopCategory topCategory, String value) {
        this.topCategory = topCategory;
        this.value = value;
    }

}
