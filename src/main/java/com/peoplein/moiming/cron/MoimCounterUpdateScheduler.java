package com.peoplein.moiming.cron;

import com.peoplein.moiming.domain.FileUpload;
import com.peoplein.moiming.repository.FileUploadRepository;
import com.peoplein.moiming.service.MemberMoimCounterService;
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
public class MoimCounterUpdateScheduler {
    private final MemberMoimCounterService memberMoimCounterService;

//    @Scheduled(cron = "0 */10 * * * *")
    @Scheduled(cron = "*/1 * * * * *")
    public void update() {
        log.info("MoimCounterUpdateScheduler started update.");
        memberMoimCounterService.update();
    }

//    @Scheduled(cron = "0 * */12 * * *")
    @Scheduled(cron = "*/1 * * * * *")
    public void delete() {
        log.info("MoimCounterUpdateScheduler started deleted.");
        memberMoimCounterService.deleteDoesNotExistMoim();
    }
}
