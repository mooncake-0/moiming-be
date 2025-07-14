package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.enums.NotificationSubCategory;
import com.peoplein.moiming.domain.enums.NotificationTopCategory;
import com.peoplein.moiming.domain.enums.NotificationType;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.MoimMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.peoplein.moiming.exception.ExceptionValue.MEMBER_NOT_FOUND;
import static com.peoplein.moiming.exception.ExceptionValue.MOIM_MEMBER_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class MemberServiceMediator {

    private final MoimMemberRepository moimMemberRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    // MMS 와 동일한 로직.
    public void delegateMoimManager(Moim moim) {

        // persisted 된 모임 전달
        // 해야할 일 - delegate 만 해주면 된다 (권한이 된 곳에서만 사용될 것)
        // 1. 가장 오래된 MoimMember 를 판단한다 (ACTIVE & NORMAL)
        MoimMember longestMoimMember = moimMemberRepository.findLongestNormalActiveMemberByMoimId(moim.getId()).orElseThrow(() ->
                new MoimingApiException(MOIM_MEMBER_NOT_FOUND)
        );

        // 2. 해당 유저에게 MANAGER 권한을 준다
        longestMoimMember.changeMoimMemberRoleType(MoimMemberRoleType.MANAGER);

        // 3. 해당 모임의 모임장 ID 를 지정한다
        moim.changeCurManagerId(longestMoimMember.getMember().getId());

        // Moim 은 이미 조회됨, 알람 형성을 위해 curManager 를 불러온다
        // MoimMember 의 FK 임
        Member longestMember = memberRepository.findById(longestMoimMember.getMember().getId()).orElseThrow(() ->
                new MoimingApiException(MEMBER_NOT_FOUND)
        );

        // 4. 모임장 승계 알림을 전송한다
        String body = "기존 모임장의 탈퇴로, " + moim.getMoimName() + " 모임의 모임장으로 위임되셨습니다";
        notificationService.createNotification(NotificationTopCategory.MOIM, NotificationSubCategory.MOIM_MANAGER_DELEGATE, NotificationType.INFORM
                , longestMember, "", body, moim.getId(), null);

    }

}
