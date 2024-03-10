package com.peoplein.moiming.controller;

import com.peoplein.moiming.domain.MoimCategoryLinker;
import com.peoplein.moiming.domain.enums.AreaValue;
import com.peoplein.moiming.domain.enums.CategoryName;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimJoinRule;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.inner.MoimCategoryMapperDto;
import com.peoplein.moiming.security.auth.model.SecurityMember;
import com.peoplein.moiming.service.MoimService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.MoimReqDto.*;
import static com.peoplein.moiming.model.dto.response.MoimRespDto.*;

@Slf4j
@Api(tags = "모임 관련")
@RestController
@RequiredArgsConstructor
public class MoimController {

    private final MoimService moimService;

    @ApiOperation("모임 생성")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "모임 생성 성공", response = MoimCreateRespDto.class),
            @ApiResponse(code = 400, message = "모임 생성 실패, ERR MSG 확인")
    })
    @PostMapping(PATH_MOIM_CREATE)
    public ResponseEntity<?> createMoim(@RequestBody @Valid MoimCreateReqDto requestDto
            , BindingResult br
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        Moim moimOut = moimService.createMoim(requestDto, principal.getMember());
        MoimCreateRespDto respDto = new MoimCreateRespDto(moimOut);
        return new ResponseEntity<>(ResponseBodyDto.createResponse("1", "모임 생성 성공", respDto), HttpStatus.CREATED);
    }


    @ApiOperation("모임 일반 조회 - 유저의 모임 20개씩 조회 Paging (내가 운영중인지 모임만 불러오기 필터 설정 가능) (페이징시 lastMoimId 커서 전달 필수)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "유저의 모임 Paging 조회 성공", response = MoimViewRespDto.class),
            @ApiResponse(code = 400, message = "유저의 모임 Paging 조회 실패, ERR MSG 확인")
    })
    @GetMapping(PATH_MOIM_GET_VIEW)
    public ResponseEntity<?> getMemberMoims(
            @RequestParam(required = false, value = "lastMoimId") Long lastMoimId
            , @RequestParam(required = false, value = "isManagerReq", defaultValue = "false") boolean isManagerReq
            , @RequestParam(required = false, defaultValue = "20") int limit
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        MoimCategoryMapperDto mapper = moimService.getMemberMoims(lastMoimId, isManagerReq, limit, principal.getMember());

        List<Moim> targetMoims = mapper.getTargetMoims();
        Map<Long, List<MoimCategoryLinker>> categoryLinkersMap = mapper.getCategoryLinkersMap();

        List<MoimViewRespDto> responseData = targetMoims.stream().map(m -> {
            if (m == null || !categoryLinkersMap.containsKey(m.getId())) {
                log.error("{}, getMemberMoims :: {}", this.getClass().getName(), "Moim 을 불러오지 못했거나, 잘못된 Id mapped");
                throw new MoimingApiException(ExceptionValue.COMMON_INVALID_SITUATION);
            }
            return new MoimViewRespDto(m, categoryLinkersMap.get(m.getId()));
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "조회 성공", responseData));
    }


    @ApiOperation("모임 세부 조회 - 특정 모임 전체 정보 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "모임 세부 조회 성공", response = MoimViewRespDto.class),
            @ApiResponse(code = 400, message = "모임 세부 조회 실패, ERR MSG 확인")
    })
    @GetMapping(PATH_MOIM_GET_DETAIL)
    public ResponseEntity<?> getMoimDetail(@PathVariable Long moimId
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal
    ) {

        MoimMember moimMember = moimService.getMoimDetail(moimId, principal.getMember());
        MoimDetailViewRespDto responseData = new MoimDetailViewRespDto(moimMember);
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "세부 조회 성공", responseData));
    }


    @ApiOperation("모임 정보 수정")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "모임 정보 수정 성공", response = MoimViewRespDto.class),
            @ApiResponse(code = 400, message = "모임 정보 수정 실패, ERR MSG 확인")
    })
    @PatchMapping(PATH_MOIM_UPDATE)
    public ResponseEntity<?> updateMoim(@RequestBody @Valid MoimUpdateReqDto requestDto
            , BindingResult br
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        Moim moimOut = moimService.updateMoim(requestDto, principal.getMember());
        MoimUpdateRespDto respDto = new MoimUpdateRespDto(moimOut);
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "모임 정보 수정 성공", respDto));

    }


    @ApiOperation("모임 가입 조건 수정")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "모임 가입 조건 수정 성공", response = MoimViewRespDto.class),
            @ApiResponse(code = 400, message = "모임 가입 조건 수정 실패, ERR MSG 확인")
    })
    @PatchMapping(PATM_MOIM_JOIN_RULE_UPDATE)
    public ResponseEntity<?> updateMoimJoinRule(@RequestBody @Valid MoimJoinRuleUpdateReqDto requestDto
            , BindingResult br
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        MoimJoinRule joinRule = moimService.updateMoimJoinRule(requestDto, principal.getMember());
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "모임 가입 조건 수정 성공", new MoimJoinRuleUpdateRespDto(joinRule)));

    }


    @ApiOperation("모임 삭제")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "모임 및 모든 모임 정보 삭제 성공", response = MoimViewRespDto.class),
            @ApiResponse(code = 400, message = "모임 삭제 실패, ERR MSG 확인")
    })
    @DeleteMapping(PATH_MOIM_DELETE) // Moim 삭제시 정말 기록 남기지 않고 삭제한다
    public ResponseEntity<?> deleteMoim(@PathVariable Long moimId
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        moimService.deleteMoim(moimId, principal.getMember());
        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "모임 및 모든 모임 정보 삭제 성공", null));
    }


    @ApiOperation("모임 추천 검색 - 이번 달 기준 조회수 가장 많은 모임들 (offset=0 필수, limit 은 원하는 갯수 (Top 20 이면 20 지정))")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "모임 추천 성공", response = MoimSuggestedDto.class),
            @ApiResponse(code = 400, message = "모임 추천 실패, ERR MSG 확인")
    })
    @GetMapping(PATH_MOIM_SUGGESTED)
    public ResponseEntity<?> getSuggestedMoim(
            @RequestParam(required = false, defaultValue = "") String areaFilter
            , @RequestParam(required = false, defaultValue = "") String categoryFilter
            , @RequestParam(defaultValue = "0") int offset
            , @RequestParam(required = false, defaultValue = "20") int limit
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        if (offset != 0) {
            log.info("{}, getSuggestedMoim :: {}", this.getClass().getName(), "해당 요청의 offset 은 현재 요구사항 기준 0만 허용됩니다");
            throw new MoimingApiException(COMMON_INVALID_REQUEST_PARAM); // 필수 parameter 누락,
        }

        AreaValue areaFilterVal = null;
        if (StringUtils.hasText(areaFilter)) { // 뭐라고 들어왔으면 필터가 걸린 것
            areaFilterVal = AreaValue.fromQueryParam(areaFilter);
        }

        CategoryName categoryFilterVal = null;
        if (StringUtils.hasText(categoryFilter)) {
            categoryFilterVal = CategoryName.fromQueryParam(categoryFilter);
        }

        MoimCategoryMapperDto mapper = moimService.getSuggestedMoim(areaFilterVal, categoryFilterVal, offset, limit);

        List<Moim> targetMoims = mapper.getTargetMoims();
        Map<Long, List<MoimCategoryLinker>> categoryLinkersMap = mapper.getCategoryLinkersMap();


        List<MoimSuggestedDto> suggestedMoims = targetMoims.stream().map(m -> {
            if (m == null || !categoryLinkersMap.containsKey(m.getId())) {
                log.error("추천 모임 Controller :: {}, {}", "Moim 을 불러오지 못했거나, 잘못된 Id mapped : ", COMMON_INVALID_SITUATION.getErrMsg());
                throw new MoimingApiException(COMMON_INVALID_SITUATION);
            }
            return new MoimSuggestedDto(m, categoryLinkersMap.get(m.getId()));
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "모임 추천 조회 성공", suggestedMoims));
    }

}


