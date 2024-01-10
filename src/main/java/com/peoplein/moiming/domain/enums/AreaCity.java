package com.peoplein.moiming.domain.enums;

import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.peoplein.moiming.exception.ExceptionValue.COMMON_INVALID_SITUATION;

@Slf4j
@Getter
@AllArgsConstructor
public enum AreaCity {

    // 서울시
    GANGNAM("강남구"),
    GANGDONG("강동구"),
    GANGBUK("강북구"),
    GANGSEO("강서구"),
    GWANAK("관악구"),
    GWANGJIN("광진구"),
    GURO("구로구"),
    GEUMCHEON("금천구"),
    NOWON("노원구"),
    DOBONG("도봉구"),
    DONGDAEMUN("동대문구"),
    DONGJAK("동작구"),
    MAPO("마포구"),
    SEODAEMUN("서대문구"),
    SEOCHO("서초구"),
    SEONGDONG("성동구"),
    SEONGBUK("성북구"),
    SONGPA("송파구"),
    YANGCHEON("양천구"),
    YEONGDEUNGPO("영등포"),
    YONGSAN("용산구"),
    EUNPYEONG("은평구"),
    JONGNO("종로구"),
    JUNG("중구"),
    JUNGNANG("중랑구");

    private final String value;

    public static AreaCity fromValue(String value) {
        for (AreaCity cityName : AreaCity.values()) {
            if (cityName.getValue().equals(value)) {
                return cityName;
            }
        }
        log.error("{}, {}", "존재하지 않는 City 전환 시도, [" + value + "], C999", COMMON_INVALID_SITUATION.getErrMsg());
        throw new MoimingApiException(COMMON_INVALID_SITUATION);
    }

}
