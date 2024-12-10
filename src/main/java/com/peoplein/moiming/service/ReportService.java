package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.Report;
import com.peoplein.moiming.domain.enums.ContentStatus;
import com.peoplein.moiming.domain.enums.ReportReason;
import com.peoplein.moiming.domain.enums.ReportTarget;
import com.peoplein.moiming.domain.file.File;
import com.peoplein.moiming.domain.file.FileDomain;
import com.peoplein.moiming.domain.file.RelatedFile;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.request.ReportReqDto;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.repository.PostCommentRepository;
import com.peoplein.moiming.repository.jpa.ReportJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;

import java.util.List;

import static com.peoplein.moiming.domain.file.FileDomain.POST;
import static com.peoplein.moiming.domain.file.FileDomain.REPORT;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.ReportReqDto.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {


    private final RelatedFileService relatedFileService;
    private final ReportJpaRepository reportRepository;
    private final StorageService storageService;

    private final MoimRepository moimRepository;
    private final MoimPostRepository moimPostRepository;
    private final PostCommentRepository postCommentRepository;

    @Transactional
    public List<File> createReportImg(List<MultipartFile> imgFiles, Member member) {

        if (member == null || imgFiles == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        return storageService.updateMultipleImgs(FileDomain.REPORT, imgFiles, member);

    }



    // MEMO :: 각 TG 들이 실제로 존재하는지에 대해 여부는 굳이 체크하지 않는다
    //         요청 특성상 그런 요청이 들어올 확률은 매우 적은데, 그걸 위해 매번 쿼리를 한 번 더 돌려주는게 좀 비효율 적으로 보임
    //         2412 변경 :: 근데 상태 변경해야 해서 어차피 식별해야함
    @Transactional
    public void createReport(ReportCreateReqDto requestDto, Member reporter) {

        if (requestDto == null || requestDto.getTarget() == null || reporter == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        // Mapping 이 다 잘 되는 것을 확인한 후에 DB 작업을 시작하기 위해 먼저 진행
        ReportReason reason = ReportReason.findReason(requestDto.getTarget(), requestDto.getReasonIndex());

        // REPORT 에 따른 자원들 상태 식별 필요 - 식별이 정상적으로 진행 되면 저장한다
        if (requestDto.getTarget().equals(ReportTarget.USER)) {
            // TODO :: 유저 신고 상태 관리 필요
        } else if (requestDto.getTarget().equals(ReportTarget.MOIM)) {
            Moim moim = moimRepository.findById(requestDto.getTargetId()).orElseThrow(() -> logAndThrowNotFoundException(requestDto.getTargetId(), requestDto.getTarget()));
            moim.changeContentStatus(ContentStatus.REPORTED, reporter.getId());
        } else if (requestDto.getTarget().equals(ReportTarget.POST)) {
            MoimPost moimPost = moimPostRepository.findById(requestDto.getTargetId()).orElseThrow(() -> logAndThrowNotFoundException(requestDto.getTargetId(), requestDto.getTarget()));
            moimPost.changeContentStatus(ContentStatus.REPORTED, reporter.getId());
        } else if (requestDto.getTarget().equals(ReportTarget.COMMENT)) {
            PostComment postComment = postCommentRepository.findById(requestDto.getTargetId()).orElseThrow(() -> logAndThrowNotFoundException(requestDto.getTargetId(), requestDto.getTarget()));
            postComment.changeContentStatus(ContentStatus.REPORTED, reporter.getId());
        }

        Report report = new Report(requestDto.getTarget(), reporter.getId(), requestDto.getTargetId(), reason.getInfo(), requestDto.getHasFiles());
        reportRepository.save(report);

        if (requestDto.getHasFiles() != null) {
            checkFileReqStatus(requestDto.getHasFiles(), requestDto.getImgFileIds());
            if (requestDto.getHasFiles()) { // 존재할 경우 저장
                relatedFileService.saveRelatedFiles(report.getId(), requestDto.getImgFileIds(), reporter.getId());
            }
        } else {
            // 여기서 발생할 리는 없음, Validation 존재
            log.info("{}, createReport :: {}", this.getClass().getName(), "발생할 일 없는 에러 발생 - has files 필드가 안들어옴");
            throw new MoimingApiException(COMMON_REQUEST_VALIDATION);
        }
    }

    private MoimingApiException logAndThrowNotFoundException(Long tgId, ReportTarget tg) {
        String l = "[" + tgId + "]의 [" + tg + "] 을 찾지 못함";
        log.warn("{}, createReport :: {}", this.getClass(), l);
        return new MoimingApiException(REPORT_TG_NOT_FOUND, l);
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
