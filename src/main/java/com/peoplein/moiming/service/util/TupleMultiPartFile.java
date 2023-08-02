package com.peoplein.moiming.service.util;

import com.peoplein.moiming.domain.FileUpload;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Getter
public class TupleMultiPartFile{
    private final MultipartFile multipartFile;
    private final File file;
    private final FileUpload fileUpload;


    public TupleMultiPartFile(MultipartFile multipartFile, String fileDir, Long ownerKey) {
        this.multipartFile = multipartFile;
        this.fileUpload = FileUpload.createFileUpload(multipartFile.getOriginalFilename(), fileDir, ownerKey);
        this.file = new File(fileUpload.getSavedFileName());
    }
}