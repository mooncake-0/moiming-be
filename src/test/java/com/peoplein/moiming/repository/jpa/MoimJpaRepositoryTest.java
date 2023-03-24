package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.BaseTest;
import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.rules.MoimRule;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.repository.MoimRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;

@SpringBootTest
@Transactional
@Rollback(value = false)
public class MoimJpaRepositoryTest extends BaseTest {

    @Autowired
    private EntityManager em;

    @Autowired
    MoimRuleRepository moimRuleRepository;

    @Autowired
    MoimRepository moimRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;
    private Moim moim;
    private MoimRule moimRule;

    @BeforeEach
    void initInstance() {
        TestUtils.truncateAllTable(jdbcTemplate);
        moim = TestUtils.initMoimAndRuleJoin();
        moimRule = moim.getMoimRules().get(0);
    }

    @Test
    @DisplayName("성공 @ 저장")
    void save() {
        //given
        //when
        Long moimId = moimRepository.save(moim);

        em.flush();
        em.clear();

        Moim moim1 = em.find(Moim.class, moimId);
        //then
        assertEquals(moim1.getId(), moim.getId());
    }


    @Test
    @DisplayName("성공 @ findMoimById")
    void findById() {
        // given
        Long moimId = moimRepository.save(moim);
        em.flush();
        em.clear();
        // when
        Moim foundMoim = moimRepository.findById(moim.getId());
        // then (동등성 비교 필요, 동일성 X - EM이 초기화 되었기 때문)
        assertEquals(moimId, foundMoim.getId());
        assertEquals(moim.getMoimName(), foundMoim.getMoimName());
        assertEquals(moim.getMoimInfo(), foundMoim.getMoimInfo());
        assertEquals(moim.getMoimPfImg(), foundMoim.getMoimPfImg());
        assertEquals(moim.getCreatedUid(), foundMoim.getCreatedUid());
        assertEquals(moim.getMoimArea().getState(), foundMoim.getMoimArea().getState());
    }

    @Test
    void findWithRuleByIdTest() {

        Long moimId = moimRepository.save(moim);
        Long ruleId = moimRuleRepository.save(moimRule);
        em.flush();
        em.clear();

        Moim findMoim = moimRepository.findWithRulesById(moimId);

        assertThat(findMoim.getId()).isEqualTo(moimId);
        assertThat(findMoim.getRuleJoin().getId()).isEqualTo(ruleId);
    }
}