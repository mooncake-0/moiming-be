package com.peoplein.moiming.service.shell;

import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.repository.MoimRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Getter
@RequiredArgsConstructor
public class NotificationServiceShell {

    private final MoimRepository moimRepository;
    private Moim moim;

    public void initMoim(Long moimId) {

        moim = moimRepository.findById(moimId).orElseThrow();

    }
}
