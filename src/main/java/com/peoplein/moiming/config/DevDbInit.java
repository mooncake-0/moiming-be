package com.peoplein.moiming.config;

import com.peoplein.moiming.domain.admin.ServiceStatus;
import com.peoplein.moiming.domain.admin.ServiceVersion;
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
import com.peoplein.moiming.service.util.MemberNicknameCreator;
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

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Profile(value = "local") // 개발용 서버에는 직접 넣는 것으로 변경
@Configuration
@RequiredArgsConstructor
public class DevDbInit {

    private final EntityManager em;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final MoimRepository moimRepository;
    private final RoleRepository roleRepository;
    private final MoimCountRepository moimCountRepository;

    private Category cOUTDOOR, cEXERCISE, cCOOK, cPET, cAMITY, cHOBBY, cJOB, cLANGUAGE, cCULTURAL;
    private Category HIKING, BASKETBALL, TABLE_TENNIS, BAKING, DOG, SMALL_TALK, MUST_GO_RESTAURANT, VOLUNTEER, FITNESS, CONCERN, STUDY, JAPANESE, MUSICAL, CYCLING, SIDE_PROJECT, MOVIE, INVESTMENT, PARENTING, BUSINESS;

    private int ageToYearConverter(int age) {
        return LocalDate.now().getYear() - age;
    }


    @Order(3)
    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void initServiceStatus() {

        log.info("{}, initServiceStatus :: {}", this.getClass().getName(), "현재 서비스 데이터 입력");

        ServiceVersion svAos = new ServiceVersion("aos", "0.0.0", LocalDate.of(2024, 8, 1));
        ServiceVersion svIos = new ServiceVersion("ios", "0.0.0", LocalDate.of(2024, 8, 1));
        ServiceVersion svServer = new ServiceVersion("server", "0.0.0", LocalDate.of(2024, 8, 1));

        em.persist(svAos);
        em.persist(svIos);
        em.persist(svServer);

        ServiceStatus serverStatus = new ServiceStatus(false, null, null, "첫 배포 진행 완료", 1L);
        em.persist(serverStatus);

    }


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
        MemberNicknameCreator nicknameCreator = new MemberNicknameCreator();
        String encrypted = encoder.encode("1234");
        Member member = Member.createMember("01087538643", encrypted, "fcmToken", null, roleUser);
        member.changeNickname("됴오타됴하");
        memberRepository.save(member);

        // 2 - Member Info 생성
        MemberInfo memberInfo = MemberInfo.saveMemberInfo(member, "", MemberGender.M, LocalDate.of(1995, 12, 18));
        memberRepository.saveMemberInfo(memberInfo);

        // 2- Member2 생성
        Member member2 = Member.createMember("01012123434", encrypted, "fcmToken", null, roleUser);
        member2.changeNickname("후리얍");
        memberRepository.save(member2);

        // 2 - Member Info 생성
        MemberInfo memberInfo2 = MemberInfo.saveMemberInfo(member2, "", MemberGender.F, LocalDate.of(1997, 6, 1));
        memberRepository.saveMemberInfo(memberInfo2);

        // 2 - Moim 생성 .
        createMoimLogic("서울 산들을 등산으로 정복하자", "서울 지역을 중심으로 등산하는 모임입니다! 서울 산들 뿐만 아니라 다른 지역 산들도 등산하지만, 회식이나 뒷풀이는 보통 서울에서 진행합니다"
                , 30, new Area(AreaValue.STATE_SEOUL, AreaValue.CITY_GWANAK), List.of(cOUTDOOR, HIKING)
                , member, LocalDateTime.of(2022, 6, 3, 0, 0, 0), ageToYearConverter(40), ageToYearConverter(20), MemberGender.N, 33, 7);

        createMoimLogic("경기 농구인들 모여라", "수원시 농구 동호회 미르마르입니다. 잘하기보단 열심히 참여하시면서 즐겁게 동호회 활동할 회원들을 모집합니다."
                , 30, new Area(AreaValue.STATE_GYEONGGI, AreaValue.CITY_SUWON), List.of(cEXERCISE, BASKETBALL)
                , member, LocalDateTime.of(2023, 7, 24, 0, 0, 0), ageToYearConverter(50), ageToYearConverter(20), MemberGender.N, 17, 4);

