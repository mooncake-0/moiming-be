package com.peoplein.moiming;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.fixed.*;
import com.peoplein.moiming.domain.rules.MoimRule;
import com.peoplein.moiming.domain.rules.RuleJoin;
import com.peoplein.moiming.domain.rules.RulePersist;
import com.peoplein.moiming.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Component
public class InitDatabaseQuery {

    private final EntityManager em;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final MoimRepository moimRepository;
    private final MoimReviewRepository moimReviewRepository;
    private final RoleRepository roleRepository;

    public InitDatabaseQuery(EntityManager em,
                             PasswordEncoder passwordEncoder,
                             CategoryRepository categoryRepository,
                             MemberRepository memberRepository,
                             MoimRepository moimRepository,
                             MoimReviewRepository moimReviewRepository,
                             RoleRepository roleRepository) {
        this.em = em;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.memberRepository = memberRepository;
        this.moimRepository = moimRepository;
        this.moimReviewRepository = moimReviewRepository;
        this.roleRepository = roleRepository;
    }

    private Long moim1Id;
    private Long moimPostId;

    public void initUserRole() {
        Role role = new Role(1L, "일반회원", RoleType.USER);
        Role role2 = new Role(2L, "관리자", RoleType.ADMIN);

        em.persist(role);
        em.persist(role2);
    }

    public void initMemberWithAdminGrant() {

        Role role = roleRepository.findByRoleType(RoleType.ADMIN);

        Member member = Member.createMember(
                InitConstant.WOOSEOK_UID,
                passwordEncoder.encode(InitConstant.WOOSEOK_PASS),
                InitConstant.WOOSEOK_EMAIL,
                InitConstant.WOOSEOK_NAME,
                "",
                InitConstant.WOOSEOK_GENDER,
                role);

        em.persist(member);
    }

    public void initMemberWithUserGrant() {

        Role role = roleRepository.findByRoleType(RoleType.USER);

        Member member1 = Member.createMember(
                InitConstant.WOOJIN_UID,
                passwordEncoder.encode(InitConstant.WOOJIN_PASS),
                InitConstant.WOOJIN_EMAIL,
                InitConstant.WOOJIN_NAME,
                "",
                InitConstant.WOOJIN_GENDER,
                role);

        Member member2 = Member.createMember(
                InitConstant.BYUNGHO_UID,
                passwordEncoder.encode(InitConstant.BYUNGHO_PASS),
                InitConstant.BYUNGHO_EMAIL,
                InitConstant.BYUNGHO_NAME,
                "",
                InitConstant.BYUNGHO_GENDER,
                role);

        Member member3 = Member.createMember(
                InitConstant.JUBIN_UID,
                passwordEncoder.encode(InitConstant.JUBIN_PASS),
                InitConstant.JUBIN_EMAIL,
                InitConstant.JUBIN_NAME,
                "",
                InitConstant.JUBIN_GENDER,
                role);


        em.persist(member1);
        em.persist(member2);
        em.persist(member3);

    }

    public void initMoimCategory() {

        Category friendship = new Category();
        initCategoryInstance(friendship, 1L, CategoryName.FRIENDSHIP, 1, true, null);

        Category study = new Category();
        initCategoryInstance(study, 2L, CategoryName.STUDY, 1, true, null);

        Category hobby = new Category();
        initCategoryInstance(hobby, 3L, CategoryName.HOBBY, 1, true, null);

        Category friendshipFood = new Category();
        initCategoryInstance(friendshipFood, 4L, CategoryName.FOOD, 2, true, friendship);

        Category friendshipAlcohol = new Category();
        initCategoryInstance(friendshipAlcohol, 5L, CategoryName.ALCOHOL, 2, true, friendship);

        Category studyCoding = new Category();
        initCategoryInstance(studyCoding, 6L, CategoryName.CODING, 2, true, study);

        Category studyChemistry = new Category();
        initCategoryInstance(studyChemistry, 7L, CategoryName.CHEMISTRY, 2, true, study);

        Category hobbyPhoto = new Category();
        initCategoryInstance(hobbyPhoto, 8L, CategoryName.PHOTOGRAPHY, 2, true, hobby);

        Category hobbyReading = new Category();
        initCategoryInstance(hobbyReading, 9L, CategoryName.READING, 2, true, hobby);

        em.persist(friendship);
        em.persist(friendshipAlcohol);
        em.persist(friendshipFood);
        em.persist(study);
        em.persist(studyChemistry);
        em.persist(studyCoding);
        em.persist(hobby);
        em.persist(hobbyPhoto);
        em.persist(hobbyReading);
    }


