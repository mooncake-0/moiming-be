package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.PostComment;
import com.peoplein.moiming.domain.enums.ReportTarget;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.request.ReportReqDto;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.repository.MoimRepository;
import com.peoplein.moiming.repository.PostCommentRepository;
import com.peoplein.moiming.repository.jpa.ReportJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.model.dto.request.ReportReqDto.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private ReportJpaRepository reportRepository;

    @Mock
    private RelatedFileService fileService;
    @Mock
    private MoimRepository moimRepository;
    @Mock
    private MoimPostRepository moimPostRepository;
    @Mock
    private PostCommentRepository postCommentRepository;


    // createReportImg 간단하므로 생략

    // 성공 - createReport : Moim 신고일 시
    @Test
    void createReport_shouldChangeStatus_whenMoimReported() {

        // given
        ReportCreateReqDto requestDto = mock(ReportCreateReqDto.class);
        Member reporter = mock(Member.class);
        Moim reportedMoim = mock(Moim.class);

        // given - stub (Report Reason 생성은 책임져야 함)
        when(requestDto.getTarget()).thenReturn(ReportTarget.MOIM);
        when(moimRepository.findById(any())).thenReturn(Optional.of(reportedMoim));
        when(requestDto.getReasonIndex()).thenReturn(0);

        // when
        reportService.createReport(requestDto, reporter);

        // then
        verify(reportedMoim, times(1)).changeContentStatus(any(), any());
        verify(reportRepository, times(1)).save(any());
        verify(fileService, times(0)).saveRelatedFiles(any(), any(), any());

    }

    // 성공 - createReport : Moim 신고했는데 사진이 있는 경우 (사진이 있는 경우의 대표 TEST)
    @Test
    void createReport_shouldChangeStatusAndSaveFiles_whenMoimReported() {

        // given
        ReportCreateReqDto requestDto = mock(ReportCreateReqDto.class);
        Member reporter = mock(Member.class);
        Moim reportedMoim = mock(Moim.class);

        // given - stub (Report Reason 생성은 책임져야 함)
        when(requestDto.getTarget()).thenReturn(ReportTarget.MOIM);
        when(moimRepository.findById(any())).thenReturn(Optional.of(reportedMoim));
        when(requestDto.getReasonIndex()).thenReturn(0);
        when(requestDto.getHasFiles()).thenReturn(true);
        when(requestDto.getImgFileIds()).thenReturn(List.of(1L));

        // when
        reportService.createReport(requestDto, reporter);

        // then
        verify(reportedMoim, times(1)).changeContentStatus(any(), any());
        verify(reportRepository, times(1)).save(any());
        verify(fileService, times(1)).saveRelatedFiles(any(), any(), any());

    }

    @Test
    void createReport_shouldChangeStatus_whenMoimPostReported() {

        // given
        ReportCreateReqDto requestDto = mock(ReportCreateReqDto.class);
        Member reporter = mock(Member.class);
        MoimPost reportedPost = mock(MoimPost.class);

        // given - stub (Report Reason 생성은 책임져야 함)
        when(requestDto.getTarget()).thenReturn(ReportTarget.POST);
        when(moimPostRepository.findById(any())).thenReturn(Optional.of(reportedPost));
        when(requestDto.getReasonIndex()).thenReturn(0);

        // when
        reportService.createReport(requestDto, reporter);

        // then
        verify(reportRepository, times(1)).save(any());
        verify(reportedPost, times(1)).changeContentStatus(any(), any());
        verify(fileService, times(0)).saveRelatedFiles(any(), any(), any());

    }

    @Test
    void createReport_shouldChangeStatus_whenPostCommentReported() {

        // given
        ReportCreateReqDto requestDto = mock(ReportCreateReqDto.class);
        Member reporter = mock(Member.class);
        PostComment reportedComment = mock(PostComment.class);

        // given - stub (Report Reason 생성은 책임져야 함)
        when(postCommentRepository.findById(any())).thenReturn(Optional.of(reportedComment));
        when(requestDto.getTarget()).thenReturn(ReportTarget.COMMENT);
        when(requestDto.getReasonIndex()).thenReturn(0);

        // when
        reportService.createReport(requestDto, reporter);

        // then
        verify(reportRepository, times(1)).save(any());
        verify(reportedComment, times(1)).changeContentStatus(any(), any());
        verify(fileService, times(0)).saveRelatedFiles(any(), any(), any());

    }


    @Test
    void createReport_shouldThrowException_whenReportReasonMapFailed_byMoimingApiException() {

        // given
        ReportCreateReqDto requestDto = mock(ReportCreateReqDto.class);
        Member reporter = mock(Member.class);

        // given - stub
        when(requestDto.getTarget()).thenReturn(ReportTarget.MOIM);
        when(requestDto.getReasonIndex()).thenReturn(100); // 이런건 없음! // 실패할 값을 지정

        // when
        // then
        assertThatThrownBy(() -> reportService.createReport(requestDto, reporter)).isInstanceOf(MoimingApiException.class);
        verify(reportRepository, times(0)).save(any());

    }

}
