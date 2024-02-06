package com.peoplein.moiming.model.dto.inner;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.member.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PostDetailsInnerDto {

    @Getter
    @AllArgsConstructor
    public static class PostDetailsDto {

        private MoimPost moimPost;
        private Map<Long, MoimMemberState> memberStates;
        private List<PostComment> parentComments;
        private Map<Long, List<PostComment>> childCommentsMap;

    }


    @Getter
    @AllArgsConstructor
    public static class PostCommentDetailsDto {

        private Set<Long> commentCreatorIds;
        private List<PostComment> parentComments;
        private Map<Long, List<PostComment>> childCommentsMap;
    }

}
