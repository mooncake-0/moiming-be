package com.peoplein.moiming.domain.enums;

import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.peoplein.moiming.exception.ExceptionValue.*;

@Slf4j
@Getter
public enum AreaValue {

    STATE_SEOUL("서울시", 0, null),
        CITY_GANGNAM("강남구", 1, STATE_SEOUL),
        CITY_GANGDONG("강동구", 1, STATE_SEOUL),
        CITY_GANGBUK("강북구", 1, STATE_SEOUL),
        CITY_GANGSEO("강서구", 1, STATE_SEOUL),
        CITY_GWANAK("관악구", 1, STATE_SEOUL),
        CITY_GWANGJIN("광진구", 1, STATE_SEOUL),
        CITY_GURO("구로구", 1, STATE_SEOUL),
        CITY_GEUMCHEON("금천구", 1, STATE_SEOUL),
        CITY_NOWON("노원구", 1, STATE_SEOUL),
        CITY_DOBONG("도봉구", 1, STATE_SEOUL),
        CITY_DONGDAEMUN("동대문구", 1, STATE_SEOUL),
        CITY_DONGJAK("동작구", 1, STATE_SEOUL),
        CITY_MAPO("마포구", 1, STATE_SEOUL),
        CITY_SEODAEMUN("서대문구", 1, STATE_SEOUL),
        CITY_SEOCHO("서초구", 1, STATE_SEOUL),
        CITY_SEONGDONG("성동구", 1, STATE_SEOUL),
        CITY_SEONGBUK("성북구", 1, STATE_SEOUL),
        CITY_SONGPA("송파구", 1, STATE_SEOUL),
        CITY_YANGCHEON("양천구", 1, STATE_SEOUL),
        CITY_YEONGDEUNGPO("영등포", 1, STATE_SEOUL),
        CITY_YONGSAN("용산구", 1, STATE_SEOUL),
        CITY_EUNPYEONG("은평구", 1, STATE_SEOUL),
        CITY_JONGNO("종로구", 1, STATE_SEOUL),
        CITY_JUNG("중구", 1, STATE_SEOUL),
        CITY_JUNGNANG("중랑구", 1, STATE_SEOUL);

    private final String name;
    private final int depth;
    private final AreaValue state;
    private final List<AreaValue> stateCities;

    AreaValue(String name, int depth, AreaValue state) {
        this.name = name;
        this.depth = depth;
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

    public static AreaValue fromName(String name) {
        for (AreaValue areaVal : AreaValue.values()) {
            if (areaVal.getName().equals(name)) {
                return areaVal;
            }
        }
        log.error("{}, fromName :: {}", "AreaValue", "[" + name + "] 에 해당하는 객체를 찾을 수 없습니다");
        throw new MoimingApiException(COMMON_MAPPABLE_ENUM_VALUE);
    }


    public static AreaValue fromQueryParam(String name) {
        for (AreaValue aName : AreaValue.values()) {
            if (aName.getName().equals(name)) {
                return aName;
            }
        }
        log.error("{}, fromQueryParam :: {}", "AreaValue",  "존재하지 않는 지역으로 필터링 시도, [" + name + "]");
        throw new MoimingApiException(COMMON_INVALID_REQUEST_PARAM);
    }


    // 검색시 사용, 텍스트 일치는 1차 지역은 제외
    public static List<AreaValue> consistsInArea(String keyword) {
        List<AreaValue> consistingArea = new ArrayList<>();
        for (AreaValue areaValue : AreaValue.values()) {
            if (areaValue.getName().contains(keyword)) {
                if (areaValue.getDepth() == 1) {
                    consistingArea.add(areaValue);
                }
            }
        }
        return consistingArea;
    }

}
