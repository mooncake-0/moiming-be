package com.peoplein.moiming.service.integrated;

import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.service.MoimPostService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
public class MoimPostServiceIntegratedTest {

    @Autowired
    private MoimPostService moimPostService;

    @Autowired
    private MoimPostRepository moimPostRepository;

    @Autowired
    private EntityManager entityManager;


    @BeforeEach
    void be() {

    }


    // 1 - createMoimPostTest - value test


    // 2 - getMoimPosts - value test
    // Member 요청
    // - Category Filter Off - 2후속 요청 확인
    // - Category Filter On - 2후속 요청 확인

    // 비Member 요청
    // - Category Filter Off - 2후속 요청 확인
    // - Category Filter On - 2후속 요청 확인

    // MoimPost 를 다른 Moim 것도 좀 넣어보기
}
