package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.MoimCategoryLinker;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.rules.MoimRule;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.repository.MoimRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;


/*
 TODO TC::
 Test 해제
 Docker 미사용으로 기본적인 Test 환경 구축 우선
 - MSL Refactor 이후 재진행 예정
 */
@SpringBootTest
@Transactional
public class MoimJpaRepositoryTest  {

    @Autowired
    private EntityManager em;

    @Autowired
    MoimRuleRepository moimRuleRepository;

    @Autowired
    MoimRepository moimRepository;

    private Moim moim;
    private MoimRule moimRule;
    private List<Category> moimCategories;

    @BeforeEach
    void initInstance() {
        moim = TestUtils.initMoimAndRuleJoin();
        moimRule = moim.getMoimRules().get(0);
        moimCategories = TestUtils.createMoimCategoriesWithTwo();

        for (int i = 0; i < moimCategories.size(); i++) {
            Category category = moimCategories.get(i);
            category.setId((long) i);
            em.persist(category);
        }
    }

//    @Test
    @DisplayName("성공 @ 저장")
    void save() {
        //given
        //when
        Long moimId = moimRepository.save(moim);

        flushAndClear();
        Moim moim1 = em.find(Moim.class, moimId);
        //then
        assertEquals(moim1.getId(), moim.getId());
    }


//    @Test
    @DisplayName("성공 @ findMoimById")
    void findById() {
        // given
        Long moimId = moimRepository.save(moim);
        flushAndClear();
        // when
        Moim foundMoim = moimRepository.findById(moim.getId());
        // then (동등성 비교 필요, 동일성 X - EM이 초기화 되었기 때문)
        assertEquals(moimId, foundMoim.getId());
        assertEquals(moim.getMoimName(), foundMoim.getMoimName());
        assertEquals(moim.getMoimInfo(), foundMoim.getMoimInfo());
        assertEquals(moim.getMoimPfImg(), foundMoim.getMoimPfImg());
        assertEquals(moim.getCreatedMemberId(), foundMoim.getCreatedMemberId());
        assertEquals(moim.getMoimArea().getState(), foundMoim.getMoimArea().getState());
    }

//    @Test
    void findWithRuleByIdTest() {

        Long moimId = moimRepository.save(moim);
        Long ruleId = moimRuleRepository.save(moimRule);
        flushAndClear();

        Moim findMoim = moimRepository.findWithJoinRuleById(moimId).orElseThrow();

        assertThat(findMoim.getId()).isEqualTo(moimId);
        assertThat(findMoim.getRuleJoin().getId()).isEqualTo(ruleId);
    }

//    @Test
    @DisplayName("Keyword 1개 기본")
    void findMoimBySearchConditionTest1() {
        // Given
        Moim moim1 = TestUtils.createMoimOnly();
        MoimCategoryLinker moimCategoryLinker1 = new MoimCategoryLinker(moim1, moimCategories.get(0));

        persist(moim1, moimCategoryLinker1);
        flushAndClear();

        // When
        List<Moim> findMoimList = moimRepository.findMoimBySearchCondition(List.of(moim1.getMoimName()), null, null);

        // Then
        assertThat(findMoimList.size()).isEqualTo(1);
        assertThat(findMoimList)
                .extracting("moimName")
                .contains(moim1.getMoimName());
    }

//    @Test
    @DisplayName("KeyWord Like 조건")
    void findMoimBySearchConditionTest2() {

        // Given
        Moim moim1 = TestUtils.createMoimOnly();
        MoimCategoryLinker moimCategoryLinker1 = new MoimCategoryLinker(moim1, moimCategories.get(0));

        Moim moim2 = TestUtils.createOtherMoimOnly("other" + TestUtils.moimName, new Area("경상북도", "대구"));
        MoimCategoryLinker moimCategoryLinker2 = new MoimCategoryLinker(moim2, moimCategories.get(1));

        Moim moim3 = TestUtils.createOtherMoimOnly("other moim", new Area("경상북도", "대구"));
        MoimCategoryLinker moimCategoryLinker3 = new MoimCategoryLinker(moim3, moimCategories.get(1));

        persist(moim1, moim2, moimCategoryLinker1, moimCategoryLinker2, moim3, moimCategoryLinker3);
        flushAndClear();

        // When
        List<Moim> findMoimList = moimRepository.findMoimBySearchCondition(List.of(TestUtils.moimName), null, null);

        // Then
        assertThat(findMoimList.size()).isEqualTo(2);
        assertThat(findMoimList)
                .extracting("moimName")
                .contains(
                        "other" + TestUtils.moimName,
                        TestUtils.moimName
                );
    }

//    @Test
    @DisplayName("KeyWord Like 조건 여러 개")
    void findMoimBySearchConditionTest3() {

        // Given
        Moim moim1 = TestUtils.createMoimOnly();
        MoimCategoryLinker moimCategoryLinker1 = new MoimCategoryLinker(moim1, moimCategories.get(0));

        Moim moim2 = TestUtils.createOtherMoimOnly("other" + TestUtils.moimName, new Area("경상북도", "대구"));
        MoimCategoryLinker moimCategoryLinker2 = new MoimCategoryLinker(moim2, moimCategories.get(1));

        Moim moim3 = TestUtils.createOtherMoimOnly("other moim", new Area("경상북도", "대구"));
        MoimCategoryLinker moimCategoryLinker3 = new MoimCategoryLinker(moim3, moimCategories.get(1));

        persist(moim1, moim2, moimCategoryLinker1, moimCategoryLinker2, moim3, moimCategoryLinker3);
        flushAndClear();

        List<String> keywords = List.of(TestUtils.moimName, "moim");

        // When
        List<Moim> findMoimList = moimRepository.findMoimBySearchCondition(keywords, null, null);

        // Then
        assertThat(findMoimList.size()).isEqualTo(3);
        assertThat(findMoimList)
                .extracting("moimName")
                .contains(
                        "other" + TestUtils.moimName,
                        TestUtils.moimName,
                        "other moim"
                );
    }

//    @Test
    @DisplayName("이름 && Area 조건")
    void findMoimBySearchConditionTest4() {

        // Given
        Moim moim1 = TestUtils.createMoimOnly();
        MoimCategoryLinker moimCategoryLinker1 = new MoimCategoryLinker(moim1, moimCategories.get(0));

        Moim moim2 = TestUtils.createOtherMoimOnly("other" , new Area("경상북도", "대구"));
        MoimCategoryLinker moimCategoryLinker2 = new MoimCategoryLinker(moim2, moimCategories.get(1));

        persist(moim1, moim2, moimCategoryLinker1, moimCategoryLinker2);
        flushAndClear();

        List<String> keywords = List.of(TestUtils.moimName);

        // When
        List<Moim> findMoimList = moimRepository.findMoimBySearchCondition(keywords, new Area("경상북도", "대구"), null);

        // Then
        assertThat(findMoimList.size()).isEqualTo(0);
    }

//    @Test
    @DisplayName("이름 && Area 조건")
    void findMoimBySearchConditionTest5() {
        // Given
        Moim moim1 = TestUtils.createMoimOnly();
        MoimCategoryLinker moimCategoryLinker1 = new MoimCategoryLinker(moim1, moimCategories.get(0));

        Moim moim2 = TestUtils.createOtherMoimOnly("other" , new Area("경상북도", "대구"));
        MoimCategoryLinker moimCategoryLinker2 = new MoimCategoryLinker(moim2, moimCategories.get(1));

        persist(moim1, moim2, moimCategoryLinker1, moimCategoryLinker2);
        flushAndClear();

        List<String> keywords = List.of("other");

        // When
        List<Moim> findMoimList = moimRepository.findMoimBySearchCondition(keywords, new Area("경상북도", "대구"), null);

        // Then
        assertThat(findMoimList.size()).isEqualTo(1);
    }

//    @Test
    @DisplayName("이름 && Area 조건")
    void findMoimBySearchConditionTest6() {
        // Given
        Moim moim1 = TestUtils.createMoimOnly();
        MoimCategoryLinker moimCategoryLinker1 = new MoimCategoryLinker(moim1, moimCategories.get(0));

        Moim moim2 = TestUtils.createOtherMoimOnly("other" , new Area("경상북도", "대구"));
        MoimCategoryLinker moimCategoryLinker2 = new MoimCategoryLinker(moim2, moimCategories.get(1));

        persist(moim1, moim2, moimCategoryLinker1, moimCategoryLinker2);
        flushAndClear();

        List<String> keywords = List.of("other");

        // When
        List<Moim> findMoimList = moimRepository.findMoimBySearchCondition(keywords, new Area("경상북도", "경산"), null);

        // Then
        assertThat(findMoimList.size()).isEqualTo(0);
    }


//    @Test
    @DisplayName("이름 && category 조건")
    void findMoimBySearchConditionTest7() {

        // Given
        Moim moim1 = TestUtils.createMoimOnly();
        MoimCategoryLinker moimCategoryLinker1 = new MoimCategoryLinker(moim1, moimCategories.get(0));

        Moim moim2 = TestUtils.createOtherMoimOnly("the" , new Area("경상북도", "대구"));
        MoimCategoryLinker moimCategoryLinker2 = new MoimCategoryLinker(moim2, moimCategories.get(1));

        Moim moim3 = TestUtils.createOtherMoimOnly("other" , new Area("경상북도", "대구"));
        MoimCategoryLinker moimCategoryLinker3 = new MoimCategoryLinker(moim2, moimCategories.get(0));

        persist(moim1, moim2, moimCategoryLinker1, moimCategoryLinker2, moim3, moimCategoryLinker3);
        flushAndClear();

        List<String> keywords = List.of("other", TestUtils.moimName);

        // When
        List<Moim> findMoimList = moimRepository.findMoimBySearchCondition(keywords, null, moimCategories.get(0));

        // Then
        assertThat(findMoimList.size()).isEqualTo(2);
    }


//    @Test
    @DisplayName("이름 && category 조건")
    void findMoimBySearchConditionTest8() {

        // Given
        Moim moim1 = TestUtils.createMoimOnly();
        MoimCategoryLinker moimCategoryLinker1 = new MoimCategoryLinker(moim1, moimCategories.get(0));

        Moim moim2 = TestUtils.createOtherMoimOnly("the" , new Area("경상북도", "대구"));
        MoimCategoryLinker moimCategoryLinker2 = new MoimCategoryLinker(moim2, moimCategories.get(1));

        Moim moim3 = TestUtils.createOtherMoimOnly("other" , new Area("경상북도", "대구"));
        MoimCategoryLinker moimCategoryLinker3 = new MoimCategoryLinker(moim2, moimCategories.get(0));

        persist(moim1, moim2, moimCategoryLinker1, moimCategoryLinker2, moim3, moimCategoryLinker3);
        flushAndClear();

        List<String> keywords = List.of("other");

        // When
        List<Moim> findMoimList = moimRepository.findMoimBySearchCondition(keywords, null, moimCategories.get(0));

        // Then
        assertThat(findMoimList.size()).isEqualTo(1);
    }


//    @Test
    @DisplayName("전체 조건")
    void findMoimBySearchConditionTest9() {

        // Given
        Moim moim1 = TestUtils.createMoimOnly();
        MoimCategoryLinker moimCategoryLinker1 = new MoimCategoryLinker(moim1, moimCategories.get(0));

        Moim moim2 = TestUtils.createOtherMoimOnly("the" , new Area("경상북도", "대구"));
        MoimCategoryLinker moimCategoryLinker2 = new MoimCategoryLinker(moim2, moimCategories.get(1));

        Moim moim3 = TestUtils.createOtherMoimOnly("other" , new Area("경상북도", "대구"));
        MoimCategoryLinker moimCategoryLinker3 = new MoimCategoryLinker(moim2, moimCategories.get(0));

        persist(moim1, moim2, moimCategoryLinker1, moimCategoryLinker2, moim3, moimCategoryLinker3);
        flushAndClear();

        List<String> keywords = List.of("other");

        // When
        List<Moim> findMoimList = moimRepository.findMoimBySearchCondition(keywords, new Area("제주도","서귀포"), moimCategories.get(1));

        // Then
        assertThat(findMoimList.size()).isEqualTo(0);
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private void persist(Object ...objects) {
        Arrays.stream(objects).forEach(o -> em.persist(o));
    }

}
