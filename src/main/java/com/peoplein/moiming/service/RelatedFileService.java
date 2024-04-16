package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.file.File;
import com.peoplein.moiming.domain.file.FileDomain;
import com.peoplein.moiming.domain.file.RelatedFile;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MoimPostRepository;
import com.peoplein.moiming.repository.jpa.FileJpaRepository;
import com.peoplein.moiming.repository.jpa.RelatedFileJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.peoplein.moiming.exception.ExceptionValue.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RelatedFileService {

    private final FileJpaRepository fileRepository;
    private final RelatedFileJpaRepository relatedFileRepository;

    @Transactional(readOnly = true)
    public List<RelatedFile> getRelatedFilesBySeq(FileDomain fileDomain, Long domainId) {

        if (fileDomain == null || domainId == null) { // LIST 가 비는건 ㄱㅊ은데 NULL 은 안됨
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        return relatedFileRepository.findWithFileByDomainIdOrderBySeq(domainId, fileDomain);
    }


    // MOIM POST 관련 저장 작업
    @Transactional
    public void saveRelatedFiles(Long domainId, List<Long> fileIds, Long memberId) {

        if (domainId == null || fileIds.isEmpty()) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        List<File> files = fileRepository.findByIds(fileIds);
        List<File> filesOrderByUserReq = validateAndOrderByUserReq(files, fileIds);

        if (fileIds.size() != filesOrderByUserReq.size()) {
            throw new MoimingApiException(COMMON_INVALID_SITUATION);
        }

        for (int idx = 0; idx < filesOrderByUserReq.size(); idx++) {
            RelatedFile relatedFile = new RelatedFile(idx, filesOrderByUserReq.get(idx), domainId, memberId);
            relatedFileRepository.save(relatedFile);
        }
    }


    // Domain 사진 관련 Update 및 삭제를 하고, Storage Service 로 삭제할 파일들 이름을 반환한다
    @Transactional
    public List<String> updateDomainRelatedFilesAndReturnRemovalNames(FileDomain fileDomain, Long domainId, List<Long> fileIds, Long memberId) {

        if (fileDomain == null || domainId == null || fileIds == null) { // LIST 가 비는건 ㄱㅊ은데 NULL 은 안됨
            throw new MoimingApiException(COMMON_INVALID_PARAM);

        }
        // fileDomain 에 따라서 소유하고 있는 List 와 새로 요청받은 List 를 비교한다
        List<RelatedFile> preRelatedFiles = relatedFileRepository.findWithFileByDomainId(domainId, fileDomain);
        Map<Long, RelatedFile> preMap = new HashMap<>();
        for (RelatedFile rf : preRelatedFiles) {
            preMap.put(rf.getFile().getId(), rf);
        }

        // 일단 새로 준비된 파일들을 준비해온다
        List<File> files = fileRepository.findByIds(fileIds);
        List<File> filesOrderByUserReq = validateAndOrderByUserReq(files, fileIds);

        // 새 요청에 있는 것들은 추가 대상 및 idx 최신화
        for (int idx = 0; idx < filesOrderByUserReq.size(); idx++) {
            File updateFile = filesOrderByUserReq.get(idx);
            if (preMap.containsKey(updateFile.getId())) { // 있다면 업데이트가 필요한 파일
                preMap.get(updateFile.getId()).changeSequence(idx, memberId);
                preMap.remove(updateFile.getId()); // 업데이트 되었으니 삭제한다
            } else {  // 없다면 새로 저장되는 파일
                RelatedFile relatedFile = new RelatedFile(idx, updateFile, domainId, memberId);
                relatedFileRepository.save(relatedFile);
            }
        }

        // MAP 에 남아있는 것들은 삭제 대상이다 (File / RelatedFile 둘다)
        Set<Long> rmFileIds = preMap.keySet();
        relatedFileRepository.removeByFileIds(rmFileIds);

        List<String> rmFileNames = preMap.values().stream().map(rf -> rf.getFile().getFileName()).collect(Collectors.toList());
        return rmFileNames;
    }


    private List<File> validateAndOrderByUserReq(List<File> unorderedFiles, List<Long> userReq) {

        if (unorderedFiles.size() != userReq.size()) {
            StringBuilder queriedIds = new StringBuilder();
            StringBuilder reqIds = new StringBuilder();
            for (File unorderedFile : unorderedFiles) {
                queriedIds.append(unorderedFile.getId()).append(".");
            }
            for (Long reqId : userReq) {
                reqIds.append(reqId).append(".");
            }
            log.info("{} , validateAndOrderByUserReq :: {}", this.getClass().getName(), "조회하지 못한 File Id 전달됨, Queried: " + queriedIds + ", Requested: " + reqIds);
            throw new MoimingApiException(STORAGE_FILE_NOT_FOUND_EXIST);
        }

        List<File> filesOrderByUserReq = new ArrayList<>();
        // 위 파일들은 fileIds 의 순서대로 정렬되어야 함 (유저의 요청대로)
        for (Long orderedId : userReq) {
            for (File notOrderedFile : unorderedFiles) {
                if (notOrderedFile.getId().equals(orderedId)) {
                    filesOrderByUserReq.add(notOrderedFile);
                }
            }
        }
        return filesOrderByUserReq;
    }
}
