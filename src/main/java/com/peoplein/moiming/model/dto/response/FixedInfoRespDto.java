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

public class FixedInfoRespDto {

    @ApiModel(value = "Moim API - 응답 - 모임 지역 고정 정보 조회")
    @Getter
    @NoArgsConstructor
    public static class FixedAreaRespDto {

        @JsonProperty("moimAreas")
        private List<MoimAreaDto> moimAreaDto;

        public FixedAreaRespDto(List<AreaValue> areaState) {
            this.moimAreaDto = areaState.stream().map(MoimAreaDto::new).collect(Collectors.toList());

        }

        public static class MoimAreaDto {
            public String state;
            public List<String> cities;

            public MoimAreaDto(AreaValue areaState) {
                this.state = areaState.getName();
                this.cities = areaState.getStateCities().stream().map(AreaValue::getName).collect(Collectors.toList());
            }
        }
    }


    @ApiModel(value = "Moim API - 응답 - 모임 카테고리 고정 정보 조회")
    @Getter
    @NoArgsConstructor
    public static class FixedMoimCategoryRespDto {

        @JsonProperty("moimCategories")
        private List<MoimCategoryDto> moimCategoryDto;

        public FixedMoimCategoryRespDto(List<Category> parentCategories, Map<Long, List<Category>> childCategoriesMap) {
            this.moimCategoryDto = parentCategories.stream().map(parent -> new MoimCategoryDto(parent, childCategoriesMap.get(parent.getId()))).collect(Collectors.toList());
        }

        public static class MoimCategoryDto {
            public String parentCategory;
            public List<String> childCategories;

            public MoimCategoryDto(Category parent, List<Category> categories) {
                this.parentCategory = parent.getCategoryName().getValue();
                this.childCategories = categories.stream().map(category -> category.getCategoryName().getValue()).collect(Collectors.toList());
            }
        }
    }


    @ApiModel(value = "Moim API - 응답 - 모임 게시물 카테고리 정보 조회")
    @Getter
    @NoArgsConstructor
    public static class FixedPostCategoryRespDto {

        @JsonProperty("moimPostCategories")
        private List<String> moimPostCategories;

        public FixedPostCategoryRespDto(List<String> moimPostCategories) {
            this.moimPostCategories = moimPostCategories;
        }
    }


    @ApiModel(value = "Moim API - 응답 - 서비스 신고 고정 정보 조회")
    @Getter
    @NoArgsConstructor
    public static class FixedReportInfoRespDto {


        @JsonProperty("reportInfo")
        private List<ReportTargetDetailDto> reportInfo;

        public FixedReportInfoRespDto(List<ReportTargetDetailDto> reportInfo) {
            this.reportInfo = reportInfo;
        }

        @Getter
        @Setter
        @AllArgsConstructor
        public static class ReportTargetDetailDto {

            private String reportTarget;
            private List<ReportIndexInfoDto> details;

            @Getter
            @Setter
            @AllArgsConstructor
            public static class ReportIndexInfoDto {
                private int index;
                private String info;
            }
        }
    }
}