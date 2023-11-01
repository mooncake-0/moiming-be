package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.request.MoimPostReqDto;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.peoplein.moiming.model.dto.request.MoimPostReqDto.*;

/*

해당 게시글을 작성한 사람은 수정/삭제가 가능하다.

운영자가 작성할 수 있는 카테고리는 공지, 가입인사, 모임후기, 자유글

참여자가 작성할 수 있는 카테고리는 가입인사, 모임후기, 자유글

모이밍 서비스를 탈퇴한 멤버가 작성한 글은 user명이 --> 탈퇴한 사용자으로 남겨진다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MoimPostService {

    private final MoimMemberRepository moimMemberRepository;
    private final MoimPostRepository moimPostRepository;

    @Transactional
    public void createMoimPost(MoimPostCreateReqDto requestDto, Member member) {

        if (requestDto == null || member == null) {
            throw new MoimingApiException("수신되는 Arguments 들은 Null 일 수 없습니다");
        }

        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(member.getId(), requestDto.getMoimId())
                .orElseThrow(() -> new MoimingApiException("모임원이 아닙니다: 잘못된 요청입니다"));

        if (!moimMember.getMemberState().equals(MoimMemberState.ACTIVE)) {
            throw new MoimingApiException("게시물을 생성할 권한이 없습니다");
        }

        MoimPost post = MoimPost.createMoimPost(requestDto.getPostTitle(), requestDto.getPostContent(), requestDto.getMoimPostCategory()
                , requestDto.getHasPrivateVisibility(), requestDto.getHasFiles()
                , moimMember.getMoim(), moimMember.getMember());

        moimPostRepository.save(post);
    }


    // 모임의 모든 Post 전달 (필요 정보는 사실상 File 빼고 전부 다)
    public List<MoimPost> getMoimPosts(Long moimId, Long lastPostId, MoimPostCategory category, int limit, Member member) {

        if (member == null) {
            throw new MoimingApiException("수신되는 Arguments 들은 Null 일 수 없습니다");
        }

        MoimPost lastPost = null;
        if (lastPostId != null) {
            lastPost = moimPostRepository.findById(lastPostId).orElseThrow(() ->
                    new MoimingApiException("마지막으로 검색한 Post 를 찾을 수 없습니다")
            );
        }


        // 멤버가 구성원인지 확인 필요 - 자체 필드에 대한 제어 로직 - 도메인단에 둘 수가 없음
        boolean moimMemberRequest = true;
        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(member.getId(), moimId).orElse(null);
        if (moimMember == null || !moimMember.getMemberState().equals(MoimMemberState.ACTIVE)) {
            moimMemberRequest = false;
        }

        return moimPostRepository.findByCategoryAndLastPostOrderByDateDesc(moimId, lastPost, category, limit, moimMemberRequest);

    }


    // 특정 Post 의 모든 Data 전달
    public void getMoimPost() {

    }


    // Post 수정
    public void updateMoimPost() {

    }

    // Post 삭제 (댓글 / 답글에 대한 로직 처리 후 최종 진행)
    public void deleteMoimPost() {

    }

}
