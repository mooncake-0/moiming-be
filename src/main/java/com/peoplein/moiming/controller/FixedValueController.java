package com.peoplein.moiming.controller;


import com.peoplein.moiming.domain.enums.AreaValue;
import com.peoplein.moiming.domain.enums.ReportReason;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.inner.AppCategoryDto;
import com.peoplein.moiming.model.dto.response.FixedInfoRespDto;
import com.peoplein.moiming.service.CategoryService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.model.dto.response.FixedInfoRespDto.*;
import static com.peoplein.moiming.model.dto.response.FixedInfoRespDto.ReportTargetDetailDto.*;

@RestController
@Api(tags = "고정 값 요청 (인증 불필요)")
@RequiredArgsConstructor
public class FixedValueController {

    private final CategoryService categoryService;

    @ApiOperation("인 앱 고정 정보 조회 (지역 / 카테고리 / 게시물 카테고리 / 신고 사유 본문) - 앱 기동중에는 변하지 않을 정보 - 저장 후 사용 권장")
    @ApiResponses({
            @ApiResponse(code = 200, message = "인 앱 고정 정보 조회 성공"),
            @ApiResponse(code = 400, message = "인 앱 고정 정보 조회 실패")
    })
    @GetMapping(PATH_FIXED_VALUES)
    public ResponseEntity<?> getFixedInfo() {

        // 지역 조회
        List<AreaValue> areaState = getAreaStates();

        // Category All 조회
        AppCategoryDto allCategories = categoryService.getAllCategories();

        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "인 앱 고정 정보 조회 성공",
                new FixedInfoRespDto(areaState, allCategories.getParentCategories(), allCategories.getChildCategoriesMap(), getReportReasons())
        ));
    }


    private List<AreaValue> getAreaStates() {

        List<AreaValue> areaState = new ArrayList<>();

        for (AreaValue areaValue : AreaValue.values()) {
            if (areaValue.getState() == null) { // 부모일 경우
                areaState.add(areaValue);
            }
        }

        return areaState;
    }



    private List<ReportTargetDetailDto> getReportReasons() {

        List<ReportIndexInfoDto> commonReportInfo = new ArrayList<>();
        List<ReportIndexInfoDto> userReportInfo = new ArrayList<>();
        for (ReportReason reason : ReportReason.values()) {
            ReportIndexInfoDto reportInfo = new ReportIndexInfoDto(reason.getIndex(), reason.getInfo());
            if (reason.getTarget() == null) {
                commonReportInfo.add(reportInfo);
            } else { // USER
                userReportInfo.add(reportInfo);
            }
        }

        List<ReportTargetDetailDto> reportInfoDto = new ArrayList<>();
        reportInfoDto.add(new ReportTargetDetailDto("COMMON", commonReportInfo));
        reportInfoDto.add(new ReportTargetDetailDto("USER", userReportInfo));

        return reportInfoDto;
    }


}
