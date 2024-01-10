package com.peoplein.moiming.domain.enums;


import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.peoplein.moiming.exception.ExceptionValue.*;

@Slf4j
@Getter
@AllArgsConstructor
public enum CategoryName {

    // 1차
    DANCE("댄스/무용", 1),
    OUTDOOR("아웃도어/액티비티", 1),
    EXERCISE("운동/스포츠", 1),
    BOOK("인문학/책/글", 1),
    JOB("업종/직무", 1),
    LANGUAGE("외국/언어", 1),
    CULTURAL("문화/공연/축제", 1),
    MUSIC("음악", 1),
    CRAFTS("공예", 1),
    COOK("요리/제조", 1),
    PET("반려동물", 1),
    AMITY("친목/모임", 1),
    HOBBY("취미", 1),

    // ===

    // 2차_DANCE
    LATIN_DANCE("라틴댄스", 2),
    SOCIAL_DANCE("사교댄스", 2),
    BROADCAST("방송/힙합", 2),
    STREET_DANCE("스트릿댄스", 2),
    BALLET("발레", 2),
    JAZZ_DANCE("재즈댄스", 2),
    KOREAN_DANCE("한국무용", 2),
    BELLY_DANCE("밸리댄스", 2),
    CONTEMPORARY_DANCE("현대무용", 2),
    SWING_DANCE("스윙댄스", 2),

    // 2차_OUTDOOR
    HIKING("등산", 2),
    WALKING("산책/트래킹", 2),
    CAMPING("캠핑/백패킹", 2),
    DOMESTIC("국내여행", 2),
    INTERNATIONAL("해외여행", 2),
    FISHING("낚시", 2),
    PARAGLIDING("패러글라이딩", 2),
    DRIVE("드라이브", 2),
    PICNIC("피크닉", 2),

    // 2차_EXERCISE
    CYCLING("자전거", 2),
    BADMINTON("배드민턴", 2),
    BOWLING("볼링", 2),
    TENNIS("테니스/스쿼시", 2),
    SKI("스키/보드", 2),
    GOLF("골프", 2),
    CLIMBING("클라이밍", 2),
    DIET("다이어트", 2),
    FITNESS("헬스/크로스핏", 2),
    YOGA("요가/필라테스", 2),
    TABLE_TENNIS("탁구", 2),
    BILLIARDS("당구/포켓볼", 2),
    RUNNING("러닝/마라톤", 2),
    SWIMMING("수영/스쿠버다이빙", 2),
    SEA("서핑/웨이크보드/요트", 2),
    FOOTBALL("축구/풋살", 2),
    BASKETBALL("농구", 2),
    BASEBALL("야구", 2),
    VOLLEYBALL("배구", 2),
    HORSEBACK_RIDING("승마", 2),
    FENCING("펜싱", 2),
    BOXING("복싱", 2),
    TAEKWONDO("태퀀도/유도", 2),
    KENDO("검도", 2),
    MARTIAL_ARTS("무술/주짓수", 2),
    SKATING("스케이트/인라인", 2),
    CRUISER("크루즈보드", 2),
    FOOT_VOLLEY("족구", 2),
    ARCHERY("양궁", 2),

    // 2차_BOOK
    READING("책/독서", 2),
    HUMANITIES("인문학", 2),
    PSYCHOLOGY("심리학", 2),
    PHILOSOPHY("철학", 2),
    HISTORY("역사", 2),
    ECONOMICS("시사/경제", 2),
    WRITING("작문/글쓰기", 2),

    // 2차_J, 2OB
    INVESTMENT("투자/재테크", 2),
    BRANDING("브랜딩", 2),
    SIDE_PROJECT("사이드프로젝트", 2),
    BUSINESS("사업/창업", 2),
    CAREER("커리어", 2),
    STUDY("스터디", 2),
    FREELANCE("프리랜서", 2),
    N_JOB("N잡", 2),

