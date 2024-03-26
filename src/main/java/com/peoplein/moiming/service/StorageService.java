package com.peoplein.moiming.service;


import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.peoplein.moiming.domain.file.File;
import com.peoplein.moiming.domain.file.FileDomain;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberInfo;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.jpa.FileJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.UUID;

import static com.peoplein.moiming.exception.ExceptionValue.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final AmazonS3Client amazonS3Client;

    private final FileJpaRepository fileRepository;
    private final MemberRepository memberRepository;


    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;


    @Transactional
    public String uploadMemberPfImg(MultipartFile imgFile, Member member) {

        if (imgFile == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        member = memberRepository.findWithMemberInfoById(member.getId()).orElseThrow(() ->
                new MoimingApiException(COMMON_INVALID_SITUATION) // 정말 이상한 상황이라
        );

        MemberInfo memberInfo = member.getMemberInfo();

        // 기존 정보 삭제 요청 (만약 프로필 이미지가 존재했었다면)
        // 1. 기존 파일 조회 및 삭제
        // 2. amazonS3Client 로 삭제
        if (memberInfo.getPfImgFileId() != null) {
            File originalFile = fileRepository.findById(memberInfo.getPfImgFileId()).orElseThrow(() -> {
                log.error("{}, uploadMemberPfImg :: {}", this.getClass().getName(), "id: [" + memberInfo.getPfImgFileId() + "] 의 파일을 찾을 수 없습니다");
                return new MoimingApiException(STORAGE_FILE_NOT_FOUND);
            });
            fileRepository.remove(originalFile);
            amazonS3Client.deleteObject(bucketName, originalFile.getFileName()); // 현재 삭제 안됨

        }

        String fileName = "user/" + UUID.randomUUID().toString();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(imgFile.getContentType());
        metadata.setContentLength(imgFile.getSize());

        try {
            amazonS3Client.putObject(bucketName, fileName, imgFile.getInputStream(), metadata);
            String resourceUrl = amazonS3Client.getResourceUrl(bucketName, fileName);

            File file = new File(FileDomain.USER, fileName, resourceUrl, imgFile.getContentType());
            fileRepository.save(file);
            memberInfo.changePfImg(file);

            return resourceUrl;

        } catch (IOException exception) {
            log.error("{}, uploadMemberPfImg :: {}", this.getClass().getName(), "InputStream 으로 파일을 읽는 중 에러 발생");
            throw new MoimingApiException(STORAGE_FILE_INPUT_STREAM_ERROR, exception);
        }
    }


    @Transactional
    public void deleteMemberPfImg(Member member) {

        if (member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        member = memberRepository.findWithMemberInfoById(member.getId()).orElseThrow(() ->
                new MoimingApiException(COMMON_INVALID_SITUATION)
        );

        MemberInfo memberInfo = member.getMemberInfo();
        if (memberInfo.getPfImgFileId() == null) { // 이미 없음
            return;
        }

        File originalFile = fileRepository.findById(memberInfo.getPfImgFileId()).orElseThrow(() -> {
            log.error("{}, uploadMemberPfImg :: {}", this.getClass().getName(), "id: [" + memberInfo.getPfImgFileId() + "] 의 파일을 찾을 수 없습니다");
            return new MoimingApiException(STORAGE_FILE_NOT_FOUND);
        });

        amazonS3Client.deleteObject(bucketName, originalFile.getFileName()); // 현재 삭제 안됨
        fileRepository.remove(originalFile);
        memberInfo.deletePfImg();
    }


    // 새로운 이미지 생성에 대한 역할만 수행한다
    @Transactional
    public File uploadMoimImg(MultipartFile imgFile) {

        if (imgFile == null || imgFile.isEmpty()) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        String fileName = "moim/" + UUID.randomUUID().toString();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(imgFile.getContentType());
        metadata.setContentLength(imgFile.getSize());

        try {
            amazonS3Client.putObject(bucketName, fileName, imgFile.getInputStream(), metadata);
            String resourceUrl = amazonS3Client.getResourceUrl(bucketName, fileName);

            File file = new File(FileDomain.MOIM, fileName, resourceUrl, imgFile.getContentType());
            fileRepository.save(file);

            return file;

        } catch (IOException exception) {
            log.error("{}, uploadMoimImg :: {}", this.getClass().getName(), "InputStream 으로 파일을 읽는 중 에러 발생");
            throw new MoimingApiException(STORAGE_FILE_INPUT_STREAM_ERROR, exception);
        }
    }


    // 기존 이미지를 삭제에 대한 요청만 처리한다
    @Transactional
    public void deleteMoimImg(Long imgFileId) {

        if (imgFileId == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        File originalFile = fileRepository.findById(imgFileId).orElseThrow(() -> {
            log.error("{}, uploadMemberPfImg :: {}", this.getClass().getName(), "id: [" + imgFileId + "] 의 파일을 찾을 수 없습니다");
            return new MoimingApiException(STORAGE_FILE_NOT_FOUND);
        });

        amazonS3Client.deleteObject(bucketName, originalFile.getFileName()); // 현재 삭제 안됨
        fileRepository.remove(originalFile);
    }

}
