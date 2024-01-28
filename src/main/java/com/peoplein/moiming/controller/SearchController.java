package com.peoplein.moiming.controller;

import com.peoplein.moiming.config.AppUrlPath;
import com.peoplein.moiming.domain.MoimCategoryLinker;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.response.SearchRespDto;
import com.peoplein.moiming.security.auth.model.SecurityMember;
import com.peoplein.moiming.service.SearchService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.response.SearchRespDto.*;

@Slf4j
@Api(tags = "모임 검색 관련")
@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    // 규칙
    // 1. 검색어 2자 이상, 2. sortBy 는 date 만 지원 (date가 default) , 3. 필요시 areaFilter, categoryFilter ON, 4. offset 은 필수. 처음이여도 0 기입. 5. 공백 전달, 자음, 모음만 전달 불가
    // 6. State 전체시, 해당 State 이름을 보낸다
    // 7. 검색어에서 카테고리, 지역 포함을 시킬 때는, depth ==1 수준은 제외된다. (즉, 서울로 입력했을때, 서울시 전체 지역 필터가 걸리지 않는다) (카테고리도 마찬가지)
    @ApiOperation("메인화면 모임 검색")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "검색 성공"),
            @ApiResponse(code = 400, message = "검색 실패, ERR MSG 확인")
    })
    @GetMapping(PATH_SEARCH_MOIM)
    public ResponseEntity<?> searchMoim(
            @RequestParam(defaultValue = "") String keyword
            , @RequestParam(required = false, defaultValue = "date") String sortBy
            , @RequestParam(required = false, defaultValue =  "") String areaFilter
            , @RequestParam(required = false, defaultValue =  "") String categoryFilter
            , @RequestParam(defaultValue = "-1") int offset
            , @RequestParam(required = false, defaultValue = "20") int limit
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        keyword = keyword.strip();
        moimKeywordValidation(keyword);

        if (!StringUtils.hasText(keyword) || offset == -1) {
            throw new MoimingApiException(COMMON_INVALID_REQUEST_PARAM); // 필수 parameter 누락,
        }

        Map<String, Object> listMap = searchService.searchMoim(keyword, sortBy, areaFilter, categoryFilter, offset, limit);

        List<Moim> searchPagedMoims = (List<Moim>) listMap.get("PAGED_MOIMS");
        List<MoimCategoryLinker> categoryLinkers = (List<MoimCategoryLinker>) listMap.get("CATEGORIES");

        Map<Long, List<MoimCategoryLinker>> categoryLinkersMap = new HashMap<>();
        for (MoimCategoryLinker categoryLinker : categoryLinkers) {
            Long keyId = categoryLinker.getMoim().getId();
            if (categoryLinkersMap.containsKey(keyId)) {
                categoryLinkersMap.get(keyId).add(categoryLinker);
            } else {
                List<MoimCategoryLinker> eachCategories = new ArrayList<>();
                eachCategories.add(categoryLinker);
                categoryLinkersMap.put(keyId, eachCategories);
            }
        }

        List<SearchMoimRespDto> responses = searchPagedMoims.stream().map(moim -> new SearchMoimRespDto(moim, categoryLinkersMap.get(moim.getId()))).collect(Collectors.toList());

        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "모임 검색 성공", responses));
    }


    // 자음
    private void moimKeywordValidation(String keywords) {

        Pattern forbiddenPattern = Pattern.compile("[ㄱ-ㅎㅏ-ㅣ]+");
        Matcher matcher = forbiddenPattern.matcher(keywords);
        if (matcher.find()) {
            throw new MoimingApiException(SEARCH_KEYWORD_INVALID); // 단순 자음, 모음으로만 이루어진건 검색불가
        }

        if (keywords.length() < 2 || keywords.length() > 20) {
            throw new MoimingApiException(SEARCH_KEYWORD_LENGTH_INVALID); // 검색어는 최소 두자 이상입니다.
        }
    }
}
