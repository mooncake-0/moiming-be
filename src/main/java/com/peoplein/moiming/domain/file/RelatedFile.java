package com.peoplein.moiming.domain.file;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

/*
 Report 와 Post 처럼, 여러개의 파일을 가지고 있는 Entity 들을 위함
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "related_file")
public class RelatedFile {

    @Id
    @Column(name = "related_file_id")
    @GeneratedValue
    private Long id;

    private int sequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    // MEMO :: INDEX 필요
    private Long domainId;

    @Enumerated(EnumType.STRING)
    private FileDomain fileDomain;

    private String fileUrl;

    private LocalDateTime createdAt;
    private Long creatorId;

    private LocalDateTime updatedAt;
    private Long updaterId;

    public RelatedFile(int sequence, File file, Long domainId, Long creatorId) {

        this.sequence = sequence;
        this.file = file;
        this.domainId = domainId;

        this.fileDomain = file.getFileDomain();
        this.fileUrl = file.getFileUrl();
        this.createdAt = LocalDateTime.now();
        this.creatorId = creatorId;
    }

    public void changeSequence(int sequence, Long updaterId) {
        this.sequence = sequence;
        this.updatedAt = LocalDateTime.now();
        this.updaterId = updaterId;
    }
}
