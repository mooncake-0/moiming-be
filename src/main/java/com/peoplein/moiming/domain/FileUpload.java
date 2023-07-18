package com.peoplein.moiming.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileUpload extends BaseEntity{

    @Id
    @GeneratedValue
    @Column(name = "file_id")
    private Long id;

    private FileUpload(String originalFileName, String savedFileName) {
        this.originalFileName = originalFileName;
        this.savedFileName = savedFileName;
    }

    @Column(unique = true)
    private String savedFileName;
    private String originalFileName;


    public static FileUpload createFileUpload(String originalFileName, String fileDir) {
        String uuid = UUID.randomUUID().toString();
        String uploadFileName = String.format("%s/%s-%s", fileDir, uuid, originalFileName);
        return new FileUpload(originalFileName, uploadFileName);
    }
}