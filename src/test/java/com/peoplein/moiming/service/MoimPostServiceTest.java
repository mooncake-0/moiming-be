package com.peoplein.moiming.service;

import com.peoplein.moiming.BaseTest;
import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.model.dto.domain.MoimPostDto;
import com.peoplein.moiming.model.dto.request.MoimPostRequestDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class MoimPostServiceTest extends BaseTest {

    @Autowired
    MoimPostService moimPostService;

    @Autowired
    EntityManager em;

    @Test
    void updateIntegrationSuccessTest() {
        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();
        MoimPost moimPost = TestUtils.initMoimPost(moim, member);
        String changedPostTitle = "fixed " + TestUtils.postTitle;

        persist(member,
                moim,
                moimPost,
                member.getRoles().get(0).getRole(),
                member.getRoles().get(0));
        flushAndClear();

        MoimPostRequestDto moimPostRequestDto = new MoimPostRequestDto(
                moim.getId(),
                moimPost.getId(),
                changedPostTitle,
                TestUtils.postContent,
                TestUtils.isNotice,
                TestUtils.moimPostCategory);

        // when
        MoimPostDto moimPostDto = moimPostService.updatePost(moimPostRequestDto, member);

        // then
        assertThat(moimPostDto.getPostTitle()).isEqualTo(changedPostTitle);
        assertThat(moimPostDto.getUpdatedUid()).isEqualTo(member.getUid());
    }
    @Test
    void updateIntegrationFailTest() {
        // 작성자만 수정 가능함.

        // given
        Member member = TestUtils.initMemberAndMemberInfo();
        Member updateMember = TestUtils.initOtherMemberAndMemberInfo();
        Moim moim = TestUtils.createMoimOnly();
        MoimPost moimPost = TestUtils.initMoimPost(moim, member);
        String changedPostTitle = "fixed " + TestUtils.postTitle;

        persist(member,
                moim,
                moimPost,
                updateMember,
                member.getRoles().get(0).getRole(),
                member.getRoles().get(0),
                updateMember.getRoles().get(0).getRole(),
                updateMember.getRoles().get(0));
        flushAndClear();

        MoimPostRequestDto moimPostRequestDto = new MoimPostRequestDto(
                moim.getId(),
                moimPost.getId(),
                changedPostTitle,
                TestUtils.postContent,
                TestUtils.isNotice,
                TestUtils.moimPostCategory);

        // when + then
        assertThatThrownBy(() -> moimPostService.updatePost(moimPostRequestDto, updateMember))
                .isInstanceOf(IllegalArgumentException.class);
    }


    void flushAndClear() {
        em.flush();
        em.clear();
    }

    void persist(Object ... objects) {
        Arrays.stream(objects).forEach(o -> em.persist(o));
    }
}
