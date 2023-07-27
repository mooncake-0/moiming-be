package com.peoplein.moiming.service;

import com.peoplein.moiming.cron.FileDeleteScheduler;
import com.peoplein.moiming.domain.FileUpload;
import com.peoplein.moiming.repository.FileUploadRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FileService {

    private static final String JPG_CONTENT_TYPE = "image/jpeg";
    private static final String PNG_CONTENT_TYPE = "image/png";
    private final Pattern PATTERN_PROPER_SUFFIX;
    private final Pattern PATTERN_FORBIDDEN_PREFIX;
    private final FileUploadRepository fileUploadRepository;
    private final FileDeleteScheduler scheduler;


    @Value("${file.dir}")
    private String fileDir;

    public FileService(FileUploadRepository fileUploadRepository, FileDeleteScheduler scheduler) {
        this.PATTERN_FORBIDDEN_PREFIX = Pattern.compile("^\\.\\.\\/.*");
        this.PATTERN_PROPER_SUFFIX = Pattern.compile(".+.(jpeg|jpg|PNG|png)$");
        this.fileUploadRepository = fileUploadRepository;
        this.scheduler = scheduler;
    }

    // File Invalid 시, RuntimeException 발생. -> 전체 트랜잭션 롤백 유도.
    // 트랜잭션 전파 시, 부모 트랜잭션에서 자동 롤백.
    // 부모 트랜잭션에서 이 메서드 호출 후, 롤백 발생할 수 있음. 따라서 이 녀석은 부모 트랜잭션에서도 가장 마지막에 호출되어야 함.
    @Transactional
    public boolean saveFile(Long ownerKey, List<MultipartFile> files) throws IOException {

        areTheyProper(files);

        final List<TupleMultiPartFile> tupleMultiPartFiles = files.stream()
                .map(multipartFile -> new TupleMultiPartFile(multipartFile, fileDir, ownerKey))
                .collect(Collectors.toList());

        tupleMultiPartFiles.forEach(tupleMultiPartFile -> fileUploadRepository.saveFile(tupleMultiPartFile.getFileUpload()));

        for (TupleMultiPartFile tupleMultiPartFile : tupleMultiPartFiles) {
            final MultipartFile multipartFile = tupleMultiPartFile.getMultipartFile();
            multipartFile.transferTo(tupleMultiPartFile.getFile());
            log.info("디스크에 파일 저장 완료 = {}", tupleMultiPartFile.getFileUpload().getSavedFileName());
        }

        return true;
    }


    @Transactional
    public void removeFile(Long ownerPk) {
        final List<FileUpload> fileUploadByOwnerPk = fileUploadRepository.findFileUploadByOwnerPk(ownerPk);
        // mark deleted by dirty check.
        fileUploadByOwnerPk.forEach(FileUpload::markDeleted);
    }

    private void areTheyProper(List<MultipartFile> files) {
        List<MultipartFile> multipartFiles = files.stream()
                .filter(file -> !hasEmptyFile(file.getOriginalFilename()))
                .filter(file -> hasProperType(file.getContentType()))
                .filter(file -> hasProperTypePerspectiveSuffix(file.getOriginalFilename()))
                .filter(file -> hasProperName(file.getOriginalFilename()))
                .collect(Collectors.toList());

        if (multipartFiles.size() != files.size()) {
            throw new RuntimeException("uploaded file invalid.");
        }
    }

    private boolean hasEmptyFile(String originalFilename) {
        return !StringUtils.hasText(originalFilename);
    }

    private boolean hasProperName(String fileName) {
        return !PATTERN_FORBIDDEN_PREFIX
                .matcher(fileName)
                .matches();
    }

    private boolean hasProperType(String contentType) {
        return (contentType.equals(JPG_CONTENT_TYPE)) ||
                (contentType.equals(PNG_CONTENT_TYPE));
    }

    private boolean hasProperTypePerspectiveSuffix(String fileName) {
        return PATTERN_PROPER_SUFFIX
                .matcher(fileName)
                .matches();
    }

    @Getter
    static class TupleMultiPartFile{
        private final MultipartFile multipartFile;
        private final File file;
        private final FileUpload fileUpload;


        public TupleMultiPartFile(MultipartFile multipartFile, String fileDir, Long ownerKey) {
            this.multipartFile = multipartFile;
            this.fileUpload = FileUpload.createFileUpload(multipartFile.getOriginalFilename(), fileDir, ownerKey);
            this.file = new File(fileUpload.getSavedFileName());
        }
    }
}
