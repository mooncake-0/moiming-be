package com.peoplein.moiming.model.dto.inner;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.member.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class StateMapperDto<T> {

    private List<T> entities;
    private Map<Member, MoimMemberState> stateMapper;


}
