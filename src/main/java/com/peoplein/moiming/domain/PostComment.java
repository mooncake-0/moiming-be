package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.PostCommentReqDto.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_comment")
public class PostComment extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "post_comment_id")
    private Long id;

    private String content;

    private int depth;

    private boolean hasDeleted; // 작성자가 직접 삭제한 댓글 여부
    private boolean reported; // 신고받은 댓글 여부

    private Long updaterId;

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

        this.hasDeleted = false;
        this.reported = false;
        this.moimPost.addCommentCnt();
        this.moimPost.addPostComment(this);
    }


    /*
     getMoim() 에서 객체 탐색 발생
     - Fetch Join 상태임을 권장. 사실 아니여도.. 뭐 큰 상관은 없긴 함
     */
    public void deleteComment(Long deleterId) {

        // 댓글 삭제 권한 - 모임 생성자, 댓글 작성자가 아니면 삭제할 수 없다
        Long moimCreatorId = this.moimPost.getMoim().getCreatorId();

        if (!deleterId.equals(this.member.getId()) && !deleterId.equals(moimCreatorId)) {
            throw new MoimingApiException(MOIM_MEMBER_NOT_AUTHORIZED);
        }

        this.content = ""; // 데이터 삭제
        this.hasDeleted = true;
        this.updaterId = deleterId;
        this.moimPost.minusCommentCnt();
    }


    public void updateComment(PostCommentUpdateReqDto requestDto, Long updaterId) {

        // 댓글 수정 권한 - 댓글 작성자가 아니면 수정할 수 없다
        if (!this.getMember().getId().equals(updaterId)) {
            throw new MoimingApiException(MOIM_MEMBER_NOT_AUTHORIZED);
        }

        if (requestDto.getContent() != null) {
            this.changeContent(requestDto.getContent());
        }

        this.updaterId = updaterId;
    }

    public void changeMember(Member member) {
        if (member == null) {
            throw new MoimingApiException(ExceptionValue.COMMON_INVALID_PARAM);
        }
        this.member = member;
    }

    private void changeContent(String content) {
        this.content = content;
    }
}
