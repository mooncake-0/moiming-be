package com.peoplein.moiming.model.dto;

import com.peoplein.moiming.domain.enums.SessionCategoryType;
import com.peoplein.moiming.model.dto.domain.SessionCategoryItemDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionCategoryDetailsDto {

    private SessionCategoryType sessionCategoryType;
    private int categoryTotalCost; // 요청시 정합성 검증용, 따로 저장 및 응답 X
    private List<SessionCategoryItemDto> sessionCategoryItems = new ArrayList<>();

    /*
     Constructor -1
     응답용 ReponseModel
     */
    public SessionCategoryDetailsDto(SessionCategoryType sessionCategoryType, List<SessionCategoryItemDto> sessionCategoryItems) {
        this.sessionCategoryType = sessionCategoryType;
        this.sessionCategoryItems = sessionCategoryItems;
    }


}