    public void initMoimEntity() {

        Member curMember = memberRepository.findMemberByUid(InitConstant.WOOSEOK_UID);

        Moim moim = Moim.createMoim(
                "모이밍",
                "모이밍을 만드는 사람들입니다",
                "",
                new Area("서울시", "중구"),
                curMember.getUid()
        );

        //Category, MML, Rule 을 만들어주면 됨.
        MoimRule mr = new RuleJoin(
                2000, 1993
                , MemberGender.M, 3
                , true, true
                , moim, curMember.getUid()
        );

        List<CategoryName> categoryNames = new ArrayList<>();
        categoryNames.add(CategoryName.FRIENDSHIP);
        categoryNames.add(CategoryName.ALCOHOL);

        List<Category> categories = categoryRepository.findByCategoryNames(categoryNames);

        for (Category category : categories) {
            MoimCategoryLinker mcl = new MoimCategoryLinker(moim, category);
            em.persist(mcl);
        }

        MemberMoimLinker.memberJoinMoim(curMember, moim, MoimRoleType.CREATOR, MoimMemberState.ACTIVE);

        em.persist(moim);

        moim1Id = moim.getId();

    }

    public void initMoimEntity2() {

        Member curMember = memberRepository.findMemberByUid(InitConstant.WOOSEOK_UID);

        Moim moim = Moim.createMoim(
                "개지랄하기",
                "지랄하는 사람들입니다",
                "",
                new Area("서울시", "중구"),
                curMember.getUid()
        );

        //Category, MML, Rule 을 만들어주면 됨.
        MoimRule mr = new RuleJoin(
                2000, 1995
                , MemberGender.M, 4
                , false, false
                , moim, curMember.getUid()
        );

        MoimRule mr2 = new RulePersist(
                true, 2, 2
                , moim, curMember.getUid()
        );

        List<CategoryName> categoryNames = new ArrayList<>();
        categoryNames.add(CategoryName.STUDY);
        categoryNames.add(CategoryName.CODING);

        List<Category> categories = categoryRepository.findByCategoryNames(categoryNames);

        for (Category category : categories) {
            MoimCategoryLinker mcl = new MoimCategoryLinker(moim, category);
            em.persist(mcl);
        }

        MemberMoimLinker.memberJoinMoim(curMember, moim, MoimRoleType.CREATOR, MoimMemberState.ACTIVE);

        em.persist(moim);

    }

    public void joinMoim1OfMember2() {


        Moim moim = moimRepository.findById(moim1Id);
        Member curMember = memberRepository.findMemberByUid(InitConstant.WOOJIN_UID);
        Member curMember2 = memberRepository.findMemberByUid(InitConstant.BYUNGHO_UID);

        MemberMoimLinker.memberJoinMoim(curMember, moim, MoimRoleType.NORMAL, MoimMemberState.ACTIVE);
        MemberMoimLinker.memberJoinMoim(curMember2, moim, MoimRoleType.NORMAL, MoimMemberState.ACTIVE);
    }


    public void initPostByMember1() {

        Moim moim = moimRepository.findById(moim1Id);
        Member member1 = memberRepository.findMemberByUid(InitConstant.WOOSEOK_UID);

        MoimPost moimPost1 = MoimPost.createMoimPost(
                "이것은 게시물 1번"
                , "게시물 1번의 내용입니다"
                , MoimPostCategory.GREETING
                , true, false
                , moim, member1
        );

        em.persist(moimPost1);
        moimPostId = moimPost1.getId();
    }

    public void initPostByMember2() {

        Moim moim = moimRepository.findById(moim1Id);
        Member member2 = memberRepository.findMemberByUid(InitConstant.WOOJIN_UID);

        MoimPost moimPost1 = MoimPost.createMoimPost(
                "이것은 게시물 23번"
                , "게시물 23번의 내용입니다"
                , MoimPostCategory.NOTICE
                , false, false
                , moim, member2
        );

        em.persist(moimPost1);
    }

    public void initPostComment() {

        Member member1 = memberRepository.findMemberByUid(InitConstant.WOOSEOK_UID);
        Member member2 = memberRepository.findMemberByUid(InitConstant.WOOJIN_UID);
        MoimPost moimPost = em.find(MoimPost.class, moimPostId);

        PostComment pc1 = PostComment.createPostComment("꼭 확인해주세요", member1, moimPost);
        PostComment pc2 = PostComment.createPostComment("넵 알겠습니다~!", member2, moimPost);

        em.persist(pc1);
        em.persist(pc2);
    }


    public void initSchedule1InMoim1() {

        Moim moim = moimRepository.findById(moim1Id);
        Member member1 = memberRepository.findMemberByUid(InitConstant.WOOSEOK_UID);
        Member member2 = memberRepository.findMemberByUid(InitConstant.WOOJIN_UID);

        Schedule schedule = Schedule.createSchedule(
                "모이밍 번개회식", "사당역 이자카야",
                LocalDateTime.now(), 6, moim, member1
        );

        MemberScheduleLinker.memberJoinSchedule(
                member2, schedule, ScheduleMemberState.ATTEND
        );

        em.persist(schedule);

    }

