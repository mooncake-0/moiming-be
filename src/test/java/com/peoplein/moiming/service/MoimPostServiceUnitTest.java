package com.peoplein.moiming.service;

import com.peoplein.moiming.BaseTest;
import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.model.dto.request_b.MoimPostRequestDto;
import com.peoplein.moiming.service.core.MoimPostServiceCore;
import com.peoplein.moiming.service.input.MoimPostServiceInput;
import com.peoplein.moiming.service.output.MoimPostServiceOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class MoimPostServiceUnitTest extends BaseTest {


    MoimPostServiceCore moimPostServiceCore;

    @BeforeEach
    void initInstance() {
        moimPostServiceCore = new MoimPostServiceCore();
    }


    @Test
    void createMoimPostTestPass() {
        // given

        Moim moim = TestUtils.initMoimAndRuleJoin();
        Member member = TestUtils.initMemberAndMemberInfo();
        MoimPostRequestDto moimPostRequestDto = TestUtils.initMoimPostRequestDto();
        MoimPostServiceInput input = MoimPostServiceInput.builder()
                .postTitleAboutNewMoimPost(moimPostRequestDto.getPostTitle())
                .postContentAboutNewMoimPost(moimPostRequestDto.getPostContent())
                .moimPostCategoryAboutNewMoimPost(moimPostRequestDto.getMoimPostCategory())
                .isNoticeAboutNewMoimPost(moimPostRequestDto.isNotice())
                .hasFilesAboutNewMoimPost(false)
                .moimAboutNewMoimPost(moim)
                .memberAboutNewMoimPost(member)
                .build();

        // when
        MoimPostServiceOutput output = moimPostServiceCore.createMoimPost(input);

        // then
        assertThat(output.getCreatedMoimPost().getPostContent()).isEqualTo(input.getPostContentAboutNewMoimPost());
        assertThat(output.getCreatedMoimPost().getPostTitle()).isEqualTo(input.getPostTitleAboutNewMoimPost());
        assertThat(output.getCreatedMoimPost().getMoimPostCategory()).isEqualTo(input.getMoimPostCategoryAboutNewMoimPost());
        assertThat(output.getCreatedMoimPost().getMoim().getMoimName()).isEqualTo(moim.getMoimName());
        assertThat(output.getCreatedMoimPost().getMember().getId()).isEqualTo(member.getId());
    }

    @Test
    void deleteMoimPostTestPass() {
        // given

        Moim moim = TestUtils.initMoimAndRuleJoin();
        Member member = TestUtils.initMemberAndMemberInfo();
        MoimPostRequestDto moimPostRequestDto = TestUtils.initMoimPostRequestDto();
        MoimPostServiceInput input = MoimPostServiceInput.builder()
                .postTitleAboutNewMoimPost(moimPostRequestDto.getPostTitle())
                .postContentAboutNewMoimPost(moimPostRequestDto.getPostContent())
                .moimPostCategoryAboutNewMoimPost(moimPostRequestDto.getMoimPostCategory())
                .isNoticeAboutNewMoimPost(moimPostRequestDto.isNotice())
                .hasFilesAboutNewMoimPost(false)
                .moimAboutNewMoimPost(moim)
                .memberAboutNewMoimPost(member)
                .build();

        // when
        MoimPostServiceOutput output = moimPostServiceCore.createMoimPost(input);

        // then
        assertThat(output.getCreatedMoimPost().getPostContent()).isEqualTo(input.getPostContentAboutNewMoimPost());
        assertThat(output.getCreatedMoimPost().getPostTitle()).isEqualTo(input.getPostTitleAboutNewMoimPost());
        assertThat(output.getCreatedMoimPost().getMoimPostCategory()).isEqualTo(input.getMoimPostCategoryAboutNewMoimPost());
        assertThat(output.getCreatedMoimPost().getMoim().getMoimName()).isEqualTo(moim.getMoimName());
        assertThat(output.getCreatedMoimPost().getMember().getId()).isEqualTo(member.getId());
    }

}