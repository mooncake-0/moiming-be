package com.peoplein.moiming.service.input;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.Schedule;
import com.peoplein.moiming.domain.enums.SessionCategoryType;
import com.peoplein.moiming.domain.fixed.SessionCategory;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Builder
public class MoimSessionServiceInput {


    private Moim moimOfNewMoimSession;

    private Schedule scheduleOfNewMoimSession;

    // 모든 카테고리를 그냥 한번에 준비하자. 얼마 되지도 않고, 로직 복잡하게 가져가지 말고
    private List<SessionCategory> allSessionCategories = new ArrayList<>();

    // 전달받은 참여 MemberId 들을 통해 참여 Member 들을 가져온다
    private List<Member> allSessionMembers = new ArrayList<>();

    public SessionCategory getSessionCategoryByType(SessionCategoryType sessionCategoryType) {
        return allSessionCategories.stream().filter(sessionCategory -> sessionCategory.getCategoryType().equals(sessionCategoryType)).findAny()
                .orElseThrow(() -> new RuntimeException("SessionCategoryList 를 불러오는데 오류가 발생했습니다"));
    }

    public Member getMemberById(Long memberId) {
        return allSessionMembers.stream().filter(member -> member.getId().equals(memberId)).findAny()
                .orElseThrow(() -> new RuntimeException("해당 id: " + memberId + "의 멤버를 찾을 수 없습니다"));
    }

}

/*
  Service 계층에서 활용할 Entity 들을 준비시켜준다
 */
