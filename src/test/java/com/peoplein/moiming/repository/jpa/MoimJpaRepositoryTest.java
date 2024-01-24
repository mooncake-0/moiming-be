package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.repository.InvalidQueryParameterException;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.support.RepositoryTestConfiguration;
import com.peoplein.moiming.support.TestObjectCreator;
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


    private Role testRole;
    private Member testMember1;
    private Member testMember2;


    private Moim sampleMoim1;


    private MoimJoinRule sampleMoim1JoinRule;

    @BeforeEach
    void be() {
        // Role 및 Member 저장
        testRole = makeTestRole(RoleType.USER);
        testMember1 = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci,testRole);

        // Moim Cateogry 저장
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 0, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth1SampleCategory), 1, testCategory1);

        // Moim 저장
        sampleMoim1 = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), testMember1);
        sampleMoim1JoinRule = makeTestMoimJoinRule(true, 40, 20, MemberGender.N);
        sampleMoim1.setMoimJoinRule(sampleMoim1JoinRule);


        em.persist(testRole);
        em.persist(testMember1);
        em.persist(testCategory1);
        em.persist(testCategory1_1);
        moimRepository.save(sampleMoim1);

        em.flush();
        em.clear();
    }


    void makeAnotherMember() {
        testMember2 = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, testRole);
        em.persist(testMember2);

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

        sampleMoim1 = em.find(Moim.class, moimId); // em.persist 에서 변경, merge 는 권장하지 않음 (재영속화)
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


    // moimId 가 잘못되었을 경우 Optional.empty 반환 CASE
    @Test
    void findWithJoinRuleById_shouldReturnEmptyOptional_whenNotFound() {

        // given
        Long wrongMoimId = 1234L;

        // when
        Optional<Moim> moimOp = moimRepository.findWithJoinRuleById(wrongMoimId);

        // then
        assertTrue(moimOp.isEmpty());

    }


    // moimId 가 Null 일 겨우
    @Test
    void findWithJoinRuleById_shouldThrowException_whenNullPassed_byInvalidQueryParameterException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> moimRepository.findWithJoinRuleById(null)).isInstanceOf(InvalidQueryParameterException.class);

    }


    // findWithMoimMembersById Test
    // 정상동작 2명까지 다 가져오고, moim 도 가져온다 (추가 쿼리도 안나감)
    @Test
    void findWithMoimMembersById_shouldReturnOptionalMoim_whenMoimIdPassed() {

        // given
        makeAnotherMember();
        sampleMoim1 = em.find(Moim.class, sampleMoim1.getId()); // sampleMoim1 재영속화
        MoimMember.memberJoinMoim(testMember2, sampleMoim1, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE); // 연관관계 맺기

        em.flush();
        em.clear();

        // when (한방 쿼리 필요)
        Optional<Moim> moimOp = moimRepository.findWithMoimMemberAndMemberById(sampleMoim1.getId());

        // then
        assertTrue(moimOp.isPresent());
        assertThat(moimOp.get().getMoimMembers().size()).isEqualTo(2);
    }


    // moimId 잘못되었을 경우 - Optional.empty() 반환
    @Test
    void findWithMoimMembersById_shouldReturnEmpty_whenWrongIdPassed() {

        // given
        Long wrongMoimId = 1234L;

        // when
        Optional<Moim> moimOp = moimRepository.findWithMoimMemberAndMemberById(wrongMoimId);

        // then
        assertTrue(moimOp.isEmpty());
    }


    // moimId Null 인 경우
    @Test
    void findWithMoimMembersById_shouldThrowException_whenNullPassed_byInvalidQueryParameterException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> moimRepository.findWithMoimMemberAndMemberById(null)).isInstanceOf(InvalidQueryParameterException.class);

    }


    // findMemberMoimsByConditions Test
    @Test
    void findMemberMoimsWithRuleAndCategoriesByConditionsPaged_shouldReturnList_whenRightInfoPassed() {

        // given

        // when

        // then
    }

}
