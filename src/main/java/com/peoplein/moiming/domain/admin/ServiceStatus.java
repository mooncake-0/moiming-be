package com.peoplein.moiming.domain.admin;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "app_version")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceStatus {

    /*
     - 정상 운영중인지, 점검중인지 띄어 놓을 수 있다
     - 점검 중이라면, 점검 시작 시간과 예상 종료 시간을 조회할 수 있따
     - description 을 적어 놓을 수 있다
     - 최근 생성일을 적어놓아서, app 에서는 최근 상태를 조회하도록 한다
     */

    @Id
    @Column(name = "service_status_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean underMaintenance;

    private LocalDateTime maintenanceStart;

    private LocalDateTime maintenanceEnd;

    private String statusDescription;

    private LocalDateTime createdAt; // 기준으로 가장 최신 상태를 조회하게 된다
    private Long creatorId;
    private LocalDateTime updatedAt;
    private Long updaterId;

    public ServiceStatus(boolean underMaintenance, LocalDateTime maintenanceStart, LocalDateTime maintenanceEnd
            , String statusDescription, Long creatorId) {
        this.underMaintenance = underMaintenance;
        this.maintenanceStart = maintenanceStart;
        this.maintenanceEnd = maintenanceEnd;
        this.statusDescription = statusDescription;
        this.creatorId = creatorId;

        this.createdAt = LocalDateTime.now();
    }

}
