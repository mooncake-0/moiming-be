package com.peoplein.moiming.service;


import com.peoplein.moiming.domain.enums.NotificationTopCategory;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberSetting;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.request.MemberSettingReqDto;
import com.peoplein.moiming.repository.jpa.MemberSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.peoplein.moiming.domain.enums.NotificationTopCategory.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.MemberSettingReqDto.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberSettingService {

    private final MemberSettingRepository memberSettingRepository;


    @Transactional(readOnly = true)
    public List<String> getAllowedReceiverFcmTokens(List<Member> receivers, NotificationTopCategory topCategory) {

        if (receivers == null || topCategory == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        List<Long> memberIds = receivers.stream().map(Member::getId).collect(Collectors.toList());
        List<MemberSetting> memberSettings = memberSettingRepository.findByIds(memberIds);

        List<String> targetFcmTokens = new ArrayList<>();

        for (MemberSetting memberSetting : memberSettings) {
            boolean allowed = topCategory.equals(MOIM) ? memberSetting.isMoimActivityNotifyAllowed() : memberSetting.isNoticeNotifyAllowed();
            if (allowed) {
                if (StringUtils.hasText(memberSetting.getMember().getFcmToken())) {
                    targetFcmTokens.add(memberSetting.getMember().getFcmToken());
                }
            }
        }

        return targetFcmTokens;
    }


    @Transactional(readOnly = true)
    public boolean isMemberNotifyAllowed(Member receiver, NotificationTopCategory topCategory) {

        if (receiver == null || topCategory == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        Optional<MemberSetting> memberSettingOp = memberSettingRepository.findById(receiver.getId());
        if (memberSettingOp.isEmpty()) {
            log.error("{}, isMemberFcmAllowed :: {}", this.getClass().getName(), receiver.getId() + " 의 멤버 설정이 존재하지 않습니다");
            return false;
        }

        MemberSetting memberSetting = memberSettingOp.get();
        // Notification 종류에 따라 차단 혹은 통과시킨다
        if (topCategory.equals(MOIM)) {
            return memberSetting.isMoimActivityNotifyAllowed();
        } else if (topCategory.equals(NOTICE)) {
            return memberSetting.isNoticeNotifyAllowed();
        } else {
            return false;
        }
    }


    @Transactional(readOnly = true)
    public MemberSetting getMemberSetting(Member member) {

        if (member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        MemberSetting memberSetting = memberSettingRepository.findById(member.getId()).orElseThrow(() -> {
            log.error("{}, isMemberFcmAllowed :: {}", this.getClass().getName(), member.getId() + " 의 멤버 설정이 존재하지 않습니다");
            return new MoimingApiException(COMMON_INVALID_SITUATION);
        });

        return memberSetting;
    }


    @Transactional
    public MemberSetting updateNotifySetting(MemberSettingUpdateReqDto requestDto, Member member) {

        if (requestDto == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        MemberSetting memberSetting = memberSettingRepository.findById(member.getId()).orElseThrow(() -> {
            log.error("{}, isMemberFcmAllowed :: {}", this.getClass().getName(), member.getId() + " 의 멤버 설정이 존재하지 않습니다");
            return new MoimingApiException(COMMON_INVALID_SITUATION);
        });

        memberSetting.changeNotifySettings(requestDto.getAllowNoticeNotify(), requestDto.getAllowMoimNotify());

        return memberSetting;
    }


}
