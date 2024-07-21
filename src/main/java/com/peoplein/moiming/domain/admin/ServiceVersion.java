package com.peoplein.moiming.domain.admin;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "service_version")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceVersion {

    @Id
    @Column(name = "service_version_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean valid;

    private String platformName;

    private String versionNumber;

    private LocalDate releaseDate;

    private LocalDateTime createdAt;

    public ServiceVersion(String platformName, String versionNumber, LocalDate releaseDate) {

        this.platformName = platformName;
        this.versionNumber = versionNumber;
        this.releaseDate = releaseDate;

        this.valid = true;
        this.createdAt = LocalDateTime.now();

    }

}