    // 2차_LANGUAGE
    ENGLISH("영어", 2),
    JAPANESE("일본어", 2),
    CHINESE("중국어", 2),
    FRENCH("프랑스어", 2),
    SPANISH("스페인어", 2),
    RUSSIAN("러시아어", 2),
    OTHER_LANGUAGE("기타언어", 2),

    // 2차_CULTURAL
    MUSICAL("뮤지컬/오페라", 2),
    SHOW("공연/연극", 2),
    MOVIE("영화", 2),
    EXHIBITION("전시회", 2),
    SHOW_PRODUCE("연기/공연 제작", 2),
    CULTURAL_HERITAGE("고궁/문화재 탐방", 2),
    FESTIVAL("파티/페스티벌", 2),

    // 2차_MUSIC
    VOCAL("노래/보컬", 2),
    GUITAR("기타/베이스", 2),
    UKULELE("우쿨렐레", 2),
    DRUM("드럼", 2),
    PIANO("피아노", 2),
    VIOLIN("바이올린", 2),
    FLUTE("플룻", 2),
    OCARINA("오카리나", 2),
    BAND("밴드/합주", 2),
    COMPOSE("작사/작곡", 2),
    INDE_MUSIC("인디음악", 2),
    HIPHOP("랩/힙합/DJ", 2),
    CLASSIC("클래식", 2),
    JAZZ("재즈", 2),
    ROCK("락/메탈", 2),
    ELECTRONIC("일렉트로닉", 2),
    KOREAN_MUSIC("국악/사물놀이", 2),
    CCM("찬양/CCM", 2),
    NEW_AGE("뉴에이지", 2),

    // 2차_CRAFTS
    PAINTING("미술/그림", 2),
    CALLIGRAPHY("캘리그라피", 2),
    FLOWER("플라워아트", 2),
    CANDLE("캔들/디퓨저/석고", 2),
    COSMETICS("천연비누/화장품", 2),
    PROP("소품공예", 2),
    LEATHER("가죽공예", 2),
    FURNITURE("가구/목공예", 2),
    MATERIAL("설탕/암금공예", 2),
    CLAY("도자/점토공예", 2),
    KNITTING("자수/뜨개질", 2),
    KIDULT("키덜트/프라모델", 2),
    MAKEUP("메이크업/네일", 2),

    // 2차_COOK
    KOREAN_FOOD("한식", 2),
    WESTERN_FOOD("양식", 2),
    JAPANESE_FOOD("일식", 2),
    CHINESE_FOOD("중식", 2),
    BAKING("제과/제빵", 2),
    HAND_DRIP("핸드드립", 2),
    WINE("소믈리에/와인", 2),
    LIQUOR_MAKE("주류제조/칵테일", 2),

    // 2차_PET
    DOG("강아지", 2),
    CAT("고양이", 2),
    FISH("물고기", 2),
    REPTILE("파충류", 2),
    BIRD("조류", 2),
    RODENT("설치류/중치류", 2),

    // 2차_AMITY
    MUST_GO_RESTAURANT("맛집", 2),
    CAFE("카페", 2),
    LIQUOR("술(19세)", 2),
    DINING("다이닝", 2),
    PEER("또래", 2),
    NEIGHBORHOOD("동네", 2),
    CONCERN("관심사", 2),
    PARENTING("육아", 2),
    SMALL_TALK("스몰토크", 2),

    // 2차_HOBBY
    CAR("차/오토바이", 2),
    PHOTOGRAPHY("사진/영상", 2),
    GAME("게임/오락", 2),
    VOLUNTEER("봉사활동", 2),
    FLOGGING("플로깅", 2);;

    private final String value;
    private final int depth;

    public static CategoryName fromValue(String value) {
        for (CategoryName cName : CategoryName.values()) {
            if (cName.getValue().equals(value)) {
                return cName;
            }
        }
        log.error("{}, {}", "존재하지 않는 카테고리 전환 시도, [" + value + "], C999", COMMON_INVALID_SITUATION.getErrMsg());
        throw new MoimingApiException(COMMON_INVALID_SITUATION);
    }

}
