package com.peoplein.moiming;

import com.peoplein.moiming.domain.*;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.fixed.QuestionChoice;
import com.peoplein.moiming.domain.fixed.ReviewQuestion;
import com.peoplein.moiming.domain.fixed.Role;
import com.peoplein.moiming.domain.rules.MoimRule;
import com.peoplein.moiming.domain.rules.RuleJoin;
import com.peoplein.moiming.domain.rules.RulePersist;
import com.peoplein.moiming.repository.CategoryRepository;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.repository.MoimReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 Dev INIT Database 를 위한 초기 세팅
 서버 띄울시에 초기화 작업 필요
 */

@Component // 초기 데이터 세팅용 Bean
@RequiredArgsConstructor
@Slf4j
@Transactional
@Profile(value = "production")
public class InitDatabase {

    private final InitDatabaseQuery initDatabaseQuery;
    private Long moim1Id;
    private Long moimPostId;

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void initUserRole() {
        initDatabaseQuery.initUserRole();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(2)
    public void initMemberWithAdminGrant() {
        initDatabaseQuery.initMemberWithAdminGrant();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(3)
    public void initMemberWithUserGrant() {
        initDatabaseQuery.initMemberWithUserGrant();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(4)
    public void initMoimCategory() {
        initDatabaseQuery.initMoimCategory();
    }


    @EventListener(ApplicationReadyEvent.class)
    @Order(5)
    public void initMoimEntity() {
        initDatabaseQuery.initMoimEntity();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(6)
    public void initMoimEntity2() {
        initDatabaseQuery.initMoimEntity2();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(7)
    public void joinMoim1OfMember2() {
        initDatabaseQuery.joinMoim1OfMember2();
    }


    @EventListener(ApplicationReadyEvent.class)
    @Order(8)
    public void initPostByMember1() {
        initDatabaseQuery.initPostByMember1();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(9)
    public void initPostByMember2() {
        initDatabaseQuery.initPostByMember2();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(10)
    public void initPostComment() {
        initDatabaseQuery.initPostComment();
    }


    @EventListener(ApplicationReadyEvent.class)
    @Order(11)
    public void initSchedule1InMoim1() {
        initDatabaseQuery.initSchedule1InMoim1();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(12)
    public void initSchedule2InMoim1() {
        initDatabaseQuery.initSchedule2InMoim1();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(13)
    public void initSchedule3InMoim1() {
        initDatabaseQuery.initSchedule3InMoim1();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(14)
    public void initReviewQuestionsAndChoices() {
        initDatabaseQuery.initReviewQuestionsAndChoices();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(15)
    public void initReviewAnswers() {
        initDatabaseQuery.initReviewAnswers();
    }
}

