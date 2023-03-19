package com.peoplein.moiming.service.input;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MoimPostServiceInput {


    private String postTitleAboutNewMoimPost;
    private String postContentAboutNewMoimPost;
    private MoimPostCategory moimPostCategoryAboutNewMoimPost;
    private boolean isNoticeAboutNewMoimPost;
    private boolean hasFilesAboutNewMoimPost;
    private Moim moimAboutNewMoimPost;
    private Member memberAboutNewMoimPost;

}
