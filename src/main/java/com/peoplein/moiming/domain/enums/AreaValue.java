package com.peoplein.moiming.domain.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


@Getter
public enum AreaValue {

    STATE_SEOUL("서울시", null),
        CITY_GANGNAM("강남구", STATE_SEOUL),
        CITY_GANGDONG("강동구", STATE_SEOUL),
        CITY_GANGBUK("강북구", STATE_SEOUL),
        CITY_GANGSEO("강서구", STATE_SEOUL),
        CITY_GWANAK("관악구", STATE_SEOUL),
        CITY_GWANGJIN("광진구", STATE_SEOUL),
        CITY_GURO("구로구", STATE_SEOUL),
        CITY_GEUMCHEON("금천구", STATE_SEOUL),
        CITY_NOWON("노원구", STATE_SEOUL),
        CITY_DOBONG("도봉구", STATE_SEOUL),
        CITY_DONGDAEMUN("동대문구", STATE_SEOUL),
        CITY_DONGJAK("동작구", STATE_SEOUL),
        CITY_MAPO("마포구", STATE_SEOUL),
        CITY_SEODAEMUN("서대문구", STATE_SEOUL),
        CITY_SEOCHO("서초구", STATE_SEOUL),
        CITY_SEONGDONG("성동구", STATE_SEOUL),
        CITY_SEONGBUK("성북구", STATE_SEOUL),
        CITY_SONGPA("송파구", STATE_SEOUL),
        CITY_YANGCHEON("양천구", STATE_SEOUL),
        CITY_YEONGDEUNGPO("영등포", STATE_SEOUL),
        CITY_YONGSAN("용산구", STATE_SEOUL),
        CITY_EUNPYEONG("은평구", STATE_SEOUL),
        CITY_JONGNO("종로구", STATE_SEOUL),
        CITY_JUNG("중구", STATE_SEOUL),
        CITY_JUNGNANG("중랑구", STATE_SEOUL);

    private final String name;
    private final AreaValue state;
    private final List<AreaValue> stateCities;

    AreaValue(String name, AreaValue state) {
        this.name = name;
        if (state != null) {
            this.state = state;
            this.stateCities = null;
            if (state.stateCities != null) {
                state.stateCities.add(this);
            } else {
                throw new ExceptionInInitializerError("Area Value 가 순서대로 들어가지 않았습니다");
            }
        } else {
            this.state = state; // null 맞음
            this.stateCities = new ArrayList<>();
        }
    }

}
