package com.peoplein.moiming.service.shell;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.model.dto.domain.MoimPostDto;
import com.peoplein.moiming.model.dto.request_b.MoimPostRequestDto;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.service.input.MoimPostServiceInput;
import com.peoplein.moiming.service.output.MoimPostServiceOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MoimPostServiceShell {


    private final MoimRepository moimRepository;
    private final MoimPostRepository moimPostRepository;


    /**
     * Required Argument
     * - @postTitle
     * - @postContent
     * - @moimPostCategory
     * - @isNotice
     * - @hasFile
     * - @moim
     * - @member
     */
    public MoimPostServiceInput readyForCreatingNewMoimPost(MoimPostRequestDto moimPostRequestDto, Member member) {
        Moim moim = moimRepository.findById(moimPostRequestDto.getMoimId()).orElseThrow();
        return MoimPostServiceInput.builder()
                .postTitleAboutNewMoimPost(moimPostRequestDto.getPostTitle())
                .postContentAboutNewMoimPost(moimPostRequestDto.getPostContent())
                .moimPostCategoryAboutNewMoimPost(moimPostRequestDto.getMoimPostCategory())
                .isNoticeAboutNewMoimPost(moimPostRequestDto.isNotice())
                .hasFilesAboutNewMoimPost(false) // TODO : 파일 로직에 따라 변경 예정
                .moimAboutNewMoimPost(moim)
                .memberAboutNewMoimPost(member)
                .build();
    }


    // TODO :: Merge 시 에러발생 했는데 원인 확인 불가
    public MoimPostDto doAfterCreatingMoimPost(MoimPostServiceOutput moimPostServiceOutput) {
        MoimPost createdMoimPost = moimPostServiceOutput.getCreatedMoimPost();
        moimPostRepository.save(createdMoimPost);
//        return MoimPostDto.createMoimPostDtoByMySelf(createdMoimPost);
        return new MoimPostDto(createdMoimPost);
    }
}
