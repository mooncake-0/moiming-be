package com.peoplein.moiming.service;


import com.peoplein.moiming.domain.Report;
import com.peoplein.moiming.domain.enums.ReportReason;
import com.peoplein.moiming.domain.file.File;
import com.peoplein.moiming.domain.file.FileDomain;
import com.peoplein.moiming.domain.file.RelatedFile;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.request.ReportReqDto;
import com.peoplein.moiming.repository.jpa.ReportJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;

import java.util.List;

import static com.peoplein.moiming.domain.file.FileDomain.POST;
import static com.peoplein.moiming.domain.file.FileDomain.REPORT;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.ReportReqDto.*;

@Service
@RequiredArgsConstructor
public class ReportService {


    private final RelatedFileService relatedFileService;
    private final ReportJpaRepository reportRepository;
    private final StorageService storageService;


    @Transactional
    public List<File> createReportImg(List<MultipartFile> imgFiles, Member member) {

        if (member == null || imgFiles == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        return storageService.updateMultipleImgs(FileDomain.REPORT, imgFiles, member);

    }



    // MEMO :: 각 TG 들이 실제로 존재하는지에 대해 여부는 굳이 체크하지 않는다
    //         요청 특성상 그런 요청이 들어올 확률은 매우 적은데, 그걸 위해 매번 쿼리를 한 번 더 돌려주는게 좀 비효율 적으로 보임
    @Transactional
    public void createReport(ReportCreateReqDto requestDto, Member reporter) {

        if (requestDto == null || reporter == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        ReportReason reason = ReportReason.findReason(requestDto.getTarget(), requestDto.getReasonIndex());
        Report report = new Report(requestDto.getTarget(), reporter.getId(), requestDto.getTargetId(), reason.getInfo(), requestDto.getHasFiles());
        reportRepository.save(report);

        if(requestDto.getHasFiles() != null) {
            checkFileReqStatus(requestDto.getHasFiles(), requestDto.getImgFileIds());
            if (requestDto.getHasFiles()) { // 존재할 경우 저장
                relatedFileService.saveRelatedFiles(report.getId(), requestDto.getImgFileIds(), reporter.getId());
            }
        }else{
            // 여기서 발생할 리는 없음, Validation 존재
            throw new MoimingApiException(COMMON_REQUEST_VALIDATION);
        }
    }


    private void checkFileReqStatus(boolean hasFiles, List<Long> fileIds) {
        if (hasFiles) { // 있다고 했는데 파일 없음
            if (fileIds == null || fileIds.isEmpty()) {
                throw new MoimingApiException(IMG_STATE_NOT_VALID);
            }
        } else {// 없다고 했는데 파일 있음
            if (fileIds != null && !fileIds.isEmpty()) {
                throw new MoimingApiException(IMG_STATE_NOT_VALID);
            }
        }
    }
}
