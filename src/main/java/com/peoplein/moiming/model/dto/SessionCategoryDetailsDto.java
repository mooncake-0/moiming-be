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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionCategoryDetailsDto {

    private SessionCategoryType sessionCategoryType;
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
