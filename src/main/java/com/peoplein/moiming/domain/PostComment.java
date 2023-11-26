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
public class PostComment extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "post_comment_id")
    private Long id;

    private String commentContent;

    private String content;

    private int depth;

    /*
     연관관계
    */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "member_id")
    private Member member;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "moim_post_id")
    private MoimPost moimPost;


    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "parent_id")
    private PostComment parent;


    public static PostComment createPostComment(String content, Member member, MoimPost moimPost, int depth, PostComment parent) {
        return new PostComment(content, member, moimPost, depth, parent);
    }


    private PostComment(String content, Member member, MoimPost moimPost, int depth, PostComment parent) {

        this.content = content;

        /*
         연관관계 매핑 및 편의 함수
         */
        this.member = member;
        this.moimPost = moimPost;
        this.depth = depth; // 바뀔 수도 있으니까..
        this.parent = parent;

        this.moimPost.addCommentCnt();
        this.moimPost.addPostComment(this);
    }

}
