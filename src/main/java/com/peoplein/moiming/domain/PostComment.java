package com.peoplein.moiming.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_comment")
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "post_comment_id")
    private Long id;

    private String commentContent;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /*
     연관관계
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_post_id")
    private MoimPost moimPost;

    public static PostComment createPostComment(String commentContent, Member member, MoimPost moimPost) {
        return new PostComment(commentContent, member, moimPost);
    }

    private PostComment(String commentContent, Member member, MoimPost moimPost) {

        this.commentContent = commentContent;

        /*
         초기화
         */
        this.createdAt = LocalDateTime.now();

        /*
         연관관계 매핑 및 편의 함수
         */
        this.member = member;
        this.moimPost = moimPost;
        this.moimPost.addPostComment(this);
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
