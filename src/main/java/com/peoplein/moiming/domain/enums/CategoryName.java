package com.peoplein.moiming.domain.enums;


import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.exception.MoimingApiException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryName {

    // 1차
    DANCE("댄스/무용"),
    OUTDOOR("아웃도어/엑티비티"),
    EXERCISE("운동/스포츠"),
    BOOK("인문학/책/글"),
    JOB("업종/직무"),
    LANGUAGE("외국/언어"),
    CULTURAL("문화/공연/축제"),
    MUSIC("음악"),
    CRAFTS("공예"),
    COOK("요리/제조"),
    PET("반려동물"),
    AMITY("친목/모임"),
    HOBBY("취미"),



    // 2차_DANCE
    LATIN_DANCE("라틴댄스"),
    SOCIAL_DANCE("사교댄스"),
    BROADCAST("방송/힙합"),
    STREET_DANCE("스트릿댄스"),
    BALLET("발레"),
    JAZZ_DANCE("재즈댄스"),
    KOREAN_DANCE("한국무용"),
    BELLY_DANCE("밸리댄스"),
    CONTEMPORARY_DANCE("현대무용"),
    SWING_DANCE("스윙댄스"),


    // 2차_OUTDOOR
    HIKING("등산"),
    WALKING("산책/트래킹"),
    CAMPING("캠핑/백패킹"),
    DOMESTIC("국내여행"),
    INTERNATIONAL("해외여행"),
    FISHING("낚시"),
    PARAGLIDING("패러글라이딩"),
    DRIVE("드라이브"),
    PICNIC("피크닉"),


    // 2차_EXERCISE
    CYCLING("자전거"),
    BADMINTON("배드민턴"),
    BOWLING("볼링"),
    TENNIS("테니스/스쿼시"),
    SKI("스키/보드"),
    GOLF("골프"),
    CLIMBING("클라이밍"),
    DIET("다이어트"),
    FITNESS("헬스/크로스핏"),
    YOGA("요가/필라테스"),
    TABLE_TENNIS("탁구"),
    BILLIARDS("당구/포켓볼"),
    RUNNING("러닝/마라톤"),
    SWIMMING("수영/스쿠버다이빙"),
    SEA("서핑/웨이크보드/요트"),
    FOOTBALL("축구/풋살"),
    BASKETBALL("농구"),
    BASEBALL("야구"),
    VOLLEYBALL("배구"),
    HORSEBACK_RIDING("승마"),
    FENCING("펜싱"),
    BOXING("복싱"),
    TAEKWONDO("태퀀도/유도"),
    KENDO("검도"),
    MARTIAL_ARTS("무술/주짓수"),
    SKATING("스케이트/인라인"),
    CRUISER("크루즈보드"),
    FOOT_VOLLEY("족구"),
    ARCHERY("양궁"),


    // 2차_BOOK
    READING("책/독서"),
    HUMANITIES("인문학"),
    PSYCHOLOGY("심리학"),
    PHILOSOPHY("철학"),
    HISTORY("역사"),
    ECONOMICS("시사/경제"),
    WRITING("작문/글쓰기"),


    // 2차_JOB
    INVESTMENT("투자/재테크"),
    BRANDING("브랜딩"),
    SIDE_PROJECT("사이드프로젝트"),
    BUSINESS("사업/창업"),
    CAREER("커리어"),
    STUDY("스터디"),
    FREELANCE("프리랜서"),
    N_JOB("N잡")


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

    public static CategoryName fromValue(String value) {
        for (CategoryName cName : CategoryName.values()) {
            if (cName.getValue().equals(value)) {
                return cName;
            }
        }
        throw new MoimingApiException("부적합한 카테고리 명입니다: " + value);
    }

}
