package com.peoplein.moiming.repository.jpa;

import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.support.RepositoryTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;

@Import({RepositoryTestConfiguration.class, MoimPostJpaRepository.class})
@ActiveProfiles("test")
@DataJpaTest
public class MoimPostJpaRepositoryTest {

    @Autowired
    private MoimPostRepository moimPostRepository;

    @Autowired
    private EntityManager em;


    @BeforeEach
    void be() {

        // MoimPost 를 약 30개 저장해보면 좋겠나..?


    }

    @Test
    void findByCategoryAndLastPostOrderByDateDesc_should_when() {

        // given

        // when

        // then

    }



}
