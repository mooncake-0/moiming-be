package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.exception.ExceptionValue.COMMON_UPDATE_REQUEST_FAILED;
import static com.peoplein.moiming.exception.ExceptionValue.MOIM_ACT_NOT_AUTHORIZED;

@Entity
@Getter
@Table(name = "moim_post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class MoimPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "moim_post_id")
    private Long id;

    private String postTitle;
    private String postContent;
    @Enumerated(EnumType.STRING)
    private MoimPostCategory moimPostCategory;
    private boolean hasPrivateVisibility;
    private boolean hasFiles;
    private int commentCnt;
    private Long updatedMemberId;

    /*
     연관관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id")
    private Moim moim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "moimPost", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private final List<PostComment> postComments = new ArrayList<>();

    @OneToMany(mappedBy = "moimPost", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private final List<PostFile> postFiles = new ArrayList<>();

    public static MoimPost createMoimPost(String postTitle, String postContent, MoimPostCategory moimPostCategory, boolean hasPrivateVisibility, boolean hasFiles, Moim moim, Member member) {
        return new MoimPost(postTitle, postContent, moimPostCategory, hasPrivateVisibility, hasFiles, moim, member);
    }

    private MoimPost(String postTitle, String postContent, MoimPostCategory moimPostCategory, boolean hasPrivateVisibility, boolean hasFiles, Moim moim, Member member) {

        checkIllegalObjectArguments(moimPostCategory, moim, member);
        checkIllegalStringArguments(postTitle, postContent);

        this.postTitle = postTitle;
        this.postContent = postContent;
        this.moimPostCategory = moimPostCategory;
        this.hasPrivateVisibility = hasPrivateVisibility;
        this.hasFiles = hasFiles;

        // 연관관계
        this.moim = moim;
        this.member = member;

        // 초기화.
        this.commentCnt = 0;
        this.updatedMemberId = member.getId();
    }

    public void addPostComment(PostComment postComment) {
        this.postComments.add(postComment);
    }

    // TODO:: 구체적인 DB 요구사항에 맞춰 checking 값 변경 필요
    public void changeMoimPostInfo(String postTitle, String postContent, MoimPostCategory postCategory, Boolean hasFiles, Boolean hasPrivateVisibility, Long memberId) {

        boolean isChanged = false;

        if (StringUtils.hasText(postTitle)) {
            isChanged = true;
            changePostTitle(postTitle);
        }

        if (StringUtils.hasText(postContent)) {
            isChanged = true;
            changePostContent(postContent);
        }

        if (postCategory != null) {
            isChanged = true;
            changePostCategory(postCategory);
        }

        if (hasFiles != null) {
            isChanged = true;
            this.hasFiles = hasFiles;
        }

        if (hasPrivateVisibility != null) {
            isChanged = true;
            this.hasPrivateVisibility = hasPrivateVisibility;
        }

        if (isChanged) {
            this.updatedMemberId = memberId;
        }else {
            log.info("{}, changeMoimPostInfo :: {}", this.getClass().getName(), "게시물 수정 요청 중 아무 수정이 발생하지 않았습니다");
            throw new MoimingApiException(COMMON_UPDATE_REQUEST_FAILED);
        }

    }


    private void changePostTitle(String postTitle) {
        checkWrongParam(postTitle);
        this.postTitle = postTitle;
    }

    private void changePostContent(String postContent) {
        checkWrongParam(postContent);
        this.postContent = postContent;
    }

    private void changePostCategory(MoimPostCategory moimPostCategory) {
        checkWrongParam(moimPostCategory);
        this.moimPostCategory = moimPostCategory;
    }

    public void changeMember(Member member) {
        if (member == null) {
            throw new MoimingApiException(ExceptionValue.COMMON_INVALID_PARAM);
        }
        this.member = member;
    }


    // 해당 게시물을 조회하려는 유저가 가능한 유저인지 판별한다
    public void checkMemberAccessibility(Optional<MoimMember> moimMemberOp) {

        if (this.hasPrivateVisibility) { // 비공개 게시물일 경우 하기를 탄다
            // moimMember 가 Active 가 아닐경우 + moimPost 가 비공개일 경우 > 거른다
            // moimMember 가 없을 경우 + moimPost 가 비공개일 경우 > 거른다
            if (moimMemberOp.isEmpty() || !moimMemberOp.get().hasActivePermission()) {
                log.error("{}, checkMemberAccessibility :: {}", this.getClass().getName(), "비공개 게시물에 접근할 수 없는 유저입니다");
                throw new MoimingApiException(MOIM_ACT_NOT_AUTHORIZED);
            }
        }
        // public 이면 누구나 가능

    }


    public void addCommentCnt() {
        this.commentCnt += 1;
    }

    public void minusCommentCnt() {
        this.commentCnt -= 1;
    }

    private void checkWrongParam(Object obj) {
        if (obj == null) {
            throw new MoimingApiException(ExceptionValue.COMMON_INVALID_PARAM);
        }

    }

    // Attribute - Class 내 포함 변수
    // Parameter - String name 같은 전달할 녀석들을 의미
    // Arguments - 실제 Code 에서 전달되는 값을 말한다. name = "Test" 면 "Test" 를 말함
    private void checkIllegalObjectArguments(Object... objs) {
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] == null) {
                throw new IllegalArgumentException(i + 1 + "번째 값이 잘못되었습니다");
            }
        }
    }

    private void checkIllegalStringArguments(String... strs) {
        for (int i = 0; i < strs.length; i++) {
            if (!StringUtils.hasText(strs[i])) {
                throw new IllegalArgumentException(i + 1 + "번째 값이 잘못되었습니다");
            }
        }
    }

}