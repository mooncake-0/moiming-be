package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.enums.ReportTarget;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.request.ReportReqDto;
import com.peoplein.moiming.repository.jpa.ReportJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.peoplein.moiming.model.dto.request.ReportReqDto.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private ReportJpaRepository reportRepository;


    @Test
    void createReport_shouldPass_whenRightInfoPassed(){

        // given
        ReportCreateReqDto requestDto = mock(ReportCreateReqDto.class);
        Member reporter = mock(Member.class);

        // given - stub (Report Reason 생성은 책임져야 함)
        when(requestDto.getTarget()).thenReturn(ReportTarget.MOIM);
        when(requestDto.getReasonIndex()).thenReturn(0);

        // when
        reportService.createReport(requestDto, reporter);

        // then
        verify(reportRepository, times(1)).save(any());

    }


    @Test
    void createReport_shouldThrowException_whenReportReasonMapFailed_byMoimingApiException() {

        // given
        ReportCreateReqDto requestDto = mock(ReportCreateReqDto.class);
        Member reporter = mock(Member.class);

        // given - stub (실패할 값들 지정)
        when(requestDto.getTarget()).thenReturn(null);
        when(requestDto.getReasonIndex()).thenReturn(100);

        // when
        // then
        assertThatThrownBy(() -> reportService.createReport(requestDto, reporter)).isInstanceOf(MoimingApiException.class);
        verify(reportRepository, times(0)).save(any());

    }

}
