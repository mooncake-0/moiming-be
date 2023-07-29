package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.FileUpload;
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
    private final FileTransactionService fileTransactionService;


    @Value("${file.dir}")
    private String fileDir;

    public FileService(FileTransactionService fileTransactionService) {
        this.PATTERN_FORBIDDEN_PREFIX = Pattern.compile("^\\.\\.\\/.*");
        this.PATTERN_PROPER_SUFFIX = Pattern.compile(".+.(jpeg|jpg|PNG|png)$");
        this.fileTransactionService = fileTransactionService;
    }


    // 사용하는 쪽에서는 어떻게 할것인가?
    // 1. 트랜잭션 없는 구간에서 saveFile()을 호출해야함.
    // 2. 그리고 먼저 호출해야함.
    public boolean saveFile(Long ownerKey, List<MultipartFile> files) throws IOException {

        areTheyProper(files);

        final List<TupleMultiPartFile> tupleMultiPartFiles = files.stream()
                .map(multipartFile -> new TupleMultiPartFile(multipartFile, fileDir, ownerKey))
                .collect(Collectors.toList());

        // 파일 저장 후 DB에 경로 저장.
        for (TupleMultiPartFile tupleMultiPartFile : tupleMultiPartFiles) {
            final MultipartFile multipartFile = tupleMultiPartFile.getMultipartFile();
            multipartFile.transferTo(tupleMultiPartFile.getFile());
            log.info("디스크에 파일 저장 완료 = {}", tupleMultiPartFile.getFileUpload().getSavedFileName());
        }

        // 트랜잭션 시작 + 종료
        saveFileToDB(tupleMultiPartFiles);
        return true;
    }


    @Transactional
    protected void saveFileToDB(List<TupleMultiPartFile> tupleMultiPartFiles) {
        fileTransactionService.saveFileToDB(tupleMultiPartFiles);
    }

    @Transactional
    public void removeFile(Long ownerPk) {
        fileTransactionService.removeFile(ownerPk);

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
