package com.peoplein.moiming.service;

import com.peoplein.moiming.TestUtils;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.repository.MoimRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest
class SearchServiceTest {

    SearchService searchService;

    Area area;
    Category category;

    @BeforeEach
    void setUp() {
        MoimRepository moimRepository = Mockito.mock(MoimRepository.class);
        searchService = new SearchService(moimRepository);

        area = TestUtils.createAreaForTest();
        category = TestUtils.createCategoryForTest();
    }


    @Test
    @DisplayName("searchService.searchMoimTest")
    void 자음아니면_에러발생하지_않음() {
        // Given:
        String keywords = "가나다";

        // When + THEN:
        assertThatCode(() -> searchService.searchMoim(keywords, area, category))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("searchService.searchMoimTest")
    void 자음_에러_발생1() {
        // Given:
        String keywords = "아아ㄱㄴㄷ아아";

        // When + THEN:
        assertThatThrownBy(() -> searchService.searchMoim(keywords, area, category))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("searchService.searchMoimTest")
    void 자음_에러_발생2() {
        // Given:
        String keywords = "아아아아 아아ㄱ아아";

        // When + THEN:
        assertThatThrownBy(() -> searchService.searchMoim(keywords, area, category))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("searchService.searchMoimTest")
    void 자음_에러_발생3() {
        // Given:
        String keywords = "ㄱ";

        // When + THEN:
        assertThatThrownBy(() -> searchService.searchMoim(keywords, area, category))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("searchService.searchMoimTest")
    void 자음_에러_발생4() {
        // Given:
        String keywords = "ㄱ ㄴ 가나다라";

        // When + THEN:
        assertThatThrownBy(() -> searchService.searchMoim(keywords, area, category))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("searchService.searchMoimTest")
    void 자음_에러_발생5() {
        // Given:
        String keywords = "가나다라 ㄱ ㄴ 가나다라";

        // When + THEN:
        assertThatThrownBy(() -> searchService.searchMoim(keywords, area, category))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("searchService.searchMoimTest")
    void 자음_에러_발생6() {
        // Given:
        String keywords = "ㄱ가나다라 가나다라";

        // When + THEN:
        assertThatThrownBy(() -> searchService.searchMoim(keywords, area, category))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("searchService.searchMoimTest")
    void 자음_에러_발생7() {
        // Given:
        String keywords = "가나다라ㄱ 가나다라";

        // When + THEN:
        assertThatThrownBy(() -> searchService.searchMoim(keywords, area, category))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("searchService.searchMoimTest")
    void 공백_에러_발생7() {
        // Given:
        String keywords1 = "";
        String keywords2 = "";

        // When + THEN:
        assertThatThrownBy(() -> searchService.searchMoim(keywords1, area, category))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> searchService.searchMoim(keywords2, area, category))
                .isInstanceOf(IllegalArgumentException.class);
    }
}