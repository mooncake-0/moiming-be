package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "moimPost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
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

    public void addCommentCnt() {
        this.commentCnt += 1;
    }

    public void minusCommentCnt() {
        this.commentCnt -= 1;
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