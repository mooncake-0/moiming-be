package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.support.RepositoryTestConfiguration;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;

@Import({RepositoryTestConfiguration.class, MoimPostJpaRepository.class})
@ActiveProfiles("test")
@DataJpaTest
public class MoimPostJpaRepositoryTest extends TestObjectCreator {

    @Autowired
    private MoimPostRepository moimPostRepository;

    @Autowired
    private EntityManager em;

    private Member moimCreator;
    private Moim testMoim;

    @BeforeEach
    void ba() throws InterruptedException {

        Role testRole = makeTestRole(RoleType.USER);
        moimCreator = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);
        em.persist(testRole);
        em.persist(moimCreator);

        // Moim Cateogry 저장
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth1SampleCategory), 2, testCategory1);
        em.persist(testCategory1);
        em.persist(testCategory1_1);

        // Moim 준비
        testMoim = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), moimCreator);
        em.persist(testMoim);

        makeMoimPosts();

        em.flush();
        em.clear();

    }


    // 1. 모임원 유저의 요청
    // Category Filter Off 첫 요청
    @Test
    void findByCategoryAndLastPostOrderByDateDesc_shouldReturnPosts_whenMoimMemberFirstRequestWithoutCategory() {

        // given
        boolean moimMemberRequest = true;
        MoimPostCategory category = null;
        MoimPost lastPost = null;

        // when
        List<MoimPost> moimPosts = moimPostRepository.findByCategoryAndLastPostOrderByDateDesc(testMoim.getId(), lastPost, category, 10, moimMemberRequest);

        // then - limit 를 제외한 동일 쿼리로 모두 가져온다
        List<MoimPost> rawMoimPosts = em.createQuery("select mp from MoimPost mp " +
                        "where mp.moim.id = :id " +
                        "order by mp.createdAt desc, mp.id desc", MoimPost.class)
                .setParameter("id", testMoim.getId())
                .getResultList();

        // then - 원했던 List 가 모두 정확하게 반환되었다
        for (int i = 0; i < moimPosts.size(); i++) {
            assertThat(moimPosts.get(i).getId()).isEqualTo(rawMoimPosts.get(i).getId());
        }
    }


    // 1. 모임원 유저의 요청
    // Category Filter Off 후속 요청
    @Test
    void findByCategoryAndLastPostOrderByDateDesc_shouldReturnPosts_whenMoimMemberNextRequestWithoutCategory() {

        // given
        boolean moimMemberRequest = true;
        MoimPostCategory category = null;
        MoimPost lastPost = null;

        // when
        List<MoimPost> firstReq = moimPostRepository.findByCategoryAndLastPostOrderByDateDesc(testMoim.getId(), lastPost, category, 10, moimMemberRequest);
        lastPost = firstReq.get(firstReq.size() - 1); // 마지막 녀석을 가져온다
        List<MoimPost> nextReq = moimPostRepository.findByCategoryAndLastPostOrderByDateDesc(testMoim.getId(), lastPost, category, 10, moimMemberRequest);

        // then - limit 를 제외한 동일 쿼리로 모두 가져온다
        List<MoimPost> rawMoimPosts = em.createQuery("select mp from MoimPost mp " +
                        "where mp.moim.id = :id " +
                        "order by mp.createdAt desc, mp.id desc", MoimPost.class)
                .setParameter("id", testMoim.getId())
                .getResultList();

        // then - 원했던 List 가 모두 정확하게 반환되었다
        for (int i = 0; i < firstReq.size() + nextReq.size(); i++) {
            if (i < firstReq.size()) {
                assertThat(firstReq.get(i).getId()).isEqualTo(rawMoimPosts.get(i).getId());
            } else {
                int tmp = i - firstReq.size();
                assertThat(nextReq.get(tmp).getId()).isEqualTo(rawMoimPosts.get(i).getId());
            }
        }
    }


    // 1. 모임원 유저의 요청
    // Category Filter On 첫 요청
    @Test
    void findByCategoryAndLastPostOrderByDateDesc_shouldReturnPosts_whenMoimMemberFirstRequestWithCategory() {

        // given
        boolean moimMemberRequest = true;
        MoimPostCategory category = MoimPostCategory.GREETING;
        MoimPost lastPost = null;

        // when
        List<MoimPost> moimPosts = moimPostRepository.findByCategoryAndLastPostOrderByDateDesc(testMoim.getId(), lastPost, category, 10, moimMemberRequest);

        // then - limit 를 제외한 동일 쿼리로 모두 가져온다
        List<MoimPost> rawMoimPosts = em.createQuery("select mp from MoimPost mp " +
                        "where mp.moim.id = :id " +
                        "and mp.moimPostCategory = :category " +
                        "order by mp.createdAt desc, mp.id desc", MoimPost.class)
                .setParameter("id", testMoim.getId())
                .setParameter("category", category)
                .getResultList();

        // then - 원했던 List 가 모두 정확하게 반환되었다
        for (int i = 0; i < moimPosts.size(); i++) {
            assertThat(moimPosts.get(i).getId()).isEqualTo(rawMoimPosts.get(i).getId());
        }
    }


    // 1. 모임원 유저의 요청
    // Category Filter On 후속 요청
    @Test
    void findByCategoryAndLastPostOrderByDateDesc_shouldReturnPosts_whenMoimMemberNextRequestWithCategory() {

        // given
        boolean moimMemberRequest = true;
        MoimPostCategory category = MoimPostCategory.GREETING;
        MoimPost lastPost = null;

        // when
        List<MoimPost> firstReq = moimPostRepository.findByCategoryAndLastPostOrderByDateDesc(testMoim.getId(), lastPost, category, 10, moimMemberRequest);
        lastPost = firstReq.get(firstReq.size() - 1); // 마지막 녀석을 가져온다
        List<MoimPost> nextReq = moimPostRepository.findByCategoryAndLastPostOrderByDateDesc(testMoim.getId(), lastPost, category, 10, moimMemberRequest);

        // then - limit 를 제외한 동일 쿼리로 모두 가져온다
        List<MoimPost> rawMoimPosts = em.createQuery("select mp from MoimPost mp " +
                        "where mp.moim.id = :id " +
                        "and mp.moimPostCategory = :category " +
                        "order by mp.createdAt desc, mp.id desc", MoimPost.class)
                .setParameter("id", testMoim.getId())
                .setParameter("category", category)
                .getResultList();

        // then - 원했던 List 가 모두 정확하게 반환되었다
        for (int i = 0; i < firstReq.size() + nextReq.size(); i++) {
            if (i < firstReq.size()) {
                assertThat(firstReq.get(i).getMoimPostCategory()).isEqualTo(MoimPostCategory.GREETING);
                assertThat(firstReq.get(i).getId()).isEqualTo(rawMoimPosts.get(i).getId());
            } else {
                int tmp = i - firstReq.size();
                assertThat(firstReq.get(tmp).getMoimPostCategory()).isEqualTo(MoimPostCategory.GREETING);
                assertThat(nextReq.get(tmp).getId()).isEqualTo(rawMoimPosts.get(i).getId());
            }
        }
    }


    // 2. 비모임원 유저의 요청 - 동일
    // Category Filter Off 후속 요청
    @Test
    void findByCategoryAndLastPostOrderByDateDesc_shouldReturnPosts_whenNotMemberNextRequestWithoutCategory() {

        // given
        boolean moimMemberRequest = false;
        MoimPostCategory category = null;
        MoimPost lastPost = null;

        // when
        List<MoimPost> firstReq = moimPostRepository.findByCategoryAndLastPostOrderByDateDesc(testMoim.getId(), lastPost, category, 10, moimMemberRequest);
        lastPost = firstReq.get(firstReq.size() - 1); // 마지막 녀석을 가져온다
        List<MoimPost> nextReq = moimPostRepository.findByCategoryAndLastPostOrderByDateDesc(testMoim.getId(), lastPost, category, 10, moimMemberRequest);

        // then - limit 를 제외한 동일 쿼리로 모두 가져온다
        List<MoimPost> rawMoimPosts = em.createQuery("select mp from MoimPost mp " +
                        "where mp.moim.id = :id " +
                        "and mp.hasPrivateVisibility = :visibility " +
                        "order by mp.createdAt desc, mp.id desc", MoimPost.class)
                .setParameter("id", testMoim.getId())
                .setParameter("visibility", moimMemberRequest)
                .getResultList();

        // then - 원했던 List 가 모두 정확하게 반환되었다
        for (int i = 0; i < firstReq.size() + nextReq.size(); i++) {
            if (i < firstReq.size()) {
                assertThat(firstReq.get(i).isHasPrivateVisibility()).isEqualTo(false);
                assertThat(firstReq.get(i).getId()).isEqualTo(rawMoimPosts.get(i).getId());
            } else {
                int tmp = i - firstReq.size();
                assertThat(firstReq.get(tmp).isHasPrivateVisibility()).isEqualTo(false);
                assertThat(nextReq.get(tmp).getId()).isEqualTo(rawMoimPosts.get(i).getId());
            }
        }
    }


    // 2. 비모임원 유저의 요청 - 동일
    // Category Filter On 후속 요청
    @Test
    void findByCategoryAndLastPostOrderByDateDesc_shouldReturnPosts_whenNotMemberNextRequestWithCategory() {

        // given
        boolean moimMemberRequest = false;
        MoimPostCategory category = MoimPostCategory.GREETING;
        MoimPost lastPost = null;

        // when
        List<MoimPost> firstReq = moimPostRepository.findByCategoryAndLastPostOrderByDateDesc(testMoim.getId(), lastPost, category, 10, moimMemberRequest);
        lastPost = firstReq.get(firstReq.size() - 1); // 마지막 녀석을 가져온다
        List<MoimPost> nextReq = moimPostRepository.findByCategoryAndLastPostOrderByDateDesc(testMoim.getId(), lastPost, category, 10, moimMemberRequest);

        // then - limit 를 제외한 동일 쿼리로 모두 가져온다
        List<MoimPost> rawMoimPosts = em.createQuery("select mp from MoimPost mp " +
                        "where mp.moim.id = :id " +
                        "and mp.hasPrivateVisibility = :visibility " + // private visibility 가 없는 것들만 가져와야 한다
                        "and mp.moimPostCategory = :category " +
                        "order by mp.createdAt desc, mp.id desc", MoimPost.class)
                .setParameter("id", testMoim.getId())
                .setParameter("visibility", moimMemberRequest)
                .setParameter("category", category)
                .getResultList();


        // then - 원했던 List 가 모두 정확하게 반환되었다
        for (int i = 0; i < firstReq.size() + nextReq.size(); i++) {
            if (i < firstReq.size()) {
                assertThat(firstReq.get(i).isHasPrivateVisibility()).isEqualTo(false);
                assertThat(firstReq.get(i).getMoimPostCategory()).isEqualTo(MoimPostCategory.GREETING);
                assertThat(firstReq.get(i).getId()).isEqualTo(rawMoimPosts.get(i).getId());
            } else {
                int tmp = i - firstReq.size();
                assertThat(firstReq.get(tmp).isHasPrivateVisibility()).isEqualTo(false);
                assertThat(firstReq.get(tmp).getMoimPostCategory()).isEqualTo(MoimPostCategory.GREETING);
                assertThat(nextReq.get(tmp).getId()).isEqualTo(rawMoimPosts.get(i).getId());
            }
        }
    }


    // Created At RANDOM 하게 바꿨을 때도 통과하는거 확인 완
    // RANDOM 한 Info 들로 저장한다
    private void makeMoimPosts() throws InterruptedException {

        MoimPostCategory category = null;
        boolean hasPrivateVisibility = false;

        Random random = new Random();

        for (int i = 0; i < 40; i++) {

            int categoryRandom = random.nextInt(4);
            int visibilityRandom = random.nextInt(2);

            if (categoryRandom % 4 == 0) category = MoimPostCategory.NOTICE;
            if (categoryRandom % 4 == 1) category = MoimPostCategory.GREETING;
            if (categoryRandom % 4 == 2) category = MoimPostCategory.REVIEW;
            if (categoryRandom % 4 == 3) category = MoimPostCategory.EXTRA;
            if (visibilityRandom % 2 == 0) hasPrivateVisibility = true;
            if (visibilityRandom % 2 == 1) hasPrivateVisibility = false;

            String title = "제목" + visibilityRandom + i + categoryRandom;
            MoimPost post = MoimPost.createMoimPost(title, "내용", category, hasPrivateVisibility, false, testMoim, moimCreator);
            if (i % 3 == 1) {
                Thread.sleep(100);
            }
            em.persist(post);
        }

    }

}