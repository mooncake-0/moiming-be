package com.peoplein.moiming;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.support.TestModelParams;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.support.TestModelParams.*;

@Transactional
@SpringBootTest
@Rollback(value = false)
public class Test extends TestObjectCreator {

    @Autowired
    private EntityManager em;

    @Autowired
    private MoimPostRepository moimPostRepository;

    private MoimPost testMoimPost;

    @BeforeEach
    void be() {

        Role testRole = makeTestRole(RoleType.USER);
        Member testMember = makeTestMember(memberEmail, memberPhone, memberName, nickname, ci, testRole);
        em.persist(testRole);
        em.persist(testMember);

        Category testCategory1 = new Category(1L, CategoryName.fromValue(depth1SampleCategory), 1, null);
        Category testCategory1_1 = new Category(2L, CategoryName.fromValue(depth1SampleCategory), 2, testCategory1);
        em.persist(testCategory1);
        em.persist(testCategory1_1);

        Moim moim = makeTestMoim(moimName, maxMember, moimArea.getState(), moimArea.getCity(), List.of(testCategory1, testCategory1_1), testMember);
        em.persist(moim);

        // Post 존재
        testMoimPost = makeMoimPost(moim, testMember, MoimPostCategory.NOTICE, false);
        em.persist(testMoimPost);

        em.flush();
        em.clear();

    }

    @org.junit.jupiter.api.Test
    void testA() {

        Optional<MoimPost> getMoimPost = moimPostRepository.findById(testMoimPost.getId());
        MoimPost found = getMoimPost.get();

        System.out.println("found.getId() = " + found.getId());
        System.out.println("=============");

        System.out.println("found.getMoim().getId() = " + found.getMoim().getId());
        System.out.println("=============");

        System.out.println("found.getMoim().getMoimName() = " + found.getMoim().getMoimName());

    }

}
