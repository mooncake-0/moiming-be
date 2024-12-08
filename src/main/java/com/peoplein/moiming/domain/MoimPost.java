package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.ContentStatus;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.file.RelatedFile;
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

import static com.peoplein.moiming.exception.ExceptionValue.*;

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
    private Long updaterId;
    @Enumerated(EnumType.STRING)
    private ContentStatus contentStatus; // 초기화는 ACTIVE 로
    private boolean hasDeleted;

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


    public static MoimPost createMoimPost(String postTitle, String postContent, MoimPostCategory moimPostCategory, boolean hasPrivateVisibility, boolean hasFiles, Moim moim, Member member) {
        return new MoimPost(postTitle, postContent, moimPostCategory, hasPrivateVisibility, hasFiles, moim, member);
    }

    private MoimPost(String postTitle, String postContent, MoimPostCategory moimPostCategory, boolean hasPrivateVisibility, boolean hasFiles, Moim moim, Member member) {

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
        this.contentStatus = ContentStatus.ACTIVE;
        this.hasDeleted = false;
    }

    public void addPostComment(PostComment postComment) {
        this.postComments.add(postComment);
    }

    // TODO:: 구체적인 DB 요구사항에 맞춰 checking 값 변경 필요
    public void changeMoimPostInfo(String postTitle, String postContent, MoimPostCategory postCategory , Boolean hasPrivateVisibility, Long memberId) {

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

        if (hasPrivateVisibility != null) {
            isChanged = true;
            this.hasPrivateVisibility = hasPrivateVisibility;
        }

        if (isChanged) {
            this.updaterId = memberId;
        }
    }

    public void changeHasFiles(Boolean hasFiles, Long updaterId) {
        if (hasFiles != null) {
            this.hasFiles = hasFiles;
            this.updaterId = updaterId;
        }
    }


    private void changePostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    private void changePostContent(String postContent) {
        this.postContent = postContent;
    }

    private void changePostCategory(MoimPostCategory moimPostCategory) {
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


    public void deletePost(Long deleterId) {
        this.hasDeleted = true;
        this.updaterId = deleterId;
    }

    public void checkInactivePost() { // checkDeleted 변경

        if (this.contentStatus == ContentStatus.HIDDEN) {
            String l = "[" + this.id + "] 는 신고 처리가 완료된 게시물입니다";
            log.warn("{} checkInactivePost :: {}", this.getClass().getName(), l);
            throw new MoimingApiException(COMMON_REPORTED_CONTENT, l);
        }

        if (this.hasDeleted) {
            String l = "[" + this.id + "] 는 삭제된 게시물입니다";
            log.warn("{} checkInactivePost :: {}", this.getClass().getName(), l);
            throw new MoimingApiException(COMMON_DELETED_RESOURCE, l);
        }
    }

    public void changeContentStatus(ContentStatus contentStatus, Long updaterId) {
        this.contentStatus = contentStatus;
        this.updaterId = updaterId;
    }

}