package com.peoplein.moiming.model.dto.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostCommentDto {

    private Long commentId;
    private String commentContent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isCreatorCurMember;
    private MoimMemberInfoDto commentCreatorInfo;

    /*
     Constructor -1
     각 필드 주입을 통해 Dto 를 생성한다
     */
    public PostCommentDto(Long commentId, String commentContent, LocalDateTime createdAt, LocalDateTime updatedAt, boolean isCreatorCurMember, MoimMemberInfoDto commentCreatorInfo) {

        this.commentId = commentId;
        this.commentContent = commentContent;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isCreatorCurMember = isCreatorCurMember;
        this.commentCreatorInfo = commentCreatorInfo;
    }

    public void setCommentCreatorInfo(MoimMemberInfoDto commentCreatorInfo) {
        this.commentCreatorInfo = commentCreatorInfo;
    }
}