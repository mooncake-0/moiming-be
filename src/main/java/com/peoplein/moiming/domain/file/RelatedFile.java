package com.peoplein.moiming.domain.file;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/*
 Report 와 Post 처럼, 여러개의 파일을 가지고 있는 Entity 들을 위함
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "file")
public class RelatedFile {

    @Id
    @Column(name = "related_file_id")
    @GeneratedValue
    private Long id;

    private int index;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    private Long domainId;

    private FileDomain fileDomain;

    private String fileUrl;


    public RelatedFile(int index, File file, Long domainId) {

        this.index = index;
        this.file = file;
        this.domainId = domainId;

        this.fileDomain = file.getFileDomain();
        this.fileUrl = file.getFileUrl();
    }

    public void changeIndex(int index) {
        this.index = index;
    }
}
