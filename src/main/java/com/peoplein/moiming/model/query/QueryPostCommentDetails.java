package com.peoplein.moiming.model.query;


import com.peoplein.moiming.model.dto.domain.MoimMemberInfoDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 게시물의 댓글들을 전달할 수 있도록 정보들을 불러오기 위한 Query DTO
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QueryPostCommentDetails {// PostComment
    private Long commentId;
    private String commentContent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Member
    private Long commentCreatorId;

    private MoimMemberInfoDto commentCreatorInfoDto;

    public QueryPostCommentDetails(Long commentId, String commentContent, LocalDateTime createdAt, LocalDateTime updatedAt
            , Long memberId){

        this.commentId = commentId;
        this.commentContent = commentContent;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.commentCreatorId = memberId;
    }

    public void setCommentCreatorInfoDto(MoimMemberInfoDto commentCreatorInfoDto) {
        this.commentCreatorInfoDto = commentCreatorInfoDto;
    }

}
