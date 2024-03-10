package com.peoplein.moiming.controller;

import com.peoplein.moiming.config.AppUrlPath;
import com.peoplein.moiming.model.ResponseBodyDto;
import com.peoplein.moiming.model.dto.request.ReportReqDto;
import com.peoplein.moiming.model.dto.response.MoimPostRespDto;
import com.peoplein.moiming.security.auth.model.SecurityMember;
import com.peoplein.moiming.service.ReportService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

import static com.peoplein.moiming.model.dto.request.ReportReqDto.*;

@Api(tags = "신고 관련")
@RestController
@RequiredArgsConstructor
public class ReportController {


    private final ReportService reportService;


    @ApiOperation("대상 신고")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Bearer {JWT_ACCESS_TOKEN}", required = true, paramType = "header")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "신고 성공"),
            @ApiResponse(code = 400, message = "신고 실패, ERR MSG 확인")
    })
    @PostMapping(AppUrlPath.PATH_CREATE_REPORT)
    public ResponseEntity<?> report(@RequestBody @Valid ReportCreateReqDto requestDto
            , BindingResult br
            , List<MultipartFile> file
            , @AuthenticationPrincipal @ApiIgnore SecurityMember principal) {

        reportService.createReport(requestDto, principal.getMember());
        return new ResponseEntity<>(ResponseBodyDto.createResponse("1", "신고 성공", null), HttpStatus.CREATED);
    }

}
