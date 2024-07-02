package com.peoplein.moiming.domain.embeddable;

import com.peoplein.moiming.domain.enums.AreaValue;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

import static com.peoplein.moiming.exception.ExceptionValue.*;

/*
 Embeddable 는 Immutable 설계 필요
 Setter 금지
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Area {

    private String state; // 경기도, 서울시 등
    private String city;  // 중구, 강남구 등

    public Area(AreaValue areaState, AreaValue areaCity) {

        if (areaState == null || areaCity == null) {
            throw new MoimingApiException(MOIM_CREATE_INFO_UNAVAILABLE, "State, City 생성 중 NULL 발견");
        }

        if (areaState.getDepth() != 0 && areaCity.getDepth() != 1) {
            throw new MoimingApiException(MOIM_CREATE_INFO_UNAVAILABLE, "State / City 간 Depth 관계 부적합");
        }

        if (!areaState.getStateCities().contains(areaCity)) {
            throw new MoimingApiException(MOIM_CREATE_INFO_UNAVAILABLE, "State 에 포함된 City 가 매핑되지 않았습니다");
        }

        this.state = areaState.getName();
        this.city = areaCity.getName();
    }


    public Area checkAndIssueNewArea(String areaState, String areaCity) {

        AreaValue newAreaState = null;
        AreaValue newAreaCity = null;

        // 수정하려는 항목이 있다면 Convert 후 수정을 진행한다
        if (areaState != null) {
            AreaValue reqAreaState = AreaValue.fromName(areaState);
            if (reqAreaState.getDepth() != 0) {
                throw new MoimingApiException(MOIM_CREATE_INFO_UNAVAILABLE, "State Depth 관계 부적합");
            }
            newAreaState = reqAreaState;
        }

        if (areaCity != null) {
            AreaValue reqAreaCity = AreaValue.fromName(areaCity);
            if (reqAreaCity.getDepth() != 1) {
                throw new MoimingApiException(MOIM_CREATE_INFO_UNAVAILABLE, "City 간 Depth 관계 부적합");
            }
            newAreaCity = reqAreaCity;
        }

        // 비어있는게 있다면 수정 대상이 아닌 항목으로, 기존 것으로 끼워서 넣어준다
        if (newAreaState == null) {
            newAreaState = AreaValue.fromName(this.state);
        }

        if (newAreaCity == null) {
            newAreaCity = AreaValue.fromName(this.city);
        }

        return new Area(newAreaState, newAreaCity);
    }


    // 필드값이 같으면 동일 객체
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Area area = (Area) obj;
        return (state.equals(area.state)) && (city.equals(area.city));
    }
}
