package com.peoplein.moiming.domain.enums;


import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
    N_JOB("N잡", 2)


    // 2차_LANGUAGE
    // 2차_CULTURAL
    // 2차_MUSIC
    // 2차_CRAFTS
    // 2차_COOK
    // 2차_PET
    // 2차_AMITY
    // 2차_HOBBY
    ;

    private final String value;
    private final int depth;

    public static CategoryName fromValue(String value) {
        for (CategoryName cName : CategoryName.values()) {
            if (cName.getValue().equals(value)) {
                return cName;
            }
        }
        throw new MoimingApiException("부적합한 카테고리 명입니다: " + value);
    }

}
