package com.peoplein.moiming.service.util;

import com.peoplein.moiming.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class LocalFilePersistor implements FilePersistor{
    @Override
    public boolean persistFiles(List<TupleMultiPartFile> files){

        for (TupleMultiPartFile tupleMultiPartFile : files) {
            final MultipartFile multipartFile = tupleMultiPartFile.getMultipartFile();

            try {
                multipartFile.transferTo(tupleMultiPartFile.getFile());
            } catch (IOException e) {
                log.error("file 저장 실패 = {}", multipartFile.getOriginalFilename());
                return false;
            }

            log.info("디스크에 파일 저장 완료 = {}", tupleMultiPartFile.getFileUpload().getSavedFileName());
        }
        return true;
    }
}
