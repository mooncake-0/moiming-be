package com.peoplein.moiming.service;

import com.peoplein.moiming.BaseTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FileServiceTest extends BaseTest {

    @Autowired
    FileService fileService;


    @Test
    @DisplayName("success")
    void test1() throws IOException {
        // GIVEN :
        MultipartFile multipartFile1 = new MockMultipartFile("3.jpg", "3.jpg", "image/jpeg", new byte[]{});
        MultipartFile multipartFile2 = new MockMultipartFile("3.PNG", "3.PNG", "image/png", new byte[]{});
        List<MultipartFile> files = List.of(multipartFile1, multipartFile2);
        Long ownerKey = 1L;

        // WHEN :
        boolean result = fileService.saveFile(ownerKey, files);

        // THEN
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("fail because of wrong inputName")
    void test2() throws IOException {
        // GIVEN :
        MultipartFile multipartFile1 = new MockMultipartFile("3.img", "3.img", "image/jpeg", new byte[]{});
        MultipartFile multipartFile2 = new MockMultipartFile("3.PNG", "3.PNG", "image/png", new byte[]{});
        List<MultipartFile> files = List.of(multipartFile1, multipartFile2);
        Long ownerKey = 1L;

        // WHEN + THEN:
        assertThatThrownBy(() -> fileService.saveFile(ownerKey, files))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("fail because of wrong contentType")
    void test3() {
        // GIVEN :
        MultipartFile multipartFile1 = new MockMultipartFile("3.jpg", "3.jpg", "json", new byte[]{});
        MultipartFile multipartFile2 = new MockMultipartFile("3.PNG", "3.PNG", "image/png", new byte[]{});
        List<MultipartFile> files = List.of(multipartFile1, multipartFile2);
        Long ownerKey = 1L;

        // WHEN + THEN:
        assertThatThrownBy(() -> fileService.saveFile(ownerKey, files))
                .isInstanceOf(RuntimeException.class);

    }

}