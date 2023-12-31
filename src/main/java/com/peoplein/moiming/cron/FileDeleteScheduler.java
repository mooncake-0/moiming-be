package com.peoplein.moiming.cron;

import com.peoplein.moiming.domain.FileUpload;
import com.peoplein.moiming.repository.FileUploadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileDeleteScheduler {
    private final FileUploadRepository fileUploadRepository;

    @Transactional
    public void delete() {
        log.info("FileDeletedScheduler Called");
        List<FileUpload> allDeletedMarkedFile = fileUploadRepository.findAllDeletedMarkedFile();
        List<String> filePath = allDeletedMarkedFile.stream()
                .map(FileUpload::getSavedFileName)
                .collect(Collectors.toList());

        deleteFiles(filePath);

        List<Long> deletedFieldId = allDeletedMarkedFile.stream()
                .map(FileUpload::getId)
                .collect(Collectors.toList());

        fileUploadRepository.removeFiles(deletedFieldId);
        log.info("FileDeletedScheduler Called. delete file count = {}", deletedFieldId.size());
    }

    private void deleteFiles(List<String> filePath) {
        filePath.forEach(this::deleteFile);
    }

    private void deleteFile(String filePath) {
        Path path = Paths.get(filePath);
        try {
            Files.delete(path);
        } catch (IOException e) {
            log.warn("fail to delete this file {}. it may be delete already", filePath);
            throw new RuntimeException(e);
        }
    }
}
