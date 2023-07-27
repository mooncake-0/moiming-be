package com.peoplein.moiming.controller;

import com.peoplein.moiming.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class FileTestController {

    private final FileService service;

    @PostMapping("/test-file/up")
    public String uploadFile(@RequestParam(name = "uploadFile") List<MultipartFile> file, Long id) throws IOException {
        if (file != null) {
            log.info("save file. id = {}, size = {}", id, file.size());
            service.saveFile(id, file);
        }
        return "ok";
    }

    @PostMapping("/test-file/del")
    public String deleteFile(Long id) {
        if (id != null) {
            service.removeFile(id);
        }
        return "ok";
    }

}
