package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimDailyCount;
import com.peoplein.moiming.domain.moim.MoimMonthlyCount;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MoimCountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.peoplein.moiming.exception.ExceptionValue.COMMON_INVALID_PARAM;
import static com.peoplein.moiming.exception.ExceptionValue.COMMON_INVALID_SITUATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoimCountService {

    private final MoimCountRepository moimCountRepository;

    public void processMoimCounting(Member member, Moim moim) {

        if (member == null || moim == null) {
            log.error("CLASS {} :: processMoimCounting :: {}", this.getClass().getName(), COMMON_INVALID_PARAM.getErrMsg());
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        // 요청한 모임을 이미 조회한 유저인지 확인
        Optional<MoimDailyCount> dailyOp = moimCountRepository.findDailyByMemberIdAndMoimIdAndCurrentDate(member.getId(), moim.getId());

        if (dailyOp.isEmpty()) {
            moimCountRepository.save(MoimDailyCount.createMoimAccessCount(member, moim));

            Optional<MoimMonthlyCount> monthlyOp = moimCountRepository.findMonthlyByMemberIdAndMoimIdAndCurrentDate(moim.getId());
            // 처음 조회한 유저일 시, monthlyOp 을 증가시킨다
            if (monthlyOp.isEmpty()) {
                moimCountRepository.save(MoimMonthlyCount.createMoimMonthlyCount(moim));
            } else { // 이미 형성되고 있었음
                monthlyOp.get().increaseMonthlyCount();
            }
        } else { // 이미 조회를 한 유저여도, 당일 조회수를 증가시킨다
            dailyOp.get().increaseMemberAccessCount();
        }

    }

}
