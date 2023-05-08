package com.peoplein.moiming.model.dto.request;

import com.peoplein.moiming.domain.enums.MemberSessionState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberSessionStateRequestDto {

    private MemberSessionState memberSessionState;

}
