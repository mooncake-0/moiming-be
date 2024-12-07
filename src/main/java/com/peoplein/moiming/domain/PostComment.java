package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.ContentStatus;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.persistence.*;

import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.PostCommentReqDto.*;

@Slf4j
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_comment")
public class PostComment extends BaseEntity{

    @Transient
    public static final String DELETED_CONTENT_DISPLAY = "모임 관리자 혹은 작성자로 인해 삭제처리된 댓글입니다";

    @Transient
    public static final String REPORTED_CONTENT_DISPLAY = "신고로 인해 숨김 처리된 댓글입니다";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "post_comment_id")
    private Long id;

    private String content;

    private int depth;

    private ContentStatus contentStatus;
    private boolean hasDeleted; // 작성자가 직접 삭제한 댓글 여부

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

        this.contentStatus = ContentStatus.ACTIVE;
        this.hasDeleted = false;

        this.moimPost.addCommentCnt();
        this.moimPost.addPostComment(this);
    }

    public void deleteComment(Long deleterId) { // 나머지 일괄 보존
        this.hasDeleted = true;
        this.updaterId = deleterId;
        this.moimPost.minusCommentCnt();
    }

    public void checkInactiveComment() {

        if (this.contentStatus == ContentStatus.HIDDEN) {
            String l = "[" + this.id + "] 는 신고 처리가 완료된 댓글입니다";
            log.warn("{} checkInactiveComment :: {}", this.getClass().getName(), l);
            throw new MoimingApiException(COMMON_REPORTED_CONTENT, l);
        }

        if (this.hasDeleted) {
            String l = "[" + this.id + "] 는 삭제된 댓글입니다";
            log.warn("{} checkInactiveComment :: {}", this.getClass().getName(), l);
            throw new MoimingApiException(COMMON_DELETED_RESOURCE, l);
        }
    }

    public void changeContentStatus(ContentStatus contentStatus, Long updaterId) {
        this.contentStatus = contentStatus;
        this.updaterId = updaterId;
        this.moimPost.minusCommentCnt();
    }

    public void updateComment(PostCommentUpdateReqDto requestDto, Long updaterId) {

        // 댓글 수정 권한 - 댓글 작성자가 아니면 수정할 수 없다
        if (!this.getMember().getId().equals(updaterId)) {
            log.error("{}, updateComment :: {}", this.getClass().getName(), "요청자는 해당 댓글을 수정할 권한이 없습니다");
            throw new MoimingApiException(MOIM_MEMBER_NOT_AUTHORIZED);
        }

        if (StringUtils.hasText(requestDto.getContent())) {
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

    public void displayContentAsDeleted() {
        this.changeContent(DELETED_CONTENT_DISPLAY);
    }

    public void displayContentAsHidden() {
        this.changeContent(REPORTED_CONTENT_DISPLAY);
    }
}
