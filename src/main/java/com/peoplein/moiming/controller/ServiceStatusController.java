package com.peoplein.moiming.controller;


import com.peoplein.moiming.domain.admin.ServiceStatus;
import com.peoplein.moiming.domain.admin.ServiceVersion;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.response.SearchRespDto;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.peoplein.moiming.config.AppUrlPath.*;
import static com.peoplein.moiming.exception.ExceptionValue.COMMON_INVALID_REQUEST_PARAM;
import static com.peoplein.moiming.exception.ExceptionValue.COMMON_INVALID_SITUATION;

/*
 간단한 요청들이니 그냥 Controller 에서 다 처리했다..
 */
@Slf4j
@Api(tags = "서비스 상태 관련")
@RestController
@RequiredArgsConstructor
public class ServiceStatusController {

    private final EntityManager em;

    @ApiOperation("현재 플랫폼별 버전 조회 (platformName 변수 필수 & ios/aos/server 중 택1)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "조회 성공"),
            @ApiResponse(code = 400, message = "조회 실패, ERR MSG 확인")
    })
    @GetMapping(PATH_GET_PLATFORM_VERSION)
    public ResponseEntity<?> getPlatformVersion(@RequestParam(required = false) String platformName) {

        if (!StringUtils.hasText(platformName) || (!platformName.equalsIgnoreCase("aos") && !platformName.equalsIgnoreCase("ios") && !platformName.equalsIgnoreCase("server"))) {
            throw new MoimingApiException(COMMON_INVALID_REQUEST_PARAM);
        }

        // 현재 유효한 버젼 조회
        List<ServiceVersion> versions = em.createQuery("SELECT sv FROM ServiceVersion sv " +
                        "WHERE sv.platformName = :platformName " +
                        "AND sv.valid = :valid", ServiceVersion.class)
                .setParameter("platformName", platformName)
                .setParameter("valid", true)
                .getResultList();

        if (versions.size() != 1) {
            log.warn("{}, getPlatformVersion :: {}", this.getClass().getName(), "[" + platformName + "] 유효 버전 조회 이상 (size:" + versions.size() + ")");
            throw new MoimingApiException(COMMON_INVALID_SITUATION);
        }

        ServiceVersion curSv = versions.get(0);

        Map<String, String> responseData = new HashMap<>();
        responseData.put("platform", curSv.getPlatformName());
        responseData.put("version", curSv.getVersionNumber());
        responseData.put("releaseDate", curSv.getReleaseDate() + "");

        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "조회 성공", responseData));
    }


    @ApiOperation("현재 (최근) 서비스 상태 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "조회 성공"),
            @ApiResponse(code = 400, message = "조회 실패, ERR MSG 확인")
    })
    @GetMapping(PATH_GET_SERVICE_STATUS)
    public ResponseEntity<?> getServiceStatus() {

        // 가장 최신 서비스 상태 조회
        List<ServiceStatus> statuses = em.createQuery("SELECT ss FROM ServiceStatus ss " +
                        "ORDER BY ss.createdAt DESC", ServiceStatus.class)
                .setMaxResults(1)
                .getResultList();

        if (statuses.size() != 1) {
            log.warn("{}, getServiceStatus :: {}", this.getClass().getName(), "가장 최신 버전 상태 조회시 오류 (size:" + statuses.size() + ")");
            throw new MoimingApiException(COMMON_INVALID_SITUATION);
        }

        ServiceStatus curVerStatus = statuses.get(0);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("underMaintenance", curVerStatus.isUnderMaintenance());
        responseData.put("maintenanceStart", curVerStatus.getMaintenanceStart());
        responseData.put("maintenanceEnd", curVerStatus.getMaintenanceEnd());
        responseData.put("statusDesc", curVerStatus.getStatusDescription());
        responseData.put("createdAt", curVerStatus.getCreatedAt() + "");

        return ResponseEntity.ok(ResponseBodyDto.createResponse("1", "조회 성공", responseData));
    }

}
