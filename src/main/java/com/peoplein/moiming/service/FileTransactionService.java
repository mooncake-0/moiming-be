package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.FileUpload;
import com.peoplein.moiming.repository.FileUploadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class FileTransactionService {

    private final FileUploadRepository fileUploadRepository;

    public void saveFileToDB(List<FileService.TupleMultiPartFile> tupleMultiPartFiles) {
        tupleMultiPartFiles.forEach(tupleMultiPartFile -> fileUploadRepository.saveFile(tupleMultiPartFile.getFileUpload()));
    }

    public void removeFile(Long ownerPk) {
        final List<FileUpload> fileUploadByOwnerPk = fileUploadRepository.findFileUploadByOwnerPk(ownerPk);
        // mark deleted by dirty check.
        fileUploadByOwnerPk.forEach(FileUpload::markDeleted);
    }
}
