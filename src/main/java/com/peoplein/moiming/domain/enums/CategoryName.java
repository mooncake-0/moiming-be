package com.peoplein.moiming.domain.enums;


import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.peoplein.moiming.exception.ExceptionValue.*;

@Slf4j
@Getter
@AllArgsConstructor
public enum CategoryName {

    // 1차
    DANCE("댄스/무용", 0),
    OUTDOOR("아웃도어/액티비티", 0),
    EXERCISE("운동/스포츠", 0),
    BOOK("인문학/책/글", 0),
    JOB("업종/직무", 0),
    LANGUAGE("외국/언어", 0),
    CULTURAL("문화/공연/축제", 0),
    MUSIC("음악", 0),
    CRAFTS("공예", 0),
    COOK("요리/제조", 0),
    PET("반려동물", 0),
    AMITY("친목/모임", 0),
    HOBBY("취미", 0),

    // ===

    // 2차_DANCE
    LATIN_DANCE("라틴댄스", 1),
    SOCIAL_DANCE("사교댄스", 1),
    BROADCAST("방송/힙합", 1),
    STREET_DANCE("스트릿댄스", 1),
    BALLET("발레", 1),
    JAZZ_DANCE("재즈댄스", 1),
    KOREAN_DANCE("한국무용", 1),
    BELLY_DANCE("밸리댄스", 1),
    CONTEMPORARY_DANCE("현대무용", 1),
    SWING_DANCE("스윙댄스", 1),

    // 2차_OUTDOOR
    HIKING("등산", 1),
    WALKING("산책/트래킹", 1),
    CAMPING("캠핑/백패킹", 1),
    DOMESTIC("국내여행", 1),
    INTERNATIONAL("해외여행", 1),
    FISHING("낚시", 1),
    PARAGLIDING("패러글라이딩", 1),
    DRIVE("드라이브", 1),
    PICNIC("피크닉", 1),

    // 2차_EXERCISE
    CYCLING("자전거", 1),
    BADMINTON("배드민턴", 1),
    BOWLING("볼링", 1),
    TENNIS("테니스/스쿼시", 1),
    SKI("스키/보드", 1),
    GOLF("골프", 1),
    CLIMBING("클라이밍", 1),
    DIET("다이어트", 1),
    FITNESS("헬스/크로스핏", 1),
    YOGA("요가/필라테스", 1),
    TABLE_TENNIS("탁구", 1),
    BILLIARDS("당구/포켓볼", 1),
    RUNNING("러닝/마라톤", 1),
    SWIMMING("수영/스쿠버다이빙", 1),
    SEA("서핑/웨이크보드/요트", 1),
    FOOTBALL("축구/풋살", 1),
    BASKETBALL("농구", 1),
    BASEBALL("야구", 1),
    VOLLEYBALL("배구", 1),
    HORSEBACK_RIDING("승마", 1),
    FENCING("펜싱", 1),
    BOXING("복싱", 1),
    TAEKWONDO("태퀀도/유도", 1),
    KENDO("검도", 1),
    MARTIAL_ARTS("무술/주짓수", 1),
    SKATING("스케이트/인라인", 1),
    CRUISER("크루즈보드", 1),
    FOOT_VOLLEY("족구", 1),
    ARCHERY("양궁", 1),

    // 2차_BOOK
    READING("책/독서", 1),
    HUMANITIES("인문학", 1),
    PSYCHOLOGY("심리학", 1),
    PHILOSOPHY("철학", 1),
    HISTORY("역사", 1),
    ECONOMICS("시사/경제", 1),
    WRITING("작문/글쓰기", 1),

    // 2차_JOB
    INVESTMENT("투자/재테크", 1),
    BRANDING("브랜딩", 1),
    SIDE_PROJECT("사이드프로젝트", 1),
    BUSINESS("사업/창업", 1),
    CAREER("커리어", 1),
    STUDY("스터디", 1),
    FREELANCE("프리랜서", 1),
    N_JOB("N잡", 1),

    // 2차_LANGUAGE
    ENGLISH("영어", 1),
    JAPANESE("일본어", 1),
    CHINESE("중국어", 1),
    FRENCH("프랑스어", 1),
    SPANISH("스페인어", 1),
    RUSSIAN("러시아어", 1),
    OTHER_LANGUAGE("기타언어", 1),

