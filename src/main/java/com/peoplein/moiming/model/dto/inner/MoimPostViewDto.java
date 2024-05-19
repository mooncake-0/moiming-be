package com.peoplein.moiming.model.dto.inner;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.file.RelatedFile;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class MoimPostViewDto {

    private List<MoimPost> moimPosts;
    private Map<Long, MoimMemberState> postCreatorStates;
    private Map<Long, RelatedFile> thumbnailFiles;

}
