package com.peoplein.moiming.domain.file;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "file")
public class File {

    @Id
    @GeneratedValue
    @Column(name = "file_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private FileDomain fileDomain;

    private String fileName;

    private String fileUrl;

    private String contentType;

    private LocalDateTime createdAt;

    public File(FileDomain fileDomain, String fileName, String fileUrl, String contentType) {

        this.fileDomain = fileDomain;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.contentType = contentType;

        // 초기화
        this.createdAt = LocalDateTime.now();
    }
}
