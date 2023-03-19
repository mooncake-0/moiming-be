package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.model.dto.domain.PostCommentDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "moim_post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimPost {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "moim_post_id")
    private Long id;

    private String postTitle;
    private String postContent;
    private MoimPostCategory moimPostCategory;
    private boolean isNotice;
    private boolean hasFiles;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
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

    @OneToMany(mappedBy = "moimPost", cascade = CascadeType.ALL)
    private List<PostComment> postComments = new ArrayList<>();

    @OneToMany(mappedBy = "moimPost", cascade = CascadeType.ALL)
    private List<PostFile> postFiles = new ArrayList<>();

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

        // 초기화
        this.createdAt = LocalDateTime.now();
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

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUpdatedUid(String updatedUid) {
        this.updatedUid = updatedUid;
    }
}
