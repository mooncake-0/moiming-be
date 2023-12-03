package com.peoplein.moiming;


import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.enums.RoleType;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.service.PostCommentService;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;
import java.util.Random;

import static com.peoplein.moiming.support.TestModelParams.*;

@SpringBootTest
@Transactional
public class BigTest extends TestObjectCreator {

    @Autowired
    private EntityManager em;


    @Autowired
    private PostCommentService service;



    @Test
    void helloV2_2() {

        Member testMember = em.find(Member.class, 1L);
        long startTime2 = System.currentTimeMillis();
        service.deleteCommentV2(526L, testMember);
        long elapsed2 = System.currentTimeMillis() - startTime2;
        System.out.println("V2 수행시간: " + elapsed2 + " 밀리초");

        em.flush();
        em.clear();

    }


    @Test
    void helloV1_3() {

        Member testMember = em.find(Member.class, 1L);
        long startTime3 = System.currentTimeMillis();
        service.deleteCommentV3(526L, testMember);
        long elapsed3 = System.currentTimeMillis() - startTime3;
        System.out.println("V3 수행시간: " + elapsed3 + " 밀리초");

        em.flush();
        em.clear();

    }

    @Test
    void helloV3_1() {

        Member testMember = em.find(Member.class, 1L);

        long startTime = System.currentTimeMillis();
        service.deleteCommentV1(526L, 66666L, 1L, testMember);
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("V1 수행시간: " + elapsed + " 밀리초");

        em.flush();
        em.clear();
    }
}