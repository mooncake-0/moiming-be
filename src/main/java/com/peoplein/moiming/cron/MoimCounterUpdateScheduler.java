package com.peoplein.moiming.cron;

import com.peoplein.moiming.service.MemberMoimCounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MoimCounterUpdateScheduler {
    private final MemberMoimCounterService memberMoimCounterService;

    @Scheduled(cron = "0 */10 * * * *")
    public void update() {
        log.info("MoimCounterUpdateScheduler started update.");
        memberMoimCounterService.update();
    }

    @Scheduled(cron = "0 * */12 * * *")
    public void delete() {
        log.info("MoimCounterUpdateScheduler started deleted.");
        memberMoimCounterService.deleteDoesNotExistMoim();
    }
}
