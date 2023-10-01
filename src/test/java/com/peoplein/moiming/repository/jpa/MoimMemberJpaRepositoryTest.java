package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.repository.InvalidQueryParameterException;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.support.RepositoryTestConfiguration;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Import({RepositoryTestConfiguration.class, MoimMemberJpaRepository.class})
@DataJpaTest
public class MoimMemberJpaRepositoryTest extends TestObjectCreator {


    @Autowired
    private MoimMemberRepository moimMemberRepository;

    @Autowired
    private EntityManager em;

    private Role testRole;
    private Member testMember;
    private Member testMember2;
    private Moim testMoim;

    @BeforeEach
    void be() {
        // Role 주입
        Role testRole = makeTestRole(RoleType.USER);

        // Member 주입
        testMember = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);

        // Category 주입
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth1SampleCategory2), 2, testCategory1);

        // Moim 주입
        testMoim = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), testMember);

        em.persist(testRole);
        em.persist(testMember);
        em.persist(testCategory1);
        em.persist(testCategory1_1);
        em.persist(testMoim);

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
    void findByMemberAndMoimId_shouldReturnOptionalMoimMember_whenBothIdPassed() {

        // given
        Long memberId = testMember.getId();
        Long moimId = testMoim.getId();

        // when
        Optional<MoimMember> moimMemberOp = moimMemberRepository.findByMemberAndMoimId(memberId, moimId);

        // then
        assertTrue(moimMemberOp.isPresent());
        assertThat(moimMemberOp.get().getMember().getId()).isEqualTo(memberId);
        assertThat(moimMemberOp.get().getMoim().getId()).isEqualTo(moimId);
        assertThat(moimMemberOp.get().getMemberRoleType()).isEqualTo(MoimMemberRoleType.MANAGER);
        assertThat(moimMemberOp.get().getMemberState()).isEqualTo(MoimMemberState.ACTIVE);
    }


    @Test
    void findByMemberAndMoimId_shouldReturnEmptyOptional_whenNotExists() {
        // given
        makeAnotherMember();
        Long member2Id = testMember2.getId();
        Long moimId = testMoim.getId();

        // when
        Optional<MoimMember> moimMemberOp = moimMemberRepository.findByMemberAndMoimId(member2Id, moimId);

        // then
        assertTrue(moimMemberOp.isEmpty());

    }

    @Test
    void findByMemberAndMoimId_shouldThrowException_whenNullPassed_byInvalidQueryParameterException() {

        // given
        // when
        // then
        assertThatThrownBy(() -> moimMemberRepository.findByMemberAndMoimId(null, null)).isInstanceOf(InvalidQueryParameterException.class);

    }
}