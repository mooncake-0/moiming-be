package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.CategoryRepository;
import com.peoplein.moiming.support.TestMockCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest extends TestMockCreator {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;


    // Input Data 를 가지고 직접 처리해주는 영역이 있음 > input Data 확인하는 Test 필요
    // generateCategoryList 는 굳이 return value 를 확인해줄 필요가 없음 - repository 가 한 일을 반환할 뿐임
    // 그냥 함수를 주어진 상황대로 진행하면서 validateCategories 를 잘 통과한다면 성공 CASE
    @Test
    void generateCategoryList_shouldProcess_whenRightInfoPassed() {

        // given
        List<String> params = List.of(depth1SampleCategory, depth2SampleCategory);

        // given - stub return val (validateCategories 검증을 통과 Case 여야 한다)
        Category category1 = mockCategory(1L, CategoryName.fromValue(depth1SampleCategory), 0, null);
        Category category2 = mockCategory(1L, CategoryName.fromValue(depth2SampleCategory), 1, category1);

        // given - stub
        doReturn(List.of(category1, category2)).when(categoryRepository).findByCategoryNames(any()); // findByCategoryNames 의 동작성은 이 테스트와 무관

        // when
        // then
        assertDoesNotThrow(() -> categoryService.generateCategoryList(params));

    }


    // Null 혹은 Empty List 검증인 경우 직접 return value 를 생성하고 반환한다 > 검증 필요
    // 2
    @Test
    void generateCategoryList_shouldReturnEmpty_whenNullArrayPassed() {

        // given
        // when
        List<Category> categories = categoryService.generateCategoryList(null);

        // then
        assertTrue(categories.isEmpty());

    }



    // 3 empty 전달
    @Test
    void generateCategoryList_shouldReturnEmpty_whenEmptyArrayPassed() {

        // given
        // when
        List<Category> categories = categoryService.generateCategoryList(new ArrayList<>());

        // then
        assertTrue(categories.isEmpty());
    }



    // 4. validateCategories 실패 사유 검증 > 1 parent 없음
    //                                  > 2 child 없음
    //                                  > 3 종속관계가 아닌 카테고리들


    @Test
    void generateCategoryList_shouldThrowException_whenNoParentCategory_byMoimingApiException() {

        // given
        List<String> params = List.of(depth2SampleCategory); // 예외 통과를 위해 필요 (사실 예외만 통과하면 뭔 값이든 상관 없음)

        // given - stub return val (validateCategories 검증을 통과 Case 여야 한다)
        Category category1 = mockCategory(1L, CategoryName.fromValue(depth1SampleCategory), 0, null);
        Category category2 = mockCategory(1L, CategoryName.fromValue(depth2SampleCategory), 1, category1);

        // given - stub
        doReturn(List.of(category2)).when(categoryRepository).findByCategoryNames(any()); // findByCategoryNames 의 동작성은 이 테스트와 무관

        // when
        // then
        assertThatThrownBy(() -> categoryService.generateCategoryList(params)).isInstanceOf(MoimingApiException.class);

    }



    @Test
    void generateCategoryList_shouldThrowException_whenNoChildCategory_byMoimingApiException() {

        // given
        List<String> params = List.of(depth1SampleCategory); // 예외 통과를 위해 필요 (사실 예외만 통과하면 뭔 값이든 상관 없음)

        // given - stub return val (validateCategories 검증을 통과 Case 여야 한다)
        Category category1 = mockCategory(1L, CategoryName.fromValue(depth1SampleCategory), 0, null);
        Category category2 = mockCategory(1L, CategoryName.fromValue(depth2SampleCategory), 1, category1);

        // given - stub
        doReturn(List.of(category1)).when(categoryRepository).findByCategoryNames(any()); // findByCategoryNames 의 동작성은 이 테스트와 무관

        // when
        // then
        assertThatThrownBy(() -> categoryService.generateCategoryList(params)).isInstanceOf(MoimingApiException.class);

    }



    @Test
    void generateCategoryList_shouldThrowException_whenCascadeRelationWrong_byMoimingApiException() {

        // given
        String wrongCategoryName = CategoryName.BOOK.getValue(); // 다른 부모 카테고리
        List<String> params = List.of(wrongCategoryName, depth2SampleCategory);

        // given - stub return val (validateCategories 검증을 통과 Case 여야 한다)
        Category wrongCategory = mockCategory(1L, CategoryName.fromValue(wrongCategoryName), 0, null);
        Category category1 = mockCategory(1L, CategoryName.fromValue(depth1SampleCategory), 0, null);
        Category category2 = mockCategory(1L, CategoryName.fromValue(depth2SampleCategory), 1, category1); // 정상 부모 매핑, but 다른 1차 카테고리와 엮임

        // given - stub
        doReturn(List.of(wrongCategory, category2)).when(categoryRepository).findByCategoryNames(any()); // findByCategoryNames 의 동작성은 이 테스트와 무관

        // when
        // then
        assertThatThrownBy(() -> categoryService.generateCategoryList(params)).isInstanceOf(MoimingApiException.class);

    }


}
