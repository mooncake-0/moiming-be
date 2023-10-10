package com.peoplein.moiming.event;

import com.peoplein.moiming.service.MemberMoimCounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Async
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberMoimCounterEventListener {

    private final MemberMoimCounterService memberMoimCounterService;

    @EventListener
    public void handleEvent(MemberMoimCounterEvent event) {
        log.info("event execute = {}", event);
        memberMoimCounterService.create(event.getMemberId(), event.getMoimId(), LocalDate.now());
    }


}
