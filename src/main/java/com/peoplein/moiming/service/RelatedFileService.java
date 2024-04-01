package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.file.File;
import com.peoplein.moiming.domain.file.RelatedFile;
import com.peoplein.moiming.repository.jpa.FileJpaRepository;
import com.peoplein.moiming.repository.jpa.RelatedFileJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatedFileService {

    private final FileJpaRepository fileRepository;
    private final RelatedFileJpaRepository relatedFileRepository;

    // MOIM POST 관련 저장 작업
    public void saveRelatedFiles(Long domainId, List<Long> fileIds) {

        File file = new File(); // 파일을 조회하는대로 RelatedFile 에 만들어서 삽입을 진행
        RelatedFile relatedFile = new RelatedFile(idx, file, domainId);
        relatedFileRepository.save(relatedFile);

    }



}
