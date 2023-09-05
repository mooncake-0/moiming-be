package com.peoplein.moiming.domain.embeddable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

/*
 Embeddable 는 Immutable 설계 필요
 Setter 금지
 */
@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Area {
    private String state; // 경기도, 서울시 등
    private String city;  // 중구, 강남구 등


    public Area checkToIssueNewArea(String requestState, String requestCity) {
        String newAreaState = this.getState();
        String newAreaCity = this.getCity();

        if (requestState != null) {
            newAreaState = requestState;
        }

        if (requestCity != null) {
            newAreaCity = requestCity;
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
