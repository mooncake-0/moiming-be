package com.peoplein.moiming.service.core;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.model.dto.request.MoimPostRequestDto;
import com.peoplein.moiming.service.MoimPostService;
import com.peoplein.moiming.service.input.MoimPostServiceInput;
import com.peoplein.moiming.service.output.MoimPostServiceOutput;
import org.springframework.stereotype.Component;

@Component
public class MoimPostServiceCore {


    public MoimPostServiceOutput createMoimPost(MoimPostServiceInput moimPostServiceInput) {
        MoimPost createdMoimPost = MoimPost.createMoimPost(
                moimPostServiceInput.getPostTitleAboutNewMoimPost(),
                moimPostServiceInput.getPostContentAboutNewMoimPost(),
                moimPostServiceInput.getMoimPostCategoryAboutNewMoimPost(),
                moimPostServiceInput.isNoticeAboutNewMoimPost(),
                moimPostServiceInput.isHasFilesAboutNewMoimPost(),
                moimPostServiceInput.getMoimAboutNewMoimPost(),
                moimPostServiceInput.getMemberAboutNewMoimPost());

        return MoimPostServiceOutput.builder()
                .createdMoimPost(createdMoimPost).build();
    }

}
