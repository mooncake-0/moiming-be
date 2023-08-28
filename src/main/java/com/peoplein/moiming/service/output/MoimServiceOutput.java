package com.peoplein.moiming.service.output;

import com.peoplein.moiming.domain.MemberMoimLinker;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.MoimCategoryLinker;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MoimServiceOutput {

    private boolean isAnyUpdated;
    private Moim createdMoim;
    private List<MoimCategoryLinker> createdMoimCategoryLinkers;
    private MemberMoimLinker createdMemberMoimLinker;
}
