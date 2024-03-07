package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.NotificationSubCategory;
import com.peoplein.moiming.domain.enums.NotificationTopCategory;
import com.peoplein.moiming.domain.enums.NotificationType;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.peoplein.moiming.domain.enums.MoimMemberState.*;
import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.MoimMemberReqDto.*;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MoimMemberService {

    private final MoimRepository moimRepository;
    private final MoimMemberRepository moimMemberRepository;
    private final NotificationService notificationService;


    // TODO :: 모임원일 경우, 모임원이 아닐경우에 대한 구분
    //         모임원이 아니여도 모임페이지에 어느정도 노출은 있는데 그 구분이 확인되어야 한다
    public List<MoimMember> getActiveMoimMembers(Long moimId, Member curMember) {

        Moim moimOp = moimRepository.findWithMoimMemberAndMemberById(moimId).orElseThrow(
                () -> new MoimingApiException(MOIM_NOT_FOUND)
        );

        return moimOp.getMoimMembers().stream().filter(moimMember -> moimMember.getMemberState().equals(ACTIVE))
                .collect(Collectors.toList());
    }


    // 2. 가입 요청
    public void joinMoim(MoimMemberJoinReqDto requestDto, Member curMember) {

        // 해당 모임이 있는지 확인한다
        Moim moimPs = moimRepository.findWithJoinRuleById(requestDto.getMoimId()).orElseThrow(
                () -> new MoimingApiException(MOIM_NOT_FOUND)
        );

        // 둘 관계가 맺어진 적이 있는지 확인한다
        MoimMember moimMemberPs = moimMemberRepository.findByMemberAndMoimId(curMember.getId(), requestDto.getMoimId()).orElse(null);
        moimPs.judgeMemberJoinByRule(moimMemberPs, curMember);

        // 알림을 발생시킨다
        notificationService.createNotification(NotificationTopCategory.MOIM, NotificationSubCategory.MOIM_JOIN, NotificationType.INFORM
                , moimPs.getCreatorId(), "", "\"" + curMember.getNickname() + "\"님이 모임에 가입하였습니다", moimPs.getId(), null);

    }


    // 3. 모임 나가기 - IBW 전환
    public void leaveMoim(MoimMemberLeaveReqDto requestDto, Member curMember) {

        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(curMember.getId(), requestDto.getMoimId()).orElseThrow(
                () -> new MoimingApiException(MOIM_MEMBER_NOT_FOUND)
        );

        if (moimMember.hasPermissionOfManager()) {
            throw new MoimingApiException(MOIM_LEAVE_FAIL_BY_MANAGER);
        }

        moimMember.changeMemberState(IBW);

        // MEMO :: MoimMember 에서 Moim 에 대한 Lazy Loading 발생 시점 (creatorId 조회)
        // 알림을 발생시킨다
        notificationService.createNotification(NotificationTopCategory.MOIM, NotificationSubCategory.MOIM_IBW, NotificationType.INFORM
                , moimMember.getMoim().getCreatorId(), "", "\"" + curMember.getNickname() + "\"님이 모임을 탈퇴하였습니다", moimMember.getMoim().getId(), null);

    }


    //    강퇴하기 (MANAGER 권한) - IBF 전환, inactiveReason 기입
    //    스스로 강퇴 불가
    public void expelMember(MoimMemberExpelReqDto requestDto, Member curMember) {

        if (requestDto.getExpelMemberId().equals(curMember.getId())) {
            log.error("{}, {}", "스스로를 강퇴하려 합니다, C999", COMMON_INVALID_SITUATION.getErrMsg());
            throw new MoimingApiException(COMMON_INVALID_SITUATION);
        }

        MoimMember requestMoimMember = moimMemberRepository.findByMemberAndMoimId(curMember.getId(), requestDto.getMoimId()).orElseThrow(
                () -> new MoimingApiException(MOIM_MEMBER_NOT_FOUND)
        );

        if (!requestMoimMember.hasPermissionOfManager()) {
            throw new MoimingApiException(MOIM_MEMBER_NOT_AUTHORIZED);
        }

        MoimMember expelMoimMember = moimMemberRepository.findByMemberAndMoimId(requestDto.getExpelMemberId(), requestDto.getMoimId()).orElseThrow(
                () -> {
                    log.error("{}, {}", "모임원이 아닌 유저를 강퇴하려 합니다, C999", COMMON_INVALID_SITUATION.getErrMsg());
                    return new MoimingApiException(COMMON_INVALID_SITUATION);
                }
        );


        expelMoimMember.changeMemberState(IBF);
        expelMoimMember.setInactiveReason(requestDto.getInactiveReason());


        // MEMO :: MoimMember 에서 Moim 에 대한 Lazy Loading 발생 시점 (moimName 조회)
        // 알림을 발생시킨다
        String notiBody = expelMoimMember.getMoim().getMoimName() + "에서 " + requestDto.getInactiveReason() + "의 이유로 탈퇴 처리 되었습니다";
        notificationService.createNotification(NotificationTopCategory.MOIM, NotificationSubCategory.MOIM_IBF, NotificationType.INFORM
                , expelMoimMember.getMember().getId(), "", notiBody, expelMoimMember.getMoim().getId(), null);

    }


    /*
     MoimMember 의 State 을 찾아와서 memberId 와 MoimMember 상태를 Mapping 해둔다
     */
    public Map<Long, MoimMemberState> getMoimMemberStates(Long moimId, Set<Long> memberIds) {

        List<MoimMember> writerMoimMembers = moimMemberRepository.findByMoimIdAndMemberIds(moimId, new ArrayList<>(memberIds));
        Map<Long, MoimMemberState> stateMapper = new HashMap<>();

        for (MoimMember writerMoimMember : writerMoimMembers) {
            stateMapper.put(writerMoimMember.getMember().getId(), writerMoimMember.getMemberState());
        }

        return stateMapper;
    }

}