        createMoimLogic("분당 테니스 동호회", "분당 테니스 동호회입니다. 연령이 다양하게 있고 완전 처음일 경우 기본적인 교육 같이 진행합니다. 회비 있고 참여 자유롭습니다."
                , 50, new Area(AreaValue.STATE_GYEONGGI, AreaValue.CITY_SEONGNAM), List.of(cEXERCISE, TABLE_TENNIS)
                , member, LocalDateTime.of(2023, 8, 3, 0, 0, 0), ageToYearConverter(60), ageToYearConverter(20), MemberGender.N, 22, 4);

        createMoimLogic("대전 모임 빵빠레", "대전에서 빵을 좋아하는 사람들의 모임입니다! 꼭 베이킹 관련된건 아니고 빵 맛집 투어를 하기도 합니다. 수요 인원을 조사해서 제과제빵 수업을 같이 듣기도 합니다. 회비는 일정 별로 따로 모으고 성별 나이 무관합니다."
                , 40, new Area(AreaValue.STATE_DAEJEON, AreaValue.CITY_DAEJEON_JUNG), List.of(cCOOK, BAKING)
                , member, LocalDateTime.of(2023, 11, 13, 0, 0, 0), ageToYearConverter(40), ageToYearConverter(20), MemberGender.N, 11, 3);

        createMoimLogic("독서 모임 독토리", "온라인 위주로 독서모임 진행하는 독토리 입니다. 회식 할 경우 서울에서 진행하긴 하는데 자주 하진 않습니다. 독서하고 느낀 점 공유하며 자유롭게 참여 가능합니다. 회비 없습니다"
                , 30, new Area(AreaValue.STATE_SEOUL, AreaValue.CITY_GANGNAM), List.of(cCOOK, BAKING)
                , member2, LocalDateTime.of(2024, 2, 9, 0, 0, 0), ageToYearConverter(60), ageToYearConverter(20), MemberGender.N, 46, 11);


        // 여기서 부터 6번
        createMoimLogic("부산 강사모", "강아지를 사랑하는 사람들의 모임입니다. 부산 지역에서 활동합니다. 자유롭게 들어와서 질문해주세요!"
                , 40, new Area(AreaValue.STATE_BUSAN, AreaValue.CITY_BUSAN_JUNG), List.of(cPET, DOG)
                , member, LocalDateTime.of(2024, 3, 2, 0, 0, 0), ageToYearConverter(60), ageToYearConverter(20), MemberGender.N, 56, 17);

        createMoimLogic("힙한 여성들의 수다", "여성들끼리 자유롭게 게시글 올리고 댓글들 달며 수다떠는 모임입니다. 아주 가끔씩 모임 주도하고 싶은 사람들끼리 알아서 모임 주도 하셔도 됩니다. 지역은 크게 상관 없는데 일단 서울로 해놨습니다"
                , 100, new Area(AreaValue.STATE_SEOUL, AreaValue.CITY_GANGBUK), List.of(cAMITY, SMALL_TALK)
                , member2, LocalDateTime.of(2024, 3, 16, 0, 0, 0), ageToYearConverter(50), ageToYearConverter(20), MemberGender.F, 77, 6);

        createMoimLogic("숨겨진 맛집을 찾아서 삼만리", "지역별 맛집을 찾아서 돌아다니는 동호회입니다. 전국 어디든 서로 맛집 추천을 받아서 놀러다니면서 친목 활동을 주로 진행합니다! 공식적인 일정에서 술은 마시지 않습니다. 전국 어디든 원하는 사람들끼리 돌아다니니 지역은 딱히 정해져 있진 않지만, 지역은 필수 지정이라 경기도로 했습니다"
                , 60, new Area(AreaValue.STATE_GYEONGGI, AreaValue.CITY_GWACHEON), List.of(cAMITY, MUST_GO_RESTAURANT)
                , member, LocalDateTime.of(2024, 1, 26, 0, 0, 0), ageToYearConverter(40), ageToYearConverter(20), MemberGender.N, 79, 11);

        createMoimLogic("유기견/유기묘를 함께 도와요", "유기견 유기묘 등 동물들을 위한 봉사활동을 해주는 동호회 입니다. 인천에서 보통 활동하며 필요에 따라 서울이나 경기지역까지 이동하기도 합니다"
                , 100, new Area(AreaValue.STATE_INCHEON, AreaValue.CITY_INCHEON_SEO), List.of(cHOBBY, VOLUNTEER)
                , member2, LocalDateTime.of(2024, 2, 25, 0, 0, 0), ageToYearConverter(40), ageToYearConverter(20), MemberGender.N, 23, 2);

