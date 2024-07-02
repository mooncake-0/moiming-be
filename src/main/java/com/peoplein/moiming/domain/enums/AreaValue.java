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

    STATE_SEOUL("서울", 0, null),
        CITY_GANGNAM("강남구", 1, STATE_SEOUL),
        CITY_GANGDONG("강동구", 1, STATE_SEOUL),
        CITY_GANGBUK("강북구", 1, STATE_SEOUL),
        CITY_SEOUL_GANGSEO("강서구", 1, STATE_SEOUL),
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
        CITY_YEONGDEUNGPO("영등포구", 1, STATE_SEOUL),
        CITY_YONGSAN("용산구", 1, STATE_SEOUL),
        CITY_EUNPYEONG("은평구", 1, STATE_SEOUL),
        CITY_JONGNO("종로구", 1, STATE_SEOUL),
        CITY_SEOUL_JUNG("중구", 1, STATE_SEOUL),
        CITY_JUNGNANG("중랑구", 1, STATE_SEOUL),


    STATE_GYEONGGI("경기", 0, null),
        CITY_GAPYEONG("가평군", 1, STATE_GYEONGGI),
        CITY_GOYANG("고양시", 1, STATE_GYEONGGI),
        CITY_GWACHEON("과천시", 1, STATE_GYEONGGI),
        CITY_GWANGMYEONG("광명시", 1, STATE_GYEONGGI),
        CITY_GWANGJU("광주시", 1, STATE_GYEONGGI),
        CITY_GURI("구리시", 1, STATE_GYEONGGI),
        CITY_GUNPO("군포시", 1, STATE_GYEONGGI),
        CITY_GIMPO("김포시", 1, STATE_GYEONGGI),
        CITY_NAMYANGJU("남양주시", 1, STATE_GYEONGGI),
        CITY_DONGDUCHEON("동두천시", 1, STATE_GYEONGGI),
        CITY_BUCHEON("부천시", 1, STATE_GYEONGGI),
        CITY_SEONGNAM("성남시", 1, STATE_GYEONGGI),
        CITY_SUWON("수원시", 1, STATE_GYEONGGI),
        CITY_SIHEUNG("시흥시", 1, STATE_GYEONGGI),
        CITY_ANSAN("안산시", 1, STATE_GYEONGGI),
        CITY_ANSEONG("안성시", 1, STATE_GYEONGGI),
        CITY_ANYANG("안양시", 1, STATE_GYEONGGI),
        CITY_YANGJU("양주시", 1, STATE_GYEONGGI),
        CITY_YANGPYEONG("양평군", 1, STATE_GYEONGGI),
        CITY_YEOJU("여주시", 1, STATE_GYEONGGI),
        CITY_YEONCHEON("연천군", 1, STATE_GYEONGGI),
        CITY_OSAN("오산시", 1, STATE_GYEONGGI),
        CITY_YONGIN("용인시", 1, STATE_GYEONGGI),
        CITY_UIWANG("의왕시", 1, STATE_GYEONGGI),
        CITY_UIJEONGBU("의정부시", 1, STATE_GYEONGGI),
        CITY_ICHEON("이천시", 1, STATE_GYEONGGI),
        CITY_PAJU("파주시", 1, STATE_GYEONGGI),
        CITY_PYEONGTAEK("평택시", 1, STATE_GYEONGGI),
        CITY_POCHEON("포천시", 1, STATE_GYEONGGI),
        CITY_HANAM("하남시", 1, STATE_GYEONGGI),
        CITY_HWASEONG("화성시", 1, STATE_GYEONGGI),


    STATE_INCHEON("인천", 0, null),
        CITY_GANGHWA("강화군", 1, STATE_INCHEON),
        CITY_GYEYANG("계양구", 1, STATE_INCHEON),
        CITY_NAMDONG("남동구", 1, STATE_INCHEON),
        CITY_INCHEON_DONG("동구", 1, STATE_INCHEON),
        CITY_MICHUHOL("미추홀구", 1, STATE_INCHEON),
        CITY_BUPYEONG("부평구", 1, STATE_INCHEON),
        CITY_INCHEON_SEO("서구", 1, STATE_INCHEON),
        CITY_YEONSU("연수구", 1, STATE_INCHEON),
        CITY_ONGJIN("옹진군", 1, STATE_INCHEON),
        CITY_INCHEON_JUNG("중구", 1, STATE_INCHEON),


    STATE_GANGWON("강원", 0, null),
        CITY_GANGNEUNG("강릉시", 1, STATE_GANGWON),
        CITY_GANGWON_GOSEONG("고성군", 1, STATE_GANGWON),
        CITY_DONGHAE("동해시", 1, STATE_GANGWON),
        CITY_SAMCHEOK("삼척시", 1, STATE_GANGWON),
        CITY_SOKCHO("속초시", 1, STATE_GANGWON),
        CITY_YANGGU("양구군", 1, STATE_GANGWON),
        CITY_YANGYANG("양양군", 1, STATE_GANGWON),
        CITY_YEONGWOL("영월군", 1, STATE_GANGWON),
        CITY_WONJU("원주시", 1, STATE_GANGWON),
        CITY_INJE("인제군", 1, STATE_GANGWON),
        CITY_JEONGSEON("정선군", 1, STATE_GANGWON),
        CITY_CHEORWON("철원군", 1, STATE_GANGWON),
        CITY_CHUNCHEON("춘천시", 1, STATE_GANGWON),
        CITY_TAEBAEK("태백시", 1, STATE_GANGWON),
        CITY_PYEONGCHANG("평창군", 1, STATE_GANGWON),
        CITY_HONGCHEON("홍천군", 1, STATE_GANGWON),
        CITY_HWACHEON("화천군", 1, STATE_GANGWON),
        CITY_HOENGSEONG("횡성군", 1, STATE_GANGWON),


    STATE_DAEJEON("대전", 0, null),
        CITY_DAEDEOK("대덕구", 1, STATE_DAEJEON),
        CITY_DAEJEON_DONG("동구", 1, STATE_DAEJEON),
        CITY_DAEJEON_SEO("서구", 1, STATE_DAEJEON),
        CITY_YUSEONG("유성구", 1, STATE_DAEJEON),
        CITY_DAEJEON_JUNG("중구", 1, STATE_DAEJEON),


    STATE_SEJONG("세종", 0, null),
        CITY_GOUN("고운동", 1, STATE_SEJONG),
        CITY_GEUMNAM("금남면", 1, STATE_SEJONG),
        CITY_NASEONG("나성동", 1, STATE_SEJONG),
        CITY_DAJEONG("다정동", 1, STATE_SEJONG),
        CITY_DAEPYEONG("대평동", 1, STATE_SEJONG),
        CITY_DODAM("도담동", 1, STATE_SEJONG),
        CITY_BANGOK("반곡동", 1, STATE_SEJONG),
        CITY_BORAM("보람동", 1, STATE_SEJONG),
        CITY_BUGANG("부강면", 1, STATE_SEJONG),
        CITY_SAEROM("새롬동", 1, STATE_SEJONG),
        CITY_SODAM("소담동", 1, STATE_SEJONG),
        CITY_SOJEONG("소정면", 1, STATE_SEJONG),
        CITY_AREUM("아름동", 1, STATE_SEJONG),
        CITY_EOJIN("어진동", 1, STATE_SEJONG),
        CITY_YEONGI("연기면", 1, STATE_SEJONG),
        CITY_YEONDONG("연동면", 1, STATE_SEJONG),
        CITY_YEONSEO("연서면", 1, STATE_SEJONG),
        CITY_JANGGUN("장군면", 1, STATE_SEJONG),
        CITY_JEONDONG("전동면", 1, STATE_SEJONG),
        CITY_JEONUI("전의면", 1, STATE_SEJONG),
        CITY_JOCHIWON("조치원읍", 1, STATE_SEJONG),
        CITY_JONGCHON("종촌동", 1, STATE_SEJONG),
        CITY_HANSOL("한솔동", 1, STATE_SEJONG),
        CITY_HAEMIL("해밀동", 1, STATE_SEJONG),


    STATE_CHUNGBUK("충북", 0, null),
        CITY_GOESAN("괴산군", 1, STATE_CHUNGBUK),
        CITY_DANYANG("단양군", 1, STATE_CHUNGBUK),
        CITY_BOEUN("보은군", 1, STATE_CHUNGBUK),
        CITY_YEONGDONG("영동군", 1, STATE_CHUNGBUK),
        CITY_OKCHEON("옥천군", 1, STATE_CHUNGBUK),
        CITY_EUMSEONG("음성군", 1, STATE_CHUNGBUK),
        CITY_JECHEON("제천시", 1, STATE_CHUNGBUK),
        CITY_JEUNGPYEONG("증평군", 1, STATE_CHUNGBUK),
        CITY_JINCHEON("진천군", 1, STATE_CHUNGBUK),
        CITY_CHEONGJU("청주시", 1, STATE_CHUNGBUK),
        CITY_CHUNGJU("충주시", 1, STATE_CHUNGBUK),


    STATE_CHUNGNAM("충남", 0, null),
        CITY_GYERYONG("계룡시", 1, STATE_CHUNGNAM),
        CITY_GONGJU("공주시", 1, STATE_CHUNGNAM),
        CITY_GEUMSAN("금산군", 1, STATE_CHUNGNAM),
        CITY_NONSAN("논산시", 1, STATE_CHUNGNAM),
        CITY_DANGJIN("당진시", 1, STATE_CHUNGNAM),
        CITY_BORYEONG("보령시", 1, STATE_CHUNGNAM),
        CITY_BUYEO("부여군", 1, STATE_CHUNGNAM),
        CITY_SEOSAN("서산시", 1, STATE_CHUNGNAM),
        CITY_SEOCHEON("서천군", 1, STATE_CHUNGNAM),
        CITY_ASAN("아산시", 1, STATE_CHUNGNAM),
        CITY_YESAN("예산군", 1, STATE_CHUNGNAM),
        CITY_CHEONAN("천안시", 1, STATE_CHUNGNAM),
        CITY_CHEONGYANG("청양군", 1, STATE_CHUNGNAM),
        CITY_TAEAN("태안군", 1, STATE_CHUNGNAM),
        CITY_HONGSEONG("홍성군", 1, STATE_CHUNGNAM),


    STATE_GWANGJU("광주", 0, null),
        CITY_GWANGJU_NAM("남구", 1, STATE_GWANGJU),
        CITY_GWANGJU_DONG("동구", 1, STATE_GWANGJU),
        CITY_GWANGJU_BUK("북구", 1, STATE_GWANGJU),
        CITY_GWANGJU_SEO("서구", 1, STATE_GWANGJU),


    STATE_JEONBUK("전북", 0, null),
        CITY_GOCHANG("고창군", 1, STATE_JEONBUK),
        CITY_GUNSAN("군산시", 1, STATE_JEONBUK),
        CITY_GIMJE("김제시", 1, STATE_JEONBUK),
        CITY_JEONBUK_NAMWON("남원시", 1, STATE_JEONBUK),
        CITY_MUJU("무주군", 1, STATE_JEONBUK),
        CITY_BUAN("부안군", 1, STATE_JEONBUK),
        CITY_SUNCHANG("순창군", 1, STATE_JEONBUK),
        CITY_WANJU("완주군", 1, STATE_JEONBUK),
        CITY_IKSAN("익산시", 1, STATE_JEONBUK),
        CITY_IMSIL("임실군", 1, STATE_JEONBUK),
        CITY_JANGSU("장수군", 1, STATE_JEONBUK),
        CITY_JEONJU("전주시", 1, STATE_JEONBUK),
        CITY_JEONGEUP("정읍시", 1, STATE_JEONBUK),
        CITY_JINAN("진안군", 1, STATE_JEONBUK),


    STATE_JEONNAM("전남", 0, null),
        CITY_GANGJIN("강진군", 1, STATE_JEONNAM),
        CITY_GOHEUNG("고흥군", 1, STATE_JEONNAM),
        CITY_GOKSEONG("곡성군", 1, STATE_JEONNAM),
        CITY_GWANGYANG("광양시", 1, STATE_JEONNAM),
        CITY_GURYE("구례군", 1, STATE_JEONNAM),
        CITY_NAJU("나주시", 1, STATE_JEONNAM),
        CITY_DAMYANG("담양군", 1, STATE_JEONNAM),
        CITY_MOKPO("목포시", 1, STATE_JEONNAM),
        CITY_MUAN("무안군", 1, STATE_JEONNAM),
        CITY_BOSEONG("보성군", 1, STATE_JEONNAM),
        CITY_SUNCHEON("순천시", 1, STATE_JEONNAM),
        CITY_SINAN("신안군", 1, STATE_JEONNAM),
        CITY_YEOSU("여수시", 1, STATE_JEONNAM),
        CITY_YEONGGWANG("영광군", 1, STATE_JEONNAM),
        CITY_YEONGAM("영암군", 1, STATE_JEONNAM),
        CITY_WANDO("완도군", 1, STATE_JEONNAM),
        CITY_JANGSEONG("장성군", 1, STATE_JEONNAM),
        CITY_JANGHEUNG("장흥군", 1, STATE_JEONNAM),
        CITY_JINDO("진도군", 1, STATE_JEONNAM),
        CITY_HAMPYEONG("함평군", 1, STATE_JEONNAM),
        CITY_HAENAM("해남군", 1, STATE_JEONNAM),
        CITY_HWASUN("화순군", 1, STATE_JEONNAM),


    STATE_DAEGU("대구", 0, null),
        CITY_GUNWI("군위군", 1, STATE_DAEGU),
        CITY_DAEGU_NAM("남구", 1, STATE_DAEGU),
        CITY_DALSEO("달서구", 1, STATE_DAEGU),
        CITY_DALSEONG("달성군", 1, STATE_DAEGU),
        CITY_DAEGU_DONG("동구", 1, STATE_DAEGU),
        CITY_DAEGU_BUK("북구", 1, STATE_DAEGU),
        CITY_DAEGU_SEO("서구", 1, STATE_DAEGU),
        CITY_SUSEONG("수성구", 1, STATE_DAEGU),
        CITY_DAEGU_JUNG("중구", 1, STATE_DAEGU),


    STATE_BUSAN("부산", 0, null),
        CITY_BUSAN_GANGSEO("강서구", 1, STATE_BUSAN),
        CITY_GEUMJEONG("금정구", 1, STATE_BUSAN),
        CITY_GIJANG("기장군", 1, STATE_BUSAN),
        CITY_BUSAN_NAM("남구", 1, STATE_BUSAN),
        CITY_BUSAN_DONG("동구", 1, STATE_BUSAN),
        CITY_DONGNAE("동래구", 1, STATE_BUSAN),
        CITY_BUSANJIN("부산진구", 1, STATE_BUSAN),
        CITY_BUSAN_BUK("북구", 1, STATE_BUSAN),
        CITY_SASANG("사상구", 1, STATE_BUSAN),
        CITY_SAHAGU("사하구", 1, STATE_BUSAN),
        CITY_BUSAN_SEO("서구", 1, STATE_BUSAN),
        CITY_SUYEONG("수영구", 1, STATE_BUSAN),
        CITY_YEONJE("연제구", 1, STATE_BUSAN),
        CITY_YEONGDO("영도구", 1, STATE_BUSAN),
        CITY_BUSAN_JUNG("중구", 1, STATE_BUSAN),
        CITY_HAEUNDAE("해운대구", 1, STATE_BUSAN),


    STATE_ULSAN("울산", 0, null),
        CITY_ULSAN_NAM("남구", 1, STATE_ULSAN),
        CITY_ULSAN_DONG("동구", 1, STATE_ULSAN),
        CITY_ULSAN_BUK("북구", 1, STATE_ULSAN),
        CITY_ULSAN_ULJU("울주군", 1, STATE_ULSAN),
        CITY_ULSAN_JUNG("중구", 1, STATE_ULSAN),


    STATE_GYEONGBUK("경북", 0, null),
        CITY_GYEONGSAN("경산시", 1, STATE_GYEONGBUK),
        CITY_GYEONGJU("경주시", 1, STATE_GYEONGBUK),
        CITY_GORYEONG("고령군", 1, STATE_GYEONGBUK),
        CITY_GUMI("구미시", 1, STATE_GYEONGBUK),
        CITY_GIMCHEON("김천시", 1, STATE_GYEONGBUK),
        CITY_MUNGYEONG("문경시", 1, STATE_GYEONGBUK),
        CITY_BONGHWA("봉화군", 1, STATE_GYEONGBUK),
        CITY_SANGJU("상주시", 1, STATE_GYEONGBUK),
        CITY_SEONGJU("성주군", 1, STATE_GYEONGBUK),
        CITY_ANDONG("안동시", 1, STATE_GYEONGBUK),
        CITY_YEONGDEOK("영덕군", 1, STATE_GYEONGBUK),
        CITY_YEONGYANG("영양군", 1, STATE_GYEONGBUK),
        CITY_YEONGJU("영주시", 1, STATE_GYEONGBUK),
        CITY_YEONGCHEON("영천시", 1, STATE_GYEONGBUK),
        CITY_YECHEON("예천군", 1, STATE_GYEONGBUK),
        CITY_ULLEUNG("울릉군", 1, STATE_GYEONGBUK),
        CITY_ULJIN("울진군", 1, STATE_GYEONGBUK),
        CITY_UISEONG("의성군", 1, STATE_GYEONGBUK),
        CITY_CHEONGDO("청도군", 1, STATE_GYEONGBUK),
        CITY_CHEONGSONG("청송군", 1, STATE_GYEONGBUK),
        CITY_CHILGOK("칠곡군", 1, STATE_GYEONGBUK),
        CITY_POHANG("포항시", 1, STATE_GYEONGBUK),


    STATE_GYEONGNAM("경남", 0, null),
        CITY_GEOJE("거제시", 1, STATE_GYEONGNAM),
        CITY_GEOCHANG("거창군", 1, STATE_GYEONGNAM),
        CITY_GYEONGNAM_GOSEONG("고성군", 1, STATE_GYEONGNAM),
        CITY_GIMHAE("김해시", 1, STATE_GYEONGNAM),
        CITY_NAMHAE("남해군", 1, STATE_GYEONGNAM),
        CITY_MIRYANG("밀양시", 1, STATE_GYEONGNAM),
        CITY_SACHEON("사천시", 1, STATE_GYEONGNAM),
        CITY_SANCHEONG("산청군", 1, STATE_GYEONGNAM),
        CITY_YANGSAN("양산시", 1, STATE_GYEONGNAM),
        CITY_UIRYEONG("의령군", 1, STATE_GYEONGNAM),
        CITY_JINJU("진주시", 1, STATE_GYEONGNAM),
        CITY_CHANGNYEONG("창녕군", 1, STATE_GYEONGNAM),
        CITY_CHANGWON("창원시", 1, STATE_GYEONGNAM),
        CITY_TONGYEONG("통영시", 1, STATE_GYEONGNAM),
        CITY_HADONG("하동군", 1, STATE_GYEONGNAM),
        CITY_HAMAN("함안군", 1, STATE_GYEONGNAM),
        CITY_HAMYANG("함양군", 1, STATE_GYEONGNAM),
        CITY_HAPCHEON("합천군", 1, STATE_GYEONGNAM),


    STATE_JEJU("제주", 0, null),
        CITY_JEJU_SINAE("제주시(시내동)", 1, STATE_JEJU),
        CITY_GUJWA("제주시 구좌읍", 1, STATE_JEJU),
        CITY_AEWOL("제주시 애월읍", 1, STATE_JEJU),
        CITY_UDO("제주시 우도면", 1, STATE_JEJU),
        CITY_JOCHEON("제주시 조천읍", 1, STATE_JEJU),
        CITY_CHUJA("제주시 추자면", 1, STATE_JEJU),
        CITY_HANGYEONG("제주시 한경면", 1, STATE_JEJU),
        CITY_HANRIM("제주시 한림읍", 1, STATE_JEJU),
        CITY_SEO_SINAE("서귀포시(시내동)", 1, STATE_JEJU),
        CITY_SEO_NAMWON("서귀포시 남원읍", 1, STATE_JEJU),
        CITY_DAEJEONG("서귀포시 대정읍", 1, STATE_JEJU),
        CITY_SEONGSAN("서귀포시 성산읍", 1, STATE_JEJU),
        CITY_ANDEOK("서귀포시 안덕면", 1, STATE_JEJU),
        CITY_PYOSEON("서귀포시 표선면", 1, STATE_JEJU);


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
        throw new MoimingApiException(COMMON_MAPPABLE_ENUM_VALUE, "[" + name + "] 에 해당하는 객체를 찾을 수 없습니다");
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

    /*
     특정 AreaValue 내부 states 들에 대한 액션으로, static 하지 않게 하기 위해 private 함수로 구성
     */
    public AreaValue getCityFromStateCities(String areaCityName) {

        AreaValue areaCity = null;

        List<AreaValue> stateCities = this.getStateCities();
        for (AreaValue stateCity : stateCities) {
            if (stateCity.getName().equals(areaCityName)) {
                areaCity = stateCity;
                break;
            }
        }

        if (areaCity == null) {
            log.warn("{}, getCityFromStateCities :: {}", this.getClass().getName(), "[" + areaCityName + "] 에 해당하는 객체를 찾을 수 없습니다");
            throw new MoimingApiException(COMMON_MAPPABLE_ENUM_VALUE, "[" + areaCityName + "] 에 해당하는 객체를 찾을 수 없습니다");
        }

        return areaCity;
    }

}
