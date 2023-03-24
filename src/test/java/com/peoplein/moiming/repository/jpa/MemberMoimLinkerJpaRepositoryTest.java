package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.BaseTest;
import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MemberMoimLinker;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.repository.MemberMoimLinkerRepository;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberMoimLinkerJpaRepositoryTest extends BaseTest {


    @Autowired
    MemberMoimLinkerRepository moimLinkerRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MoimRepository moimRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    EntityManager em;

    @Autowired
    JdbcTemplate jdbcTemplate;

    Member member;
    Moim moim;
    MemberMoimLinker memberMoimLinker;

    @BeforeEach
    void initInstance() {
        TestUtils.truncateAllTable(jdbcTemplate);
        member = TestUtils.initMemberAndMemberInfo();
        moim = TestUtils.initMoimAndRuleJoin();
        memberMoimLinker = MemberMoimLinker.memberJoinMoim(
                member,
                moim,
                MoimRoleType.LEADER,
                MoimMemberState.ACTIVE);
    }


    @Test
    void saveTest() {

        Long memberId = memberRepository.save(member);
        Long moimId = moimRepository.save(moim);
        Long roleId = roleRepository.save(member.getRoles().get(0).getRole());

        Long savedId = moimLinkerRepository.save(memberMoimLinker);

        assertThat(savedId).isEqualTo(memberMoimLinker.getId());
    }

    @Test
    void findByMemberIdTest() {

        Long memberId = memberRepository.save(member);
        Long moimId = moimRepository.save(moim);
        Long roleId = roleRepository.save(member.getRoles().get(0).getRole());
        Long linkerId = moimLinkerRepository.save(memberMoimLinker);
        em.flush();
        em.clear();

        List<MemberMoimLinker> findLinker = moimLinkerRepository.findByMemberId(memberId);

        assertThat(findLinker.size()).isEqualTo(1);
        assertThat(findLinker.get(0).getId()).isEqualTo(linkerId);
        assertThat(findLinker.get(0).getMember().getId()).isEqualTo(memberId);
    }

    @Test
    void findByMemberIdNotExistedTest() {

        Long memberId = 98765433210L;
        em.flush();
        em.clear();

        List<MemberMoimLinker> findLinker = moimLinkerRepository.findByMemberId(memberId);

        assertThat(findLinker.size()).isEqualTo(0);
    }

    @Test
    void findWithMoimByMemberIdTest() {

        Long memberId = memberRepository.save(member);
        Long moimId = moimRepository.save(moim);
        Long roleId = roleRepository.save(member.getRoles().get(0).getRole());
        Long linkerId = moimLinkerRepository.save(memberMoimLinker);
        em.flush();
        em.clear();

        List<MemberMoimLinker> findLinker = moimLinkerRepository.findWithMoimByMemberId(memberId);

        assertThat(findLinker.size()).isEqualTo(1);
        assertThat(findLinker.get(0).getId()).isEqualTo(linkerId);
        assertThat(findLinker.get(0).getMember().getId()).isEqualTo(memberId);
    }

    @Test
    void findWithMoimByMemberIdNotExistedTest() {

        Long memberId = 98765433210L;
        em.flush();
        em.clear();

        List<MemberMoimLinker> findLinker = moimLinkerRepository.findWithMoimByMemberId(memberId);

        assertThat(findLinker.size()).isEqualTo(0);
    }

    @Test
    void findByMemberAndMoimIdTest() {

        Long memberId = memberRepository.save(member);
        Long moimId = moimRepository.save(moim);
        Long roleId = roleRepository.save(member.getRoles().get(0).getRole());
        Long linkerId = moimLinkerRepository.save(memberMoimLinker);
        em.flush();
        em.clear();

        MemberMoimLinker findLinker = moimLinkerRepository.findByMemberAndMoimId(memberId, moimId);

        assertThat(findLinker.getId()).isEqualTo(linkerId);
        assertThat(findLinker.getMember().getId()).isEqualTo(memberId);
        assertThat(findLinker.getMoim().getId()).isEqualTo(moimId);
    }

    @Test
    void findByMemberAndMoimIdNotExistedTest() {

        Long memberId = memberRepository.save(member);
        Long moimId = moimRepository.save(moim);
        Long roleId = roleRepository.save(member.getRoles().get(0).getRole());
        Long linkerId = moimLinkerRepository.save(memberMoimLinker);
        em.flush();
        em.clear();

        MemberMoimLinker findLinker1 = moimLinkerRepository.findByMemberAndMoimId(9876543210L, moimId);
        MemberMoimLinker findLinker2 = moimLinkerRepository.findByMemberAndMoimId(memberId, 9876543210L);

        assertThat(findLinker1).isNull();
        assertThat(findLinker2).isNull();

    }


    @Test
    void findWithMoimByMemberAndMoimIdTest() {

        Long memberId = memberRepository.save(member);
        Long moimId = moimRepository.save(moim);
        Long roleId = roleRepository.save(member.getRoles().get(0).getRole());
        Long linkerId = moimLinkerRepository.save(memberMoimLinker);
        em.flush();
        em.clear();

        MemberMoimLinker findLinker = moimLinkerRepository.findWithMoimByMemberAndMoimId(memberId, moimId);

        assertThat(findLinker.getId()).isEqualTo(linkerId);
        assertThat(findLinker.getMember().getId()).isEqualTo(memberId);
        assertThat(findLinker.getMoim().getId()).isEqualTo(moimId);
    }

    @Test
    void findWithMoimByMemberAndMoimIdNotExistedTest() {

        Long memberId = memberRepository.save(member);
        Long moimId = moimRepository.save(moim);
        Long roleId = roleRepository.save(member.getRoles().get(0).getRole());
        Long linkerId = moimLinkerRepository.save(memberMoimLinker);
        em.flush();
        em.clear();

        MemberMoimLinker findLinker1 = moimLinkerRepository.findWithMoimByMemberAndMoimId(9876543210L, moimId);
        MemberMoimLinker findLinker2 = moimLinkerRepository.findWithMoimByMemberAndMoimId(memberId, 9876543210L);

        assertThat(findLinker1).isNull();
        assertThat(findLinker2).isNull();

    }


    @Test
    void findWithMemberInfoByMemberAndMoimIdTest() {

        Long memberId = memberRepository.save(member);
        Long moimId = moimRepository.save(moim);
        Long roleId = roleRepository.save(member.getRoles().get(0).getRole());
        Long linkerId = moimLinkerRepository.save(memberMoimLinker);
        em.flush();
        em.clear();

        MemberMoimLinker findLinker = moimLinkerRepository.findWithMemberInfoByMemberAndMoimId(memberId, moimId);

        assertThat(findLinker.getId()).isEqualTo(linkerId);
        assertThat(findLinker.getMember().getId()).isEqualTo(memberId);
        assertThat(findLinker.getMoim().getId()).isEqualTo(moimId);
    }

    @Test
    void findWithMemberInfoByMemberAndMoimIdNotExistedTest() {

        Long memberId = memberRepository.save(member);
        Long moimId = moimRepository.save(moim);
        Long roleId = roleRepository.save(member.getRoles().get(0).getRole());
        Long linkerId = moimLinkerRepository.save(memberMoimLinker);
        em.flush();
        em.clear();

        MemberMoimLinker findLinker1 = moimLinkerRepository.findWithMemberInfoByMemberAndMoimId(9876543210L, moimId);
        MemberMoimLinker findLinker2 = moimLinkerRepository.findWithMemberInfoByMemberAndMoimId(memberId, 9876543210L);

        assertThat(findLinker1).isNull();
        assertThat(findLinker2).isNull();

    }

/*


    MemberMoimLinker findWithMemberInfoAndMoimByMemberAndMoimId(Long memberId, Long moimId);
    List<MemberMoimLinker> findWithMemberInfoAndMoimByMoimId(Long moimId);
    List<MemberMoimLinker> findByMoimIdAndMemberIds(Long moimId, List<Long> memberIds);
    void remove(MemberMoimLinker memberMoimLinker);

 */

}