        createMoimLogic("용인 헬창 형님들 같이 운동해요!", "용인 인근 지역에서 같이 헬스하는 사람들의 모임입니다. 자유롭게 가입해서 글 남겨주세요! 시간 맞는 사람들끼리 같이 운동합시다. 운동만 합니다."
                , 20, new Area(AreaValue.STATE_GYEONGGI, AreaValue.CITY_YONGIN), List.of(cEXERCISE, FITNESS)
                , member, LocalDateTime.of(2024, 3, 21, 0, 0, 0), ageToYearConverter(50), ageToYearConverter(20), MemberGender.M, 36, 4);


        // 여기서 부터 11번
        createMoimLogic("중기학 (중앙대 기계공학부 학생회)", "중앙대학교 기계공학부 학생회 모임입니다. 자유롭게 가입해서 글 올리고 족보 공유 모두 가능합니다. 적절한 카테고리가 없어서 친목으로 해놨습니다"
                , 100, new Area(AreaValue.STATE_SEOUL, AreaValue.CITY_GWANAK), List.of(cAMITY, CONCERN)
                , member, LocalDateTime.of(2023, 7, 8, 0, 0, 0), ageToYearConverter(30), ageToYearConverter(20), MemberGender.N, 115, 21);

        createMoimLogic("동탄 모각코 모임", "동탄에서 모각코 (모여서 각자 코딩) 모임입니다. 모각코긴 하지만 모여서 수다 떨기도 하고 코딩 스터디를 진행하기도 합니다. 정말 코딩만 하고 싶으시면 모집할 때 같이 올려두시면 됩니다!"
                , 20, new Area(AreaValue.STATE_GYEONGGI, AreaValue.CITY_HWASEONG), List.of(cJOB, STUDY)
                , member, LocalDateTime.of(2024, 4, 27, 0, 0, 0), ageToYearConverter(45), ageToYearConverter(25), MemberGender.N, 16, 2);

        createMoimLogic("강원대 일본어학과", "강원대 일본어학과 모임입니다. 같이 생존해나가요!"
                , 100, new Area(AreaValue.STATE_GANGWON, AreaValue.CITY_CHUNCHEON), List.of(cLANGUAGE, JAPANESE)
                , member2, LocalDateTime.of(2023, 9, 17, 0, 0, 0), ageToYearConverter(32), ageToYearConverter(20), MemberGender.N, 18, 4);

        createMoimLogic("뮤덕들 집합소", "서울에서 뮤지컬 보는 뮤덕들의 모임입니다. 같이 티케팅도 해주고 뮤지컬 관람도 자주 합니다 (되팔이 행위시 무조건 강퇴). 뮤덕들, 뮤린이들 모두 대환영입니다 (지역은 무관합니다)"
                , 40, new Area(AreaValue.STATE_SEOUL, AreaValue.CITY_GANGNAM), List.of(cCULTURAL, MUSICAL)
                , member, LocalDateTime.of(2023, 12, 10, 0, 0, 0), -1, -1, MemberGender.N, 166, 24);

        createMoimLogic("서사동 - 서울 사이클링 동호회", "서울 중심으로 활동하는 사이클링 동호회입니다. 대회 준비를 자주 하며 주기적으로 훈련도 진행합니다. 꽤 진지하게 사이클링하는 동호회입니다. 초보분들도 들어오시면 같이 단계별로 훈련합니다."
                , 50, new Area(AreaValue.STATE_SEOUL, AreaValue.CITY_NOWON), List.of(cEXERCISE, CYCLING)
                , member, LocalDateTime.of(2023, 6, 11, 0, 0, 0), ageToYearConverter(50), ageToYearConverter(20), MemberGender.N, 39, 8);


        // 여기서 부터 16번
        createMoimLogic("사이드 프로젝트 매칭해 공간", "코딩, 디자인, 기획 혹은 다른 사이드 프로젝트를 찾는 사람들의 모임입니다. 구인글 올리거나 구직글 올리시면 됩니다. 홍보 목적으로 들어오시는 사람은 적발시 바로 강퇴입니다. 매칭되면 나가셔도 됩니다. 지역은 필수라 설정되어있긴 하지만 알아서 자유롭게 설정하면 됩니다."
                , 100, new Area(AreaValue.STATE_GYEONGGI, AreaValue.CITY_NAMYANGJU), List.of(cJOB, SIDE_PROJECT)
                , member2, LocalDateTime.of(2024, 1, 18, 0, 0, 0), -1, -1, MemberGender.N, 119, 28);

