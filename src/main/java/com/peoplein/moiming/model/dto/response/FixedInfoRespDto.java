package com.peoplein.moiming.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.peoplein.moiming.domain.enums.AreaValue;
import com.peoplein.moiming.domain.enums.MoimPostCategory;
import com.peoplein.moiming.domain.fixed.Category;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApiModel(value = "Moim API - 응답 - 모임 고정 정보 조회 (지역 / 카테고리)")
@Getter
@Setter
@NoArgsConstructor
public class FixedInfoRespDto {

    @JsonProperty("moimAreas")
    private List<MoimAreaDto> moimAreaDto;

    @JsonProperty("moimCategories")
    private List<MoimCategoryDto> moimCategoryDto;

    @JsonProperty("moimPostCategories")
    private List<String> moimPostCategories;

    @JsonProperty("reportInfo")
    private List<ReportTargetDetailDto> reportInfo;

    public FixedInfoRespDto(List<AreaValue> areaState, List<Category> parentCategories, Map<Long, List<Category>> childCategoriesMap, List<ReportTargetDetailDto> reportInfo) {

        this.moimAreaDto = areaState.stream().map(MoimAreaDto::new).collect(Collectors.toList());
        this.moimCategoryDto = parentCategories.stream().map(parent -> new MoimCategoryDto(parent, childCategoriesMap.get(parent.getId()))).collect(Collectors.toList());
        this.moimPostCategories = new ArrayList<>();
        for (MoimPostCategory mpc : MoimPostCategory.values()) {
            moimPostCategories.add(mpc.getValue());
        }
        this.reportInfo = reportInfo;
    }

    public static class MoimAreaDto {
        public String state;
        public List<String> cities;
        public MoimAreaDto(AreaValue areaState) {
            this.state = areaState.getName();
            this.cities = areaState.getStateCities().stream().map(AreaValue::getName).collect(Collectors.toList());
        }
    }

    public static class MoimCategoryDto {
        public String parentCategory;
        public List<String> childCategories;
        public MoimCategoryDto(Category parent, List<Category> categories) {
            this.parentCategory = parent.getCategoryName().getValue();
            this.childCategories = categories.stream().map(category -> category.getCategoryName().getValue()).collect(Collectors.toList());
        }
    }


    @Getter
    @Setter
    @AllArgsConstructor
    public static class ReportTargetDetailDto{

        private String reportTarget;
        private List<ReportIndexInfoDto> details;

        @Getter
        @Setter
        @AllArgsConstructor
        public static class ReportIndexInfoDto{
            private int index;
            private String info;
        }
    }
}