    public void initSchedule2InMoim1() {

        Moim moim = moimRepository.findById(moim1Id);
        Member member1 = memberRepository.findMemberByUid(InitConstant.WOOSEOK_UID);
        Member member2 = memberRepository.findMemberByUid(InitConstant.WOOJIN_UID);

        Schedule schedule = Schedule.createSchedule(
                "정기회식", "냠냠냠냠",
                LocalDateTime.now(), 4, moim, member2
        );

        MemberScheduleLinker.memberJoinSchedule(
                member1, schedule, ScheduleMemberState.ATTEND
        );

        em.persist(schedule);
    }

    public void initSchedule3InMoim1() {

        Moim moim = moimRepository.findById(moim1Id);

        Member member1 = memberRepository.findMemberByUid(InitConstant.WOOSEOK_UID);
        Member member2 = memberRepository.findMemberByUid(InitConstant.WOOJIN_UID);
        Member member3 = memberRepository.findMemberByUid(InitConstant.BYUNGHO_UID);

        Schedule schedule = Schedule.createSchedule(
                "송별회 회식", "동탄역 북광장",
                LocalDateTime.now(), 5, moim, member1
        );

        MemberScheduleLinker.memberJoinSchedule(
                member2, schedule, ScheduleMemberState.ATTEND
        );
        MemberScheduleLinker.memberJoinSchedule(
                member3, schedule, ScheduleMemberState.ATTEND
        );

        em.persist(schedule);
    }


    public void initReviewQuestionsAndChoices() {

        ReviewQuestion question1 = new ReviewQuestion(ReviewQuestionType.CHOICE, QuestionName.IMPRESSION, "첫 모임 어떠셨어요?");

        QuestionChoice question1Choice1 = new QuestionChoice("재밌었어요", 1, question1);
        QuestionChoice question1Choice2 = new QuestionChoice("친해지기 어려웠어요", 2, question1);
        QuestionChoice question1Choice3 = new QuestionChoice("나쁘지 않았어요", 3, question1);
        QuestionChoice question1Choice4 = new QuestionChoice("다음에 또 나가고 싶어요", 4, question1);


        ReviewQuestion question2 = new ReviewQuestion(ReviewQuestionType.CHOICE, QuestionName.VIBE, "모임 분위기는 어땠나요?");

        QuestionChoice question2Choice1 = new QuestionChoice("진지했어요", 1, question2);
        QuestionChoice question2Choice2 = new QuestionChoice("텐션이 높았어요", 2, question2);
        QuestionChoice question2Choice3 = new QuestionChoice("기존 멤버들끼리만 너무 친해보였어요", 3, question2);


        ReviewQuestion question3 = new ReviewQuestion(ReviewQuestionType.CHOICE, QuestionName.EXPECTED, "가입당시 기대했던 모임과 실제 모임과 차이가 있나요?");

        QuestionChoice question3Choice1 = new QuestionChoice("기대했던대로에요", 1, question3);
        QuestionChoice question3Choice2 = new QuestionChoice("기대했던 것과 다르지만 만족해요", 2, question3);
        QuestionChoice question3Choice3 = new QuestionChoice("기대했던 것과 달라요", 3, question3);


        ReviewQuestion question4 = new ReviewQuestion(ReviewQuestionType.CHOICE, QuestionName.REENGAGE, "다음에 모임에 또 참여하고 싶으세요?");

        QuestionChoice question4Choice1 = new QuestionChoice("네", 1, question4);
        QuestionChoice question4Choice2 = new QuestionChoice("아니요", 2, question4);

        em.persist(question1);
        em.persist(question2);
        em.persist(question3);
        em.persist(question4);
    }