        createMoimLogic("영화 평론 공간 (모임X)", "보신 영화 자유롭게 평론하는 모임입니다. 모이실 분들은 모이셔도 되지만 공식적으로 해당 모임은 오프라인 모임을 진행하진 않습니다. 지역은 상관 없기 때문에 서울로 합니다. 자유롭게 보신 영화, 감상문, 평점 등에 대해서 올려주시면 돼요~ 수상한 회원들은 바로 신고 / 강퇴 조치 합니다"
                , 50, new Area(AreaValue.STATE_SEOUL, AreaValue.CITY_DONGDAEMUN), List.of(cCULTURAL, MOVIE)
                , member, LocalDateTime.of(2024, 2, 25, 0, 0, 0), -1, -1, MemberGender.N, 42, 9);

        createMoimLogic("대구 투자 및 재테크 스터디", "주식, 코인, 부동산 등 자유롭게 정보들을 교류하는 모임입니다. 정보만 얻어가려는 사람들 다 보이니까 자제해주세요"
                , 50, new Area(AreaValue.STATE_DAEGU, AreaValue.CITY_DAEGU_DONG), List.of(cJOB, INVESTMENT)
                , member2, LocalDateTime.of(2024, 6, 2, 0, 0, 0), ageToYearConverter(60), ageToYearConverter(20), MemberGender.N, 25, 4);

        createMoimLogic("광주 육아맘들 소통해요~", "광주에서 육아하는 사람들끼리 소통하는 모임입니다. 자유롭게 가입해서 글 남겨주세요~ 종종 소통글 많이 올리겠습니다"
                , 30, new Area(AreaValue.STATE_GWANGJU, AreaValue.CITY_GWANGJU_BUK), List.of(cAMITY, PARENTING)
                , member2, LocalDateTime.of(2024, 4, 23, 0, 0, 0), ageToYearConverter(50), ageToYearConverter(25), MemberGender.F, 16, 3);

        createMoimLogic("모임의 시작과 끝까지, 모이밍", "모이밍을 만드는 이상한 사람들의 모임입니다. 될 때까지 하는 모이밍 화이팅!"
                , 10, new Area(AreaValue.STATE_SEOUL, AreaValue.CITY_GANGNAM), List.of(cJOB, BUSINESS)
                , member, LocalDateTime.of(2024, 5, 1, 0, 0, 0), ageToYearConverter(40), ageToYearConverter(25), MemberGender.N, 50, 8);

