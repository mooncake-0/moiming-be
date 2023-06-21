package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.model.dto.domain.PostCommentDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Table(name = "moim_post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class MoimPost extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "moim_post_id")
    private Long id;

    private String postTitle;
    private String postContent;

    @Enumerated(value = EnumType.STRING)
    private MoimPostCategory moimPostCategory;
    private boolean isNotice;
    private boolean hasFiles;

    private String updatedUid;

    /*
     연관관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id")
    private Moim moim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "moimPost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<PostComment> postComments = new ArrayList<>();

    @OneToMany(mappedBy = "moimPost", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private final List<PostFile> postFiles = new ArrayList<>();

    public static MoimPost createMoimPost(String postTitle, String postContent, MoimPostCategory moimPostCategory, boolean isNotice, boolean hasFiles, Moim moim, Member member) {
        return new MoimPost(postTitle, postContent, moimPostCategory, isNotice, hasFiles, moim, member);
    }

    private MoimPost(String postTitle, String postContent, MoimPostCategory moimPostCategory, boolean isNotice, boolean hasFiles, Moim moim, Member member) {

        DomainChecker.checkRightString(this.getClass().getName(), false, postTitle, postContent);
        DomainChecker.checkWrongObjectParams(this.getClass().getName(), moimPostCategory, isNotice, hasFiles, moim, member);

        this.postTitle = postTitle;
        this.postContent = postContent;
        this.moimPostCategory = moimPostCategory;
        this.isNotice = isNotice;
        this.hasFiles = hasFiles;

        // 연관관계
        this.moim = moim;
        this.member = member;

        // 초기화.
        this.updatedUid = member.getUid();
    }

    public void addPostComment(PostComment postComment) {
        DomainChecker.checkWrongObjectParams(this.getClass().getName() + ", addPostComment()", postComment);
        this.postComments.add(postComment);
    }

    public void removePostComment(PostComment postComment) {
        this.postComments.remove(postComment);
    }

    public void changePostTitle(String postTitle) {
        DomainChecker.checkRightString(this.getClass().getName(), false, postTitle);
        this.postTitle = postTitle;
    }

    public void changePostContent(String postContent) {
        DomainChecker.checkRightString(this.getClass().getName(), false, postContent);
        this.postContent = postContent;
    }


    public void changePostCategory(MoimPostCategory moimPostCategory) {
        DomainChecker.checkWrongObjectParams(this.getClass().getName(), moimPostCategory);
        this.moimPostCategory = moimPostCategory;
    }


    public void setNotice(boolean notice) {
        this.isNotice = notice;
    }


    // 업데이트 했을 때, UID 바뀌는지
    // 업데이트 안했을 때, UID 안 바뀌는지
    public boolean update(String postTitle,
                       String postContent,
                       boolean isNotice,
                       MoimPostCategory moimPostCategory,
                       String updatedUid) {

        checkWrongArgument(postTitle, postContent, isNotice, moimPostCategory, updatedUid);

        if (!isChangedAny(postTitle, postContent, isNotice, moimPostCategory)) {
            return false;
        }

        this.postTitle = postTitle;
        this.postContent = postContent;
        this.isNotice = isNotice;
        this.moimPostCategory = moimPostCategory;
        this.updatedUid = updatedUid;

        return true;
    }

    public void checkWrongArgument(String postTitle,
                                    String postContent,
                                    boolean isNotice,
                                    MoimPostCategory moimPostCategory,
                                    String updatedUid) {

        if (!StringUtils.hasText(postTitle) ||
                !StringUtils.hasText(postContent) ||
                !StringUtils.hasText(updatedUid) ||
                Objects.isNull(moimPostCategory)) {
            throw new IllegalArgumentException();
        }
    }

    private boolean isChangedAny(String postTitle,
                                 String postContent,
                                 boolean isNotice,
                                 MoimPostCategory moimPostCategory) {

        return !this.postTitle.equals(postTitle) ||
                !this.postContent.equals(postContent) ||
                this.isNotice != isNotice ||
                !this.moimPostCategory.equals(moimPostCategory);
    }

    public void delete(MemberMoimLinker memberMoimLinker) {

        System.out.println("HERE1");
        if (!canDelete(memberMoimLinker)) {
            log.error("삭제할 권한이 없는 유저의 요청입니다");
            throw new RuntimeException("삭제할 권한이 없는 유저의 요청입니다");
        }

        this.moim = null;
        this.member = null;
    }

    private boolean canDelete(MemberMoimLinker memberMoimLinker) {
        MoimRoleType moimRoleType = memberMoimLinker.getMoimRoleType();
        return moimRoleType.equals(MoimRoleType.LEADER) ||
                moimRoleType.equals(MoimRoleType.MANAGER);
    }

}
