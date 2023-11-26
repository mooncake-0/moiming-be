package com.peoplein.moiming.service.integrated;


import com.peoplein.moiming.repository.PostCommentRepository;
import com.peoplein.moiming.service.PostCommentService;
import com.peoplein.moiming.support.TestObjectCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

// Service 예외 상황은 단위 테스트에서 다 진행
// 성공시 DB Tx Test
@SpringBootTest
@Transactional
public class PostCommentServiceIntegratedTest extends TestObjectCreator {

    @Autowired
    private PostCommentService postCommentService;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private EntityManager em;



}