        // 여기까지..

    }

    private void createMoimLogic(String name, String info, int maxMember, Area area, List<Category> categories, Member member
            , LocalDateTime createdAt, int ageMax, int ageMin, MemberGender gender, int lastCnt, int thisCnt) {

        Moim moim = Moim.createMoim(name, info, maxMember, area, categories, member);
        MoimJoinRule mjr = MoimJoinRule.createMoimJoinRule(ageMax, ageMin, gender, member);
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
        cJOB = new Category((long) i++, CategoryName.JOB, 0, null);
        cLANGUAGE = new Category((long) i++, CategoryName.LANGUAGE, 0, null);
        cCULTURAL = new Category((long) i++, CategoryName.CULTURAL, 0, null);
        Category cMUSIC = new Category((long) i++, CategoryName.MUSIC, 0, null);
        Category cCRAFTS = new Category((long) i++, CategoryName.CRAFTS, 0, null);
        cCOOK = new Category((long) i++, CategoryName.COOK, 0, null);
        cPET = new Category((long) i++, CategoryName.PET, 0, null);
        cAMITY = new Category((long) i++, CategoryName.AMITY, 0, null);
        cHOBBY = new Category((long) i++, CategoryName.HOBBY, 0, null);

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
        FITNESS = new Category((long) i++, CategoryName.FITNESS, 1, cEXERCISE);
        CYCLING = new Category((long) i++, CategoryName.CYCLING, 1, cEXERCISE);
        categoryRepository.save(CYCLING);
        categoryRepository.save(new Category((long) i++, CategoryName.BADMINTON, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.BOWLING, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.TENNIS, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.SKI, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.GOLF, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.CLIMBING, 1, cEXERCISE));
        categoryRepository.save(new Category((long) i++, CategoryName.DIET, 1, cEXERCISE));
        categoryRepository.save(FITNESS);
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
        STUDY = new Category((long) i++, CategoryName.STUDY, 1, cJOB);
        SIDE_PROJECT = new Category((long) i++, CategoryName.SIDE_PROJECT, 1, cJOB);
        INVESTMENT = new Category((long) i++, CategoryName.INVESTMENT, 1, cJOB);
        BUSINESS = new Category((long) i++, CategoryName.BUSINESS, 1, cJOB);
        categoryRepository.save(INVESTMENT);
        categoryRepository.save(new Category((long) i++, CategoryName.BRANDING, 1, cJOB));
        categoryRepository.save(SIDE_PROJECT);
        categoryRepository.save(BUSINESS);
        categoryRepository.save(new Category((long) i++, CategoryName.CAREER, 1, cJOB));
        categoryRepository.save(STUDY);
        categoryRepository.save(new Category((long) i++, CategoryName.FREELANCE, 1, cJOB));
        categoryRepository.save(new Category((long) i++, CategoryName.N_JOB, 1, cJOB));

        // LANGUAGE 2차
        JAPANESE = new Category((long) i++, CategoryName.JAPANESE, 1, cLANGUAGE);
        categoryRepository.save(new Category((long) i++, CategoryName.ENGLISH, 1, cLANGUAGE));
        categoryRepository.save(JAPANESE);
        categoryRepository.save(new Category((long) i++, CategoryName.CHINESE, 1, cLANGUAGE));
        categoryRepository.save(new Category((long) i++, CategoryName.FRENCH, 1, cLANGUAGE));
        categoryRepository.save(new Category((long) i++, CategoryName.SPANISH, 1, cLANGUAGE));
        categoryRepository.save(new Category((long) i++, CategoryName.RUSSIAN, 1, cLANGUAGE));
        categoryRepository.save(new Category((long) i++, CategoryName.OTHER_LANGUAGE, 1, cLANGUAGE));

        // CULTURAL 2차
        MUSICAL = new Category((long) i++, CategoryName.MUSICAL, 1, cCULTURAL);
        MOVIE = new Category((long) i++, CategoryName.MOVIE, 1, cCULTURAL);
        categoryRepository.save(MUSICAL);
        categoryRepository.save(new Category((long) i++, CategoryName.SHOW, 1, cCULTURAL));
        categoryRepository.save(MOVIE);
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
        DOG = new Category((long) i++, CategoryName.DOG, 1, cPET);
        categoryRepository.save(DOG);
        categoryRepository.save(new Category((long) i++, CategoryName.CAT, 1, cPET));
        categoryRepository.save(new Category((long) i++, CategoryName.FISH, 1, cPET));
        categoryRepository.save(new Category((long) i++, CategoryName.REPTILE, 1, cPET));
        categoryRepository.save(new Category((long) i++, CategoryName.BIRD, 1, cPET));
        categoryRepository.save(new Category((long) i++, CategoryName.RODENT, 1, cPET));

        // AMITY 2차
        SMALL_TALK = new Category((long) i++, CategoryName.SMALL_TALK, 1, cAMITY);
        MUST_GO_RESTAURANT = new Category((long) i++, CategoryName.MUST_GO_RESTAURANT, 1, cAMITY);
        CONCERN = new Category((long) i++, CategoryName.CONCERN, 1, cAMITY);
        PARENTING = new Category((long) i++, CategoryName.PARENTING, 1, cAMITY);
        categoryRepository.save(MUST_GO_RESTAURANT);
        categoryRepository.save(new Category((long) i++, CategoryName.CAFE, 1, cAMITY));
        categoryRepository.save(new Category((long) i++, CategoryName.DINING, 1, cAMITY));
        categoryRepository.save(new Category((long) i++, CategoryName.PEER, 1, cAMITY));
        categoryRepository.save(new Category((long) i++, CategoryName.NEIGHBORHOOD, 1, cAMITY));
        categoryRepository.save(CONCERN);
        categoryRepository.save(PARENTING);
        categoryRepository.save(SMALL_TALK);

        // HOBBY 2차
        VOLUNTEER = new Category((long) i++, CategoryName.VOLUNTEER, 1, cHOBBY);
        categoryRepository.save(new Category((long) i++, CategoryName.CAR, 1, cHOBBY));
        categoryRepository.save(new Category((long) i++, CategoryName.PHOTOGRAPHY, 1, cHOBBY));
        categoryRepository.save(new Category((long) i++, CategoryName.GAME, 1, cHOBBY));
        categoryRepository.save(VOLUNTEER);
        categoryRepository.save(new Category((long) i++, CategoryName.FLOGGING, 1, cHOBBY));

    }
}
