package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.domain.moim.Moim;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private MoimPostCategory moimPostCategory;
    private boolean isNotice;
    private boolean hasFiles;

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

    @OneToMany(mappedBy = "moimPost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<PostComment> postComments = new ArrayList<>();

    @OneToMany(mappedBy = "moimPost", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private final List<PostFile> postFiles = new ArrayList<>();

    public static MoimPost createMoimPost(String postTitle, String postContent, MoimPostCategory moimPostCategory, boolean isNotice, boolean hasFiles, Moim moim, Member member) {
        return new MoimPost(postTitle, postContent, moimPostCategory, isNotice, hasFiles, moim, member);
    }

    private MoimPost(String postTitle, String postContent, MoimPostCategory moimPostCategory, boolean isNotice, boolean hasFiles, Moim moim, Member member) {

        this.postTitle = postTitle;
        this.postContent = postContent;
        this.moimPostCategory = moimPostCategory;
        this.isNotice = isNotice;
        this.hasFiles = hasFiles;

        // 연관관계
        this.moim = moim;
        this.member = member;

        // 초기화.
        this.updatedMemberId = member.getId();
    }

    public void addPostComment(PostComment postComment) {
        this.postComments.add(postComment);
    }

    public void removePostComment(PostComment postComment) {
        this.postComments.remove(postComment);
    }

    public void changePostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public void changePostContent(String postContent) {
        this.postContent = postContent;
    }


    public void changePostCategory(MoimPostCategory moimPostCategory) {
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
                          Long updatedMemberId) {

        checkWrongArgument(postTitle, postContent, isNotice, moimPostCategory, updatedMemberId);

        if (!isChangedAny(postTitle, postContent, isNotice, moimPostCategory)) {
            return false;
        }

        this.postTitle = postTitle;
        this.postContent = postContent;
        this.isNotice = isNotice;
        this.moimPostCategory = moimPostCategory;
        this.updatedMemberId = updatedMemberId;

        return true;
    }

    public void checkWrongArgument(String postTitle,
                                   String postContent,
                                   boolean isNotice,
                                   MoimPostCategory moimPostCategory,
                                   Long updatedMemberId) {

        if (!StringUtils.hasText(postTitle) ||
                !StringUtils.hasText(postContent) ||
                Objects.isNull(moimPostCategory) ||
                Objects.isNull(updatedMemberId)) {
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

    public void delete(MoimMember moimMember) {

        System.out.println("HERE1");
        if (!canDelete(moimMember)) {
            log.error("삭제할 권한이 없는 유저의 요청입니다");
            throw new RuntimeException("삭제할 권한이 없는 유저의 요청입니다");
        }

        this.moim = null;
        this.member = null;
    }

    private boolean canDelete(MoimMember moimMember) {
        MoimMemberRoleType moimMemberRoleType = moimMember.getMoimMemberRoleType();
        return moimMemberRoleType.equals(MoimMemberRoleType.MANAGER);
    }

}