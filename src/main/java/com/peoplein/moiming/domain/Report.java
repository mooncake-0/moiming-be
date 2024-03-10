package com.peoplein.moiming.domain;


import com.peoplein.moiming.domain.enums.ReportTarget;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report{

    @Id
    @GeneratedValue
    @Column(name = "report_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private ReportTarget target;

    private Long reporterId;

    private Long targetId;

    private String reason;

    private boolean hasFiles;

    // TODO 사진 관련 추가

    private LocalDateTime createdAt;

    public Report(ReportTarget target, Long reporterId, Long targetId, String reason, boolean hasFiles) {
        this.target = target;
        this.reporterId = reporterId;
        this.targetId = targetId;
        this.reason = reason;
        this.hasFiles = hasFiles;

        // 초기화
        this.createdAt = LocalDateTime.now();
    }
}
