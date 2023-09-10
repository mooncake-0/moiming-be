package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.repository.RoleRepository;
import com.peoplein.moiming.support.RepositoryTestConfiguration;
import com.peoplein.moiming.support.TestModelParams;
import com.peoplein.moiming.support.TestObjectCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;


@Import({RepositoryTestConfiguration.class, MoimJpaRepository.class})
@ActiveProfiles("test")
@DataJpaTest
public class MoimJpaRepositoryTest extends TestObjectCreator {


    @Autowired
    private MoimRepository moimRepository;


    @Autowired
    private EntityManager em;


    private Member moimCreator;


    private Moim sampleMoim1;


    private MoimJoinRule sampleMoim1JoinRule;

    @BeforeEach
    void be() {
        // Role 및 Member 저장
        Role testRole = makeTestRole(RoleType.USER);
        moimCreator = makeTestMember(memberEmail, memberPhone, memberName, testRole);

        // Moim Cateogry 저장
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth1SampleCategory), 2, testCategory1);

        // Moim 저장
        sampleMoim1 = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), moimCreator);
        sampleMoim1JoinRule = makeTestMoimJoinRule(true, 40, 20, MemberGender.N);
        sampleMoim1.setMoimJoinRule(sampleMoim1JoinRule);

        em.persist(testRole);
        em.persist(moimCreator);
        em.persist(testCategory1);
        em.persist(testCategory1_1);
        moimRepository.save(sampleMoim1);

        em.flush();
        em.clear();
    }


    @Test
    void save_shouldSave_whenMoimEntityPassed() {

    }

    @Test
    void findById_shouldReturnOptionalMoim_whenMoimIdPassed() {

    }

    @Test
    void findWithJoinRuleById_shouldReturnOptionalMoim_whenMoimIdPassed() {

        // given
        Long moimId = sampleMoim1.getId();


        em.flush();
        em.clear();


        // when
        Optional<Moim> moimOp = moimRepository.findWithJoinRuleById(moimId);


        // then
        assertTrue(moimOp.isPresent());
        assertThat(moimOp.get().getMoimName()).isEqualTo(moimName);
        assertThat(moimOp.get().getMoimJoinRule().getId()).isEqualTo(sampleMoim1JoinRule.getId());
        assertThat(moimOp.get().getMoimCategoryLinkers().size()).isEqualTo(2);

    }


    @Test
    void findWithJoinRuleById_shouldReturnOptionalMoimWithoutJoinRule_whenMoimIdPassed() {

        // given
        Long moimId = sampleMoim1.getId();

        em.persist(sampleMoim1);
        sampleMoim1.setMoimJoinRule(null);

        em.flush();
        em.clear();

        // when
        Optional<Moim> moimOp = moimRepository.findWithJoinRuleById(moimId);

        // then
        assertTrue(moimOp.isPresent());
        assertThat(moimOp.get().getMoimName()).isEqualTo(moimName);
        assertThat(moimOp.get().getMoimJoinRule()).isEqualTo(null);
        assertThat(moimOp.get().getMoimCategoryLinkers().size()).isEqualTo(2);

    }


}