    // 2차_CULTURAL
    MUSICAL("뮤지컬/오페라", 1),
    SHOW("공연/연극", 1),
    MOVIE("영화", 1),
    EXHIBITION("전시회", 1),
    SHOW_PRODUCE("연기/공연 제작", 1),
    CULTURAL_HERITAGE("고궁/문화재 탐방", 1),
    FESTIVAL("파티/페스티벌", 1),

    // 2차_MUSIC
    VOCAL("노래/보컬", 1),
    GUITAR("기타/베이스", 1),
    UKULELE("우쿨렐레", 1),
    DRUM("드럼", 1),
    PIANO("피아노", 1),
    VIOLIN("바이올린", 1),
    FLUTE("플룻", 1),
    OCARINA("오카리나", 1),
    BAND("밴드/합주", 1),
    COMPOSE("작사/작곡", 1),
    INDE_MUSIC("인디음악", 1),
    HIPHOP("랩/힙합/DJ", 1),
    CLASSIC("클래식", 1),
    JAZZ("재즈", 1),
    ROCK("락/메탈", 1),
    ELECTRONIC("일렉트로닉", 1),
    KOREAN_MUSIC("국악/사물놀이", 1),
    CCM("찬양/CCM", 1),
    NEW_AGE("뉴에이지", 1),

    // 2차_CRAFTS
    PAINTING("미술/그림", 1),
    CALLIGRAPHY("캘리그라피", 1),
    FLOWER("플라워아트", 1),
    CANDLE("캔들/디퓨저/석고", 1),
    COSMETICS("천연비누/화장품", 1),
    PROP("소품공예", 1),
    LEATHER("가죽공예", 1),
    FURNITURE("가구/목공예", 1),
    MATERIAL("설탕/암금공예", 1),
    CLAY("도자/점토공예", 1),
    KNITTING("자수/뜨개질", 1),
    KIDULT("키덜트/프라모델", 1),
    MAKEUP("메이크업/네일", 1),

    // 2차_COOK
    KOREAN_FOOD("한식", 1),
    WESTERN_FOOD("양식", 1),
    JAPANESE_FOOD("일식", 1),
    CHINESE_FOOD("중식", 1),
    BAKING("제과/제빵", 1),
    HAND_DRIP("핸드드립", 1),
    WINE("소믈리에/와인", 1),
    LIQUOR_MAKE("주류제조/칵테일", 1),

    // 2차_PET
    DOG("강아지", 1),
    CAT("고양이", 1),
    FISH("물고기", 1),
    REPTILE("파충류", 1),
    BIRD("조류", 1),
    RODENT("설치류/중치류", 1),

    // 2차_AMITY
    MUST_GO_RESTAURANT("맛집", 1),
    CAFE("카페", 1),
    LIQUOR("술(19세)", 1),
    DINING("다이닝", 1),
    PEER("또래", 1),
    NEIGHBORHOOD("동네", 1),
    CONCERN("관심사", 1),
    PARENTING("육아", 1),
    SMALL_TALK("스몰토크", 1),

    // 2차_HOBBY
    CAR("차/오토바이", 1),
    PHOTOGRAPHY("사진/영상", 1),
    GAME("게임/오락", 1),
    VOLUNTEER("봉사활동", 1),
    FLOGGING("플로깅", 1);

    private final String value;
    private final int depth;

    public static CategoryName fromValue(String value) {
        for (CategoryName cName : CategoryName.values()) {
            if (cName.getValue().equals(value)) {
                return cName;
            }
        }
        log.error("{}, fromValue :: {}", "CategoryName", "[" + value + "] 에 해당하는 객체를 찾을 수 없습니다");
        throw new MoimingApiException(COMMON_MAPPABLE_ENUM_VALUE);
    }


    public static CategoryName fromQueryParam(String value) {
        for (CategoryName cName : CategoryName.values()) {
            if (cName.getValue().equals(value)) {
                return cName;
            }
        }
        log.error("{}, fromQueryParam :: {}", "CategoryName",  "존재하지 않는 모임 종류로 필터링 시도, [" + value + "]");
        throw new MoimingApiException(COMMON_INVALID_REQUEST_PARAM);
    }


    // 검색시 사용, 텍스트 일치는 1차 카테고리는 제외
    public static List<CategoryName> consistsInCategoryName(String keyword) {
        List<CategoryName> consistingCategoryName = new ArrayList<>();
        for (CategoryName cName : CategoryName.values()) {
            if (cName.getValue().contains(keyword)) {
                if (cName.depth == 1) {
                    consistingCategoryName.add(cName);
                }
            }
        }
        return consistingCategoryName;
    }

}