    public void initReviewAnswers() {

        Moim moim = moimRepository.findById(moim1Id);
        Member member1 = memberRepository.findMemberByUid(InitConstant.WOOSEOK_UID);
        Member member2 = memberRepository.findMemberByUid(InitConstant.WOOJIN_UID);
        Member member3 = memberRepository.findMemberByUid(InitConstant.BYUNGHO_UID);

        MoimReview moimReview1 = MoimReview.writeReview(member1, moim);
        MoimReview moimReview2 = MoimReview.writeReview(member2, moim);
        MoimReview moimReview3 = MoimReview.writeReview(member3, moim);

        List<ReviewQuestion> reviewQuestions = moimReviewRepository.findAllReviewQuestions();

        ReviewAnswer mr1_anw1 = ReviewAnswer.createAnswer(1, "", moimReview1, findReviewQuestion(reviewQuestions, QuestionName.IMPRESSION));
        ReviewAnswer mr1_anw2 = ReviewAnswer.createAnswer(1, "", moimReview1, findReviewQuestion(reviewQuestions, QuestionName.VIBE));
        ReviewAnswer mr1_anw3 = ReviewAnswer.createAnswer(1, "", moimReview1, findReviewQuestion(reviewQuestions, QuestionName.EXPECTED));
        ReviewAnswer mr1_anw4 = ReviewAnswer.createAnswer(1, "", moimReview1, findReviewQuestion(reviewQuestions, QuestionName.REENGAGE));

        ReviewAnswer mr2_anw1 = ReviewAnswer.createAnswer(2, "", moimReview2, findReviewQuestion(reviewQuestions, QuestionName.IMPRESSION));
        ReviewAnswer mr2_anw2 = ReviewAnswer.createAnswer(2, "", moimReview2, findReviewQuestion(reviewQuestions, QuestionName.VIBE));
        ReviewAnswer mr2_anw3 = ReviewAnswer.createAnswer(2, "", moimReview2, findReviewQuestion(reviewQuestions, QuestionName.EXPECTED));
        ReviewAnswer mr2_anw4 = ReviewAnswer.createAnswer(2, "", moimReview2, findReviewQuestion(reviewQuestions, QuestionName.REENGAGE));

        ReviewAnswer mr3_anw1 = ReviewAnswer.createAnswer(4, "", moimReview3, findReviewQuestion(reviewQuestions, QuestionName.IMPRESSION));
        ReviewAnswer mr3_anw2 = ReviewAnswer.createAnswer(3, "", moimReview3, findReviewQuestion(reviewQuestions, QuestionName.VIBE));
        ReviewAnswer mr3_anw3 = ReviewAnswer.createAnswer(3, "", moimReview3, findReviewQuestion(reviewQuestions, QuestionName.EXPECTED));
        ReviewAnswer mr3_anw4 = ReviewAnswer.createAnswer(1, "", moimReview3, findReviewQuestion(reviewQuestions, QuestionName.REENGAGE));

        em.persist(moimReview1);
        em.persist(moimReview2);
        em.persist(moimReview3);
    }

    public void initSessionCategories() {
        SessionCategory sessionCategory1 = SessionCategory.createSessionCategory(SessionCategoryType.ACTIVITY);
        SessionCategory sessionCategory2= SessionCategory.createSessionCategory(SessionCategoryType.FOOD);
        SessionCategory sessionCategory3 = SessionCategory.createSessionCategory(SessionCategoryType.ALCOHOL);
        SessionCategory sessionCategory4 = SessionCategory.createSessionCategory(SessionCategoryType.DRINKS);
        SessionCategory sessionCategory5 = SessionCategory.createSessionCategory(SessionCategoryType.EXTRA);

        em.persist(sessionCategory1);
        em.persist(sessionCategory2);
        em.persist(sessionCategory3);
        em.persist(sessionCategory4);
        em.persist(sessionCategory5);
    }

    private ReviewQuestion findReviewQuestion(List<ReviewQuestion> reviewQuestions, QuestionName questionName) {
        return reviewQuestions.stream().filter(q -> q.getQuestionName().equals(questionName)).findAny().orElseThrow(() -> new RuntimeException(questionName + " 이런 질문 없습니다"));
    }


    private void initCategoryInstance(Category instance, Long id, CategoryName categoryName, int categoryDepth, boolean using, Category parent) {
        instance.setId(id);
        instance.setCategoryName(categoryName);
        instance.setCategoryDepth(categoryDepth);
        instance.setUsing(using);
        if (parent != null) {
            instance.setParent(parent);
        }

    }

    private static class InitConstant {

        public static String WOOSEOK_EMAIL = "a@moiming.net";
        public static String WOOSEOK_NAME = "강우석";
        public static MemberGender WOOSEOK_GENDER = MemberGender.M;
        public static String WOOSEOK_UID = "wrock.kang";
        public static String WOOSEOK_PASS = "1234";


        public static String WOOJIN_EMAIL = "w@moiming.net";
        public static String WOOJIN_NAME = "김우진";
        public static MemberGender WOOJIN_GENDER = MemberGender.M;
        public static String WOOJIN_UID = "kwj3591";
        public static String WOOJIN_PASS = "1234";

        public static String BYUNGHO_EMAIL = "i@moiming.net";
        public static String BYUNGHO_NAME = "박병호";
        public static MemberGender BYUNGHO_GENDER = MemberGender.M;
        public static String BYUNGHO_UID = "parkho1234";
        public static String BYUNGHO_PASS = "1234";

        public static String JUBIN_EMAIL = "b@moiming.net";
        public static String JUBIN_NAME = "이주빈";
        public static MemberGender JUBIN_GENDER = MemberGender.F;
        public static String JUBIN_UID = "jbjb";
        public static String JUBIN_PASS = "1234";

    }
}

