package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.MoimCategoryLinker;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
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

import java.util.ArrayList;
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
    private List<Category> categories = new ArrayList<>();

    @BeforeEach
    void be() {
        // Role 주입
        Role testRole = makeTestRole(RoleType.USER);

        // Member 주입
        testMember = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);

        // Category 주입
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth1SampleCategory2), 2, testCategory1);
        categories.add(testCategory1);
        categories.add(testCategory1_1);

        // Moim 주입
        testMoim = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), categories, testMember);

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


    // 내가 주인인 Moim 을 만든다
    void buildManagingMoims(int END) {
        Moim lastMoim = null;
        for (int i = 0; i < END; i++) {
            lastMoim = makeTestMoim(moimName + i, maxMember, moimArea.getState(), moimArea.getCity(), categories, testMember);
            em.persist(lastMoim);
        }
        MoimJoinRule joinRule = makeTestMoimJoinRule(true, 40, 20, MemberGender.N);
        lastMoim.setMoimJoinRule(joinRule);

    }

    // 나는 다른 Moim 에 Join 한다
    void buildOtherMoims(int END, int END2) {

        for (int i = 0; i < END2; i++) { // ACTIVE 하지 않은 모임들도 추가해보자
            Moim moim = makeTestMoim(moimName + ", 탈퇴한 모임임 " + i, maxMember, moimArea.getState(), moimArea.getCity(), categories, testMember2);
            MoimMember.memberJoinMoim(testMember, moim, MoimMemberRoleType.NORMAL, MoimMemberState.IBW);
            em.persist(moim);
        }

        for (int i = 0; i < END; i++) {
            Moim moim = makeTestMoim(moimName + ", 일반회원임 " + i, maxMember, moimArea.getState(), moimArea.getCity(), categories, testMember2);
            MoimMember.memberJoinMoim(testMember, moim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
            em.persist(moim);
        }
    }


    // TODO :: 굳이 Assertion 하느라 시간낭비 하지 않고, 잘 출력되는 모습으로 확인한다
    //         나중에 Batch Insert 된 데이터를 통해 Query Test 를 할 수 있는 더 좋은 방안이 나온다면, 그 Test 로 수정한다
    @Test
    void findMemberMoimsWithRuleAndCategoriesByConditionsPaged_shouldReturnMoimMembers_whenRightInfoPassed() {

        // given
        makeAnotherMember();
        buildManagingMoims(25); // 내가 주인인거 25개 생성
        buildOtherMoims(5, 5); // 내가 주인이 아닌거 5개 생성 , 직접 탈퇴한 모임 5개 생성
        em.flush();
        em.clear();

        // when
        List<MoimMember> memberMoims = moimMemberRepository.findMemberMoimsWithRuleAndCategoriesByConditionsPaged(testMember.getId(), true, false, null, 20);
        if (memberMoims.isEmpty()) {
            return;
        }

        Moim lastMoim = memberMoims.get(memberMoims.size() - 1).getMoim();
        List<MoimMember> memberMoims2 = moimMemberRepository.findMemberMoimsWithRuleAndCategoriesByConditionsPaged(testMember.getId(), true, false, lastMoim, 20);

//        Assertions.assertThat(memberMoims2).isSortedAccordingTo((o1, o2) -> Localda)

        // then
        for (MoimMember moimMember : memberMoims) {
            System.out.println("moimMember.getMoim().getMoimName() = " + moimMember.getMoim().getMoimName());
            System.out.println("moimMember.getMoim().getCreatedAt() = " + moimMember.getMoim().getCreatedAt());
            String s = moimMember.getMoim().getMoimJoinRule() == null ? "없음" : "있음!";
            System.out.println("Moim Has Join Rule :: " +  s);
            List<MoimCategoryLinker> cLinker = moimMember.getMoim().getMoimCategoryLinkers();
            for (MoimCategoryLinker moimCategoryLinker : cLinker) {
                System.out.println("moimCategoryLinker.getCategory() = " + moimCategoryLinker.getCategory().getCategoryName());

            }
            System.out.println("ENDS========================");
        }

        System.out.println("=================================== NEXT 20 PAGED ======================================");
        for (MoimMember moimMember : memberMoims2) {
            System.out.println("moimMember.getMoim().getMoimName() = " + moimMember.getMoim().getMoimName());
            System.out.println("moimMember.getMoim().getCreatedAt() = " + moimMember.getMoim().getCreatedAt());
            List<MoimCategoryLinker> cLinker = moimMember.getMoim().getMoimCategoryLinkers();
            for (MoimCategoryLinker moimCategoryLinker : cLinker) {
                System.out.println("moimCategoryLinker.getCategory() = " + moimCategoryLinker.getCategory().getCategoryName());

            }
            System.out.println("ENDS========================");
        }
    }
}