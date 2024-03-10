package com.peoplein.moiming.service;


import com.peoplein.moiming.domain.Report;
import com.peoplein.moiming.domain.enums.ReportReason;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.request.ReportReqDto;
import com.peoplein.moiming.repository.jpa.ReportJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.ReportReqDto.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportJpaRepository reportRepository;


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

    }
}
