package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.FileUpload;

import java.util.List;

public interface FileUploadRepository {

    Long saveFile(FileUpload fileUpload);
    List<FileUpload> findFileUploadByOwnerPk(Long pk);

    void removeFiles(List<Long> fileIds);

    List<FileUpload> findAllDeletedMarkedFile();


}
