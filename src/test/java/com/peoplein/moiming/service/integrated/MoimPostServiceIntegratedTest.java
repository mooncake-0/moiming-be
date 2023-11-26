package com.peoplein.moiming.service.integrated;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.service.MoimPostService;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static com.peoplein.moiming.model.dto.request.MoimPostReqDto.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static com.peoplein.moiming.support.TestModelParams.moimArea;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class MoimPostServiceIntegratedTest extends TestObjectCreator {

    @Autowired
    private MoimPostService moimPostService;

    @Autowired
    private MoimPostRepository moimPostRepository;

    @Autowired
    private EntityManager em;

    private Member moimCreator;
    private Member moimMember;
    private Member notMoimMember;
    private Moim testMoim;


    @BeforeEach
    void be() {

        Role testRole = makeTestRole(RoleType.USER);
        moimCreator = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);
        moimMember = makeTestMember(memberEmail2, memberPhone2, memberName2, nickname2, ci2, testRole);
        notMoimMember = makeTestMember(memberEmail3, memberPhone3, memberName3, nickname3, ci3, testRole);
        em.persist(testRole);
        em.persist(moimCreator);
        em.persist(moimMember);
        em.persist(notMoimMember);


        // Moim Cateogry 저장
        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth1SampleCategory), 2, testCategory1);
        em.persist(testCategory1);
        em.persist(testCategory1_1);

        // Moim 준비
        testMoim = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), moimCreator);
        MoimMember.memberJoinMoim(moimMember, testMoim, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE);
        em.persist(testMoim);

        // 다른 모임 Post 도 만들어놔보자
        Moim testMoim2 = makeTestMoim(moimName2, maxMember2, moimArea2.getState(), moimArea2.getCity(), List.of(testCategory1, testCategory1_1), moimCreator);
        em.persist(testMoim2);
        em.persist(MoimPost.createMoimPost("제목", "내용", MoimPostCategory.GREETING, false, false, testMoim2, moimCreator));

        em.flush();
        em.clear();

    }


    // 1 - createMoimPostTest - value test
    @Test
    void createMoimPost_shouldCreateMoimPost_whenRightInfoPassed() {

        // given
        MoimPostCreateReqDto requestDto = new MoimPostCreateReqDto(testMoim.getId(), "제목입니다", "내용입니다", MoimPostCategory.GREETING, false, false);

        // when
        moimPostService.createMoimPost(requestDto, moimMember);
        em.flush();
        em.clear();

        // then - verify
        List<MoimPost> moimPosts = em.createQuery("select mp from MoimPost mp " +
                        "where mp.moim.id = :moim_id " +
                        "and mp.member.id = :member_id", MoimPost.class)
                .setParameter("moim_id", testMoim.getId())
                .setParameter("member_id", moimMember.getId())
                .getResultList();

        assertThat(moimPosts).isNotEmpty();
        MoimPost savedPost = moimPosts.get(0);
        assertThat(savedPost.getPostTitle()).isEqualTo("제목입니다");
        assertThat(savedPost.getPostContent()).isEqualTo("내용입니다");
        assertThat(savedPost.getMoimPostCategory()).isEqualTo(MoimPostCategory.GREETING);

    }


    // 2 - getMoimPosts - value test
    // Member 요청
    // - 10개 미만일때
    @Test
    void getMoimPosts_shouldReturnMoimPosts_whenLessThan10PostsAndMemberFirstRequestNoFilter() throws InterruptedException {

        // given
        makeMoimPosts(8, testMoim, moimCreator, em);

        // when
        List<MoimPost> moimPosts = moimPostService.getMoimPosts(testMoim.getId(), null, null, 10, moimMember);

        // then
        assertThat(moimPosts.size()).isEqualTo(8);

    }

    // Member 요청
    // - 후속이 줄게 없는데 온 요청 - 0개를 반환한다
    @Test
    void getMoimPosts_shouldReturnRightPostAndNoPosts_whenMemberRequestTwoTimesNoFilterAndNoMoreLeft() throws InterruptedException {

        // given
        makeMoimPosts(10, testMoim, moimCreator, em);

        // when
        List<MoimPost> firstReqResults = moimPostService.getMoimPosts(testMoim.getId(), null, null, 10, moimMember);
        int lastPostIndex = firstReqResults.size() == 0 ? 0 : firstReqResults.size() - 1; // 마지막 녀석을 가져온다
        Long lastPostId = firstReqResults.get(lastPostIndex).getId();
        List<MoimPost> secondReqResults = moimPostService.getMoimPosts(testMoim.getId(), lastPostId, null, 10, moimMember);

        // then
        assertThat(firstReqResults.size()).isEqualTo(10);
        assertThat(secondReqResults.size()).isEqualTo(0);

    }

    // Member 요청
    // - Category Filter Off - 2후속 요청 확인
    @Test
    void getMoimPosts_shouldReturnRightMoimPosts_whenMemberRequestTwoTimesNoFilter() throws InterruptedException {

        // given
        makeMoimPosts(30, testMoim, moimCreator, em);

        // when
        List<MoimPost> firstReqResults = moimPostService.getMoimPosts(testMoim.getId(), null, null, 10, moimMember);
        int lastPostIndex = firstReqResults.size() == 0 ? 0 : firstReqResults.size() - 1; // 마지막 녀석을 가져온다
        Long lastPostId = firstReqResults.get(lastPostIndex).getId();
        List<MoimPost> secondReqResults = moimPostService.getMoimPosts(testMoim.getId(), lastPostId, null, 10, moimMember);

        // then - db verify
        List<MoimPost> allMoimPosts = em.createQuery("select mp from MoimPost mp " +
                        "where mp.moim.id = :moim_id " +
                        "order by mp.createdAt desc, mp.id desc", MoimPost.class)
                .setParameter("moim_id", testMoim.getId())
                .getResultList();

        for (int i = 0; i < firstReqResults.size() + secondReqResults.size(); i++) {
            if (i < firstReqResults.size()) {
                assertThat(firstReqResults.get(i).getId()).isEqualTo(allMoimPosts.get(i).getId());
            } else {
                int tmp = i - firstReqResults.size();
                assertThat(secondReqResults.get(tmp).getId()).isEqualTo(allMoimPosts.get(i).getId());
            }
        }
    }


    // Member 요청
    // - Category Filter On - 2후속 요청 확인
    @Test
    void getMoimPosts_shouldReturnRightMoimPosts_whenMemberRequestTwoTimesCategoryFilter() throws InterruptedException {

        // given
        makeMoimPosts(30, testMoim, moimCreator, em);
        MoimPostCategory category = MoimPostCategory.GREETING;

        // when
        List<MoimPost> firstReqResults = moimPostService.getMoimPosts(testMoim.getId(), null, category, 10, moimMember);
        int lastPostIndex = firstReqResults.size() == 0 ? 0 : firstReqResults.size() - 1; // 마지막 녀석을 가져온다
        Long lastPostId = firstReqResults.get(lastPostIndex).getId();
        List<MoimPost> secondReqResults = moimPostService.getMoimPosts(testMoim.getId(), lastPostId, category, 10, moimMember);

        // then - db verify
        List<MoimPost> allMoimPosts = em.createQuery("select mp from MoimPost mp " +
                        "where mp.moim.id = :moim_id " +
                        "and mp.moimPostCategory = :category " +
                        "order by mp.createdAt desc, mp.id desc", MoimPost.class)
                .setParameter("moim_id", testMoim.getId())
                .setParameter("category", category)
                .getResultList();

        System.out.println("allMoimPosts.size() = " + allMoimPosts.size());
        System.out.println("firstReqResults.size() = " + firstReqResults.size());
        System.out.println("secondReqResults.size() = " + secondReqResults.size());

        for (int i = 0; i < firstReqResults.size() + secondReqResults.size(); i++) {
            if (i < firstReqResults.size()) {
                assertThat(firstReqResults.get(i).getMoimPostCategory()).isEqualTo(MoimPostCategory.GREETING);
                assertThat(firstReqResults.get(i).getId()).isEqualTo(allMoimPosts.get(i).getId());
            } else {
                int tmp = i - firstReqResults.size();
                assertThat(secondReqResults.get(tmp).getMoimPostCategory()).isEqualTo(MoimPostCategory.GREETING);
                assertThat(secondReqResults.get(tmp).getId()).isEqualTo(allMoimPosts.get(i).getId());
            }
        }
    }


    // 비 Member 요청
    // - 10개 미만일때
    @Test
    void getMoimPosts_shouldReturnMoimPosts_whenLessThan10PostsAndNotMemberFirstRequestNoFilter() throws InterruptedException {

        // given
        makeMoimPosts(8, testMoim, moimCreator, em);

        // when
        List<MoimPost> moimPosts = moimPostService.getMoimPosts(testMoim.getId(), null, null, 10, notMoimMember);
        System.out.println("moimPosts.size() = " + moimPosts.size());

        // then
        assertThat(moimPosts.size()).isLessThanOrEqualTo(8); // 별게 다있네
        for (MoimPost moimPost : moimPosts) {
            assertThat(moimPost.isHasPrivateVisibility()).isEqualTo(false);
        }


    }


    // 비 Member 요청
    // - Category Filter Off - 2후속 요청 확인
    @Test
    void getMoimPosts_shouldReturnRightMoimPosts_whenNotMemberRequestTwoTimesNoFilter() throws InterruptedException {

        // given
        makeMoimPosts(30, testMoim, moimCreator, em);

        // when
        List<MoimPost> firstReqResults = moimPostService.getMoimPosts(testMoim.getId(), null, null, 10, notMoimMember);
        int lastPostIndex = firstReqResults.size() == 0 ? 0 : firstReqResults.size() - 1; // 마지막 녀석을 가져온다
        Long lastPostId = firstReqResults.get(lastPostIndex).getId();
        List<MoimPost> secondReqResults = moimPostService.getMoimPosts(testMoim.getId(), lastPostId, null, 10, notMoimMember);

        // then - db verify
        List<MoimPost> allMoimPosts = em.createQuery("select mp from MoimPost mp " +
                        "where mp.moim.id = :moim_id " +
                        "and mp.hasPrivateVisibility = :visibility " +
                        "order by mp.createdAt desc, mp.id desc", MoimPost.class)
                .setParameter("visibility", false) // 공개인애들만 가져온다
                .setParameter("moim_id", testMoim.getId())
                .getResultList();


        for (int i = 0; i < firstReqResults.size() + secondReqResults.size(); i++) {
            if (i < firstReqResults.size()) {
                assertThat(firstReqResults.get(i).isHasPrivateVisibility()).isEqualTo(false);
                assertThat(firstReqResults.get(i).getId()).isEqualTo(allMoimPosts.get(i).getId());
            } else {
                int tmp = i - firstReqResults.size();
                assertThat(secondReqResults.get(tmp).isHasPrivateVisibility()).isEqualTo(false);
                assertThat(secondReqResults.get(tmp).getId()).isEqualTo(allMoimPosts.get(i).getId());
            }
        }
    }


    // 비 Member 요청
    // - Category Filter On - 2후속 요청 확인
    @Test
    void getMoimPosts_shouldReturnRightMoimPosts_whenNotMemberRequestTwoTimesCategoryFilter() throws InterruptedException {

        // given
        makeMoimPosts(30, testMoim, moimCreator, em);
        MoimPostCategory category = MoimPostCategory.GREETING;

        // when
        List<MoimPost> firstReqResults = moimPostService.getMoimPosts(testMoim.getId(), null, category, 10, notMoimMember);
        int lastPostIndex = firstReqResults.size() == 0 ? 0 : firstReqResults.size() - 1; // 마지막 녀석을 가져온다
        Long lastPostId = firstReqResults.get(lastPostIndex).getId();
        List<MoimPost> secondReqResults = moimPostService.getMoimPosts(testMoim.getId(), lastPostId, category, 10, notMoimMember);

        // then - db verify
        List<MoimPost> allMoimPosts = em.createQuery("select mp from MoimPost mp " +
                        "where mp.moim.id = :moim_id " +
                        "and mp.moimPostCategory = :category " +
                        "and mp.hasPrivateVisibility = :visibility " +
                        "order by mp.createdAt desc, mp.id desc", MoimPost.class)
                .setParameter("moim_id", testMoim.getId())
                .setParameter("category", category)
                .setParameter("visibility", false) // 공개인 Post 만 가져온다
                .getResultList();

        System.out.println("allMoimPosts.size() = " + allMoimPosts.size());
        System.out.println("firstReqResults.size() = " + firstReqResults.size());
        System.out.println("secondReqResults.size() = " + secondReqResults.size());

        for (int i = 0; i < firstReqResults.size() + secondReqResults.size(); i++) {
            if (i < firstReqResults.size()) {
                assertThat(firstReqResults.get(i).isHasPrivateVisibility()).isEqualTo(false);
                assertThat(firstReqResults.get(i).getMoimPostCategory()).isEqualTo(MoimPostCategory.GREETING);
                assertThat(firstReqResults.get(i).getId()).isEqualTo(allMoimPosts.get(i).getId());
            } else {
                int tmp = i - firstReqResults.size();
                assertThat(secondReqResults.get(tmp).isHasPrivateVisibility()).isEqualTo(false);
                assertThat(secondReqResults.get(tmp).getMoimPostCategory()).isEqualTo(MoimPostCategory.GREETING);
                assertThat(secondReqResults.get(tmp).getId()).isEqualTo(allMoimPosts.get(i).getId());
            }
        }
    }
}