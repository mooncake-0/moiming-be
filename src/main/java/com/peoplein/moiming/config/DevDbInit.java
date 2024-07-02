package com.peoplein.moiming.config;

import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.AreaValue;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberInfo;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.domain.moim.MoimMonthlyCount;
import com.peoplein.moiming.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DevDbInit {


    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final MoimRepository moimRepository;
    private final RoleRepository roleRepository;
    private final MoimCountRepository moimCountRepository;

    private Category cOUTDOOR, cEXERCISE, cCOOK;
    private Category HIKING, BASKETBALL, TABLE_TENNIS, BAKING;

    @Order(2)
    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void initSampleMoim() {

        log.info("{}, initDevMoims :: {}", this.getClass().getName(), "개발용 모임 데이터 INPUT");

        // 1 - Role 생성
        Role roleUser = new Role(1L, "유저 권한", RoleType.USER);
        roleRepository.save(roleUser);

        // 2- Member 생성
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String encrypted = encoder.encode("1234");
        Member member = Member.createMember("01087538643", encrypted, "fcmToken", null, roleUser);
        memberRepository.save(member);

        // 2 - Member Info 생성
        MemberInfo memberInfo = MemberInfo.saveMemberInfo(member, "", MemberGender.M, LocalDate.of(1995, 12, 18));
        memberRepository.saveMemberInfo(memberInfo);

        // 2- Member2 생성
        Member member2 = Member.createMember("01012123434", encrypted, "fcmToken", null, roleUser);
        memberRepository.save(member2);

        // 2 - Member Info 생성
        MemberInfo memberInfo2 = MemberInfo.saveMemberInfo(member2, "", MemberGender.F, LocalDate.of(1997, 6, 1));
        memberRepository.saveMemberInfo(memberInfo2);

        // 2 - Moim 생성 .
        createMoimLogic("서울 산들을 등산으로 정복하자", "서울 지역을 중심으로 등산하는 모임입니다! 서울 산들 뿐만 아니라 다른 지역 산들도 등산하지만, 회식이나 뒷풀이는 보통 서울에서 진행합니다"
                , 30, new Area(AreaValue.STATE_SEOUL, AreaValue.CITY_GWANAK), List.of(cOUTDOOR, HIKING)
                , member, LocalDateTime.of(2022, 6, 3, 0, 0, 0), 40, 20, MemberGender.N, 33, 7);

        createMoimLogic("경기 농구인들 모여라", "수원시 농구 동호회 미르마르입니다. 잘하기보단 열심히 참여하시면서 즐겁게 동호회 활동할 회원들을 모집합니다."
                , 30, new Area(AreaValue.STATE_GYEONGGI, AreaValue.CITY_SUWON), List.of(cEXERCISE, BASKETBALL)
                , member, LocalDateTime.of(2023, 7, 24, 0, 0, 0), 50, 20, MemberGender.N, 17, 4);

        createMoimLogic("분당 테니스 동호회", "분당 테니스 동호회입니다. 연령이 다양하게 있고 완전 처음일 경우 기본적인 교육 같이 진행합니다. 회비 있고 참여 자유롭습니다."
                , 50, new Area(AreaValue.STATE_GYEONGGI, AreaValue.CITY_SEONGNAM), List.of(cEXERCISE, TABLE_TENNIS)
                , member, LocalDateTime.of(2023, 8, 3, 0, 0, 0), 60, 20, MemberGender.N, 22, 4);

        createMoimLogic("대전 모임 빵빠레", "대전에서 빵을 좋아하는 사람들의 모임입니다! 꼭 베이킹 관련된건 아니고 빵 맛집 투어를 하기도 합니다. 수요 인원을 조사해서 제과제빵 수업을 같이 듣기도 합니다. 회비는 일정 별로 따로 모으고 성별 나이 무관합니다."
                , 40, new Area(AreaValue.STATE_DAEJEON, AreaValue.CITY_DAEJEON_JUNG), List.of(cCOOK, BAKING)
                , member, LocalDateTime.of(2023, 11, 13, 0, 0, 0), 40, 20, MemberGender.N, 11, 3);

        createMoimLogic("독서 모임 독토리", "온라인 위주로 독서모임 진행하는 독토리 입니다. 회식 할 경우 서울에서 진행하긴 하는데 자주 하진 않습니다. 독서하고 느낀 점 공유하며 자유롭게 참여 가능합니다. 회비 없습니다"
                , 30, new Area(AreaValue.STATE_SEOUL, AreaValue.CITY_GANGNAM), List.of(cCOOK, BAKING)
                , member2, LocalDateTime.of(2024, 2, 9, 0, 0, 0), 60, 20, MemberGender.N, 46, 11);


        // 여기까지..

    }

    private void createMoimLogic(String name, String info, int maxMember, Area area, List<Category> categories, Member member
            , LocalDateTime createdAt, int ageMax, int ageMin, MemberGender gender, int lastCnt, int thisCnt) {

        Moim moim = Moim.createMoim(name, info, maxMember, area, categories, member);
        MoimJoinRule mjr = MoimJoinRule.createMoimJoinRule(ageMax, ageMin, gender);
        moim.initJoinRule(mjr);
        moimRepository.save(moim);
        moim.changeMoimCreatedTimeInNeed(createdAt); // PERSIST 이후 수행해야 PRE_Persist 안걸림

        MoimMonthlyCount lastMonth = MoimMonthlyCount.createMoimMonthlyCount(moim);
        lastMonth.changeDateTimeForSampleData(LocalDate.now().withDayOfMonth(1).minusMonths(1));
        lastMonth.changeCntForSampleData(lastCnt);
        MoimMonthlyCount thisMonth = MoimMonthlyCount.createMoimMonthlyCount(moim);
        thisMonth.changeCntForSampleData(thisCnt);
        moimCountRepository.save(lastMonth);
        moimCountRepository.save(thisMonth);

    }


    @Order(1)
    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void initCategory() {

        log.info("{}, initCategory :: {}", this.getClass().getName(), "Current 카테고리 데이터 INPUT");

        // Category 추가
        int i = 1;
        Category cDANCE = new Category((long) i++, CategoryName.DANCE, 0, null);
        cOUTDOOR = new Category((long) i++, CategoryName.OUTDOOR, 0, null);
        cEXERCISE = new Category((long) i++, CategoryName.EXERCISE, 0, null);
        Category cBOOK = new Category((long) i++, CategoryName.BOOK, 0, null);
        Category cJOB = new Category((long) i++, CategoryName.JOB, 0, null);
        Category cLANGUAGE = new Category((long) i++, CategoryName.LANGUAGE, 0, null);
        Category cCULTURAL = new Category((long) i++, CategoryName.CULTURAL, 0, null);
        Category cMUSIC = new Category((long) i++, CategoryName.MUSIC, 0, null);
        Category cCRAFTS = new Category((long) i++, CategoryName.CRAFTS, 0, null);
        cCOOK = new Category((long) i++, CategoryName.COOK, 0, null);
        Category cPET = new Category((long) i++, CategoryName.PET, 0, null);
        Category cAMITY = new Category((long) i++, CategoryName.AMITY, 0, null);
        Category cHOBBY = new Category((long) i++, CategoryName.HOBBY, 0, null);

        categoryRepository.save(cDANCE);
        categoryRepository.save(cOUTDOOR);
        categoryRepository.save(cEXERCISE);
        categoryRepository.save(cBOOK);
        categoryRepository.save(cJOB);
        categoryRepository.save(cLANGUAGE);
        categoryRepository.save(cCULTURAL);
        categoryRepository.save(cMUSIC);
        categoryRepository.save(cCRAFTS);
        categoryRepository.save(cCOOK);
        categoryRepository.save(cPET);
        categoryRepository.save(cAMITY);
        categoryRepository.save(cHOBBY);

        // DANCE 2차
        categoryRepository.save(new Category((long) i++, CategoryName.LATIN_DANCE, 1, cDANCE));
        categoryRepository.save(new Category((long) i++, CategoryName.SOCIAL_DANCE, 1, cDANCE));
        categoryRepository.save(new Category((long) i++, CategoryName.BROADCAST, 1, cDANCE));
        categoryRepository.save(new Category((long) i++, CategoryName.STREET_DANCE, 1, cDANCE));
        categoryRepository.save(new Category((long) i++, CategoryName.BALLET, 1, cDANCE));
        categoryRepository.save(new Category((long) i++, CategoryName.JAZZ_DANCE, 1, cDANCE));
        categoryRepository.save(new Category((long) i++, CategoryName.KOREAN_DANCE, 1, cDANCE));
        categoryRepository.save(new Category((long) i++, CategoryName.BELLY_DANCE, 1, cDANCE));
        categoryRepository.save(new Category((long) i++, CategoryName.CONTEMPORARY_DANCE, 1, cDANCE));
        categoryRepository.save(new Category((long) i++, CategoryName.SWING_DANCE, 1, cDANCE));

        // OUTDOOR 2차
        HIKING = new Category((long) i++, CategoryName.HIKING, 1, cOUTDOOR);
        categoryRepository.save(HIKING);
        categoryRepository.save(new Category((long) i++, CategoryName.WALKING, 1, cOUTDOOR));
        categoryRepository.save(new Category((long) i++, CategoryName.CAMPING, 1, cOUTDOOR));
        categoryRepository.save(new Category((long) i++, CategoryName.DOMESTIC, 1, cOUTDOOR));
        categoryRepository.save(new Category((long) i++, CategoryName.INTERNATIONAL, 1, cOUTDOOR));
        categoryRepository.save(new Category((long) i++, CategoryName.FISHING, 1, cOUTDOOR));
        categoryRepository.save(new Category((long) i++, CategoryName.PARAGLIDING, 1, cOUTDOOR));
        categoryRepository.save(new Category((long) i++, CategoryName.DRIVE, 1, cOUTDOOR));
        categoryRepository.save(new Category((long) i++, CategoryName.PICNIC, 1, cOUTDOOR));


        // EXERCISE 2차
        TABLE_TENNIS = new Category((long) i++, CategoryName.TABLE_TENNIS, 1, cEXERCISE);
        BASKETBALL = new Category((long) i++, CategoryName.BASKETBALL, 1, cEXERCISE);
        categoryRepository.save(new Category((long) i++, CategoryName.CYCLING, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.BADMINTON, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.BOWLING, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.TENNIS, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.SKI, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.GOLF, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.CLIMBING, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.DIET, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.FITNESS, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.YOGA, 1, cEXERCISE));
        categoryRepository.save(TABLE_TENNIS);
        categoryRepository.save(new Category((long) i++, CategoryName.BILLIARDS, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.RUNNING, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.SWIMMING, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.SEA, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.FOOTBALL, 1, cEXERCISE));
        categoryRepository.save(BASKETBALL);
        categoryRepository.save(new Category((long) i++, CategoryName.BASEBALL, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.VOLLEYBALL, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.HORSEBACK_RIDING, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.FENCING, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.BOXING, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.TAEKWONDO, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.KENDO, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.MARTIAL_ARTS, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.SKATING, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.CRUISER, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.FOOT_VOLLEY, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.ARCHERY, 1, cEXERCISE));

        // BOOK 2차
        categoryRepository.save(new Category((long) i++, CategoryName.READING, 1, cBOOK));
        categoryRepository.save(new Category((long) i++, CategoryName.HUMANITIES, 1, cBOOK));
        categoryRepository.save(new Category((long) i++, CategoryName.PSYCHOLOGY, 1, cBOOK));
        categoryRepository.save(new Category((long) i++, CategoryName.PHILOSOPHY, 1, cBOOK));
        categoryRepository.save(new Category((long) i++, CategoryName.HISTORY, 1, cBOOK));
        categoryRepository.save(new Category((long) i++, CategoryName.ECONOMICS, 1, cBOOK));
        categoryRepository.save(new Category((long) i++, CategoryName.WRITING, 1, cBOOK));

        // JOB 2차
        categoryRepository.save(new Category((long) i++, CategoryName.INVESTMENT, 1, cJOB));
        categoryRepository.save(new Category((long) i++, CategoryName.BRANDING, 1, cJOB));
        categoryRepository.save(new Category((long) i++, CategoryName.SIDE_PROJECT, 1, cJOB));
        categoryRepository.save(new Category((long) i++, CategoryName.BUSINESS, 1, cJOB));
        categoryRepository.save(new Category((long) i++, CategoryName.CAREER, 1, cJOB));
        categoryRepository.save(new Category((long) i++, CategoryName.STUDY, 1, cJOB));
        categoryRepository.save(new Category((long) i++, CategoryName.FREELANCE, 1, cJOB));
        categoryRepository.save(new Category((long) i++, CategoryName.N_JOB, 1, cJOB));

        // LANGUAGE 2차
        categoryRepository.save(new Category((long) i++, CategoryName.ENGLISH, 1, cLANGUAGE));
        categoryRepository.save(new Category((long) i++, CategoryName.JAPANESE, 1, cLANGUAGE));
        categoryRepository.save(new Category((long) i++, CategoryName.CHINESE, 1, cLANGUAGE));
        categoryRepository.save(new Category((long) i++, CategoryName.FRENCH, 1, cLANGUAGE));
        categoryRepository.save(new Category((long) i++, CategoryName.SPANISH, 1, cLANGUAGE));
        categoryRepository.save(new Category((long) i++, CategoryName.RUSSIAN, 1, cLANGUAGE));
        categoryRepository.save(new Category((long) i++, CategoryName.OTHER_LANGUAGE, 1, cLANGUAGE));

        // CULTURAL 2차
        categoryRepository.save(new Category((long) i++, CategoryName.MUSICAL, 1, cCULTURAL));
        categoryRepository.save(new Category((long) i++, CategoryName.SHOW, 1, cCULTURAL));
        categoryRepository.save(new Category((long) i++, CategoryName.MOVIE, 1, cCULTURAL));
        categoryRepository.save(new Category((long) i++, CategoryName.EXHIBITION, 1, cCULTURAL));
        categoryRepository.save(new Category((long) i++, CategoryName.SHOW_PRODUCE, 1, cCULTURAL));
        categoryRepository.save(new Category((long) i++, CategoryName.CULTURAL_HERITAGE, 1, cCULTURAL));
        categoryRepository.save(new Category((long) i++, CategoryName.FESTIVAL, 1, cCULTURAL));

        // MUSIC 2차
        categoryRepository.save(new Category((long) i++, CategoryName.VOCAL, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.GUITAR, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.UKULELE, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.DRUM, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.PIANO, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.VIOLIN, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.FLUTE, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.OCARINA, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.BAND, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.COMPOSE, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.INDE_MUSIC, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.HIPHOP, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.CLASSIC, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.JAZZ, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.ROCK, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.ELECTRONIC, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.KOREAN_MUSIC, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.CCM, 1, cMUSIC));
        categoryRepository.save(new Category((long) i++, CategoryName.NEW_AGE, 1, cMUSIC));

        // CRAFT 2차
        categoryRepository.save(new Category((long) i++, CategoryName.PAINTING, 1, cCRAFTS));
        categoryRepository.save(new Category((long) i++, CategoryName.CALLIGRAPHY, 1, cCRAFTS));
        categoryRepository.save(new Category((long) i++, CategoryName.FLOWER, 1, cCRAFTS));
        categoryRepository.save(new Category((long) i++, CategoryName.CANDLE, 1, cCRAFTS));
        categoryRepository.save(new Category((long) i++, CategoryName.COSMETICS, 1, cCRAFTS));
        categoryRepository.save(new Category((long) i++, CategoryName.PROP, 1, cCRAFTS));
        categoryRepository.save(new Category((long) i++, CategoryName.LEATHER, 1, cCRAFTS));
        categoryRepository.save(new Category((long) i++, CategoryName.FURNITURE, 1, cCRAFTS));
        categoryRepository.save(new Category((long) i++, CategoryName.MATERIAL, 1, cCRAFTS));
        categoryRepository.save(new Category((long) i++, CategoryName.CLAY, 1, cCRAFTS));
        categoryRepository.save(new Category((long) i++, CategoryName.KNITTING, 1, cCRAFTS));
        categoryRepository.save(new Category((long) i++, CategoryName.KIDULT, 1, cCRAFTS));
        categoryRepository.save(new Category((long) i++, CategoryName.MAKEUP, 1, cCRAFTS));

        // COOK 2차
        BAKING = new Category((long) i++, CategoryName.BAKING, 1, cCOOK);
        categoryRepository.save(new Category((long) i++, CategoryName.KOREAN_FOOD, 1, cCOOK));
        categoryRepository.save(new Category((long) i++, CategoryName.WESTERN_FOOD, 1, cCOOK));
        categoryRepository.save(new Category((long) i++, CategoryName.JAPANESE_FOOD, 1, cCOOK));
        categoryRepository.save(new Category((long) i++, CategoryName.CHINESE_FOOD, 1, cCOOK));
        categoryRepository.save(BAKING);
        categoryRepository.save(new Category((long) i++, CategoryName.HAND_DRIP, 1, cCOOK));

        // PET 2차
        categoryRepository.save(new Category((long) i++, CategoryName.DOG, 1, cPET));
        categoryRepository.save(new Category((long) i++, CategoryName.CAT, 1, cPET));
        categoryRepository.save(new Category((long) i++, CategoryName.FISH, 1, cPET));
        categoryRepository.save(new Category((long) i++, CategoryName.REPTILE, 1, cPET));
        categoryRepository.save(new Category((long) i++, CategoryName.BIRD, 1, cPET));
        categoryRepository.save(new Category((long) i++, CategoryName.RODENT, 1, cPET));

        // AMITY 2차
        categoryRepository.save(new Category((long) i++, CategoryName.MUST_GO_RESTAURANT, 1, cAMITY));
        categoryRepository.save(new Category((long) i++, CategoryName.CAFE, 1, cAMITY));
        categoryRepository.save(new Category((long) i++, CategoryName.DINING, 1, cAMITY));
        categoryRepository.save(new Category((long) i++, CategoryName.PEER, 1, cAMITY));
        categoryRepository.save(new Category((long) i++, CategoryName.NEIGHBORHOOD, 1, cAMITY));
        categoryRepository.save(new Category((long) i++, CategoryName.CONCERN, 1, cAMITY));
        categoryRepository.save(new Category((long) i++, CategoryName.PARENTING, 1, cAMITY));
        categoryRepository.save(new Category((long) i++, CategoryName.SMALL_TALK, 1, cAMITY));

        // HOBBY 2차
        categoryRepository.save(new Category((long) i++, CategoryName.CAR, 1, cHOBBY));
        categoryRepository.save(new Category((long) i++, CategoryName.PHOTOGRAPHY, 1, cHOBBY));
        categoryRepository.save(new Category((long) i++, CategoryName.GAME, 1, cHOBBY));
        categoryRepository.save(new Category((long) i++, CategoryName.VOLUNTEER, 1, cHOBBY));
        categoryRepository.save(new Category((long) i++, CategoryName.FLOGGING, 1, cHOBBY));

    }
}
