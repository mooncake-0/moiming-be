package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MemberRepository;
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

    private final MemberRepository memberRepository;
    private final MoimRepository moimRepository;
    private final MoimMemberRepository moimMemberRepository;
    private final NotificationService notificationService;


    // TODO :: curMember 의 권한에 따른 분리는 Client 단에서 구분?
    @Transactional(readOnly = true)
    public List<MoimMember> getActiveMoimMembers(Long moimId, Member curMember) {

        if (moimId == null) { // 당장은 Member 가 사용되지 않음
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        List<MoimMember> moimMembers = moimMemberRepository.findActiveWithMemberByMoimId(moimId, true);

        return moimMembers;
    }


    // 2. 가입 요청
    public void joinMoim(MoimMemberJoinReqDto requestDto, Member curMember) {

        curMember.isMemberInfoRegistered();

        // 해당 모임이 있는지 확인한다
        Moim moimPs = moimRepository.findWithJoinRuleById(requestDto.getMoimId()).orElseThrow(
                () -> new MoimingApiException(MOIM_NOT_FOUND)
        );

        // 둘 관계가 맺어진 적이 있는지 확인한다
        MoimMember moimMemberPs = moimMemberRepository.findByMemberAndMoimId(curMember.getId(), requestDto.getMoimId()).orElse(null);

        // moimPs 에 Join Rule 이 있고, moimMemberPs 가 존재하지 않는다면 curMember 에 판단이 들어갈 것이므로, 영속화시켜줘야 한다 (Security 에서 온 객체)
        if(moimPs.getMoimJoinRule() != null && moimMemberPs == null) {
            curMember = persistSecurityMember(curMember.getId());
        }

        moimPs.judgeMemberJoinByRule(moimMemberPs, curMember);

        // 알람 형성을 위해 모임의 Manager 를 조회한다
        Member curManager = memberRepository.findById(moimPs.getCurManagerId()).orElseThrow(() ->
                new MoimingApiException(MEMBER_NOT_FOUND)
        );

        // 알림을 발생시킨다
        notificationService.createNotification(NotificationTopCategory.MOIM, NotificationSubCategory.MOIM_JOIN, NotificationType.INFORM
                , curManager, "", "\"" + curMember.getNickname() + "\"님이 모임에 가입하였습니다", moimPs.getId(), null);

    }


    // 3. 모임 나가기 - IBW 전환
    // basic flow - 모임원이 나가면서 상태가 IBW 로 전환된다. 모임의 현재 모임원은 -1 이 된다
    // alternative flow 1 - 모임장이 나감을 시도한다. 모임장이 나가는데 모임원이 남아있다면, 남은 모임원 중 가장 오래 있었던 모임원에게 모임장을 위임한다.
    //                    - 나가는 모임장은 MANAGER 권한을 가지고 있었던 것으로 기록되지만, ACTIVE 가 IBW 로 변환된다
    // alternative flow 2 - 모임장이 나감을 시도한다. 모임장이 나가는데 모임원이 없다면, 모임은 삭제처리된다. 모임의 삭제된 시각을 기록한다
    // exceptional flow - 로직상 발생할 수 있는 예외 사항은 없다
    public void leaveMoim(MoimMemberLeaveReqDto requestDto, Member curMember) {

        curMember.isMemberInfoRegistered();

        // 모임 나가기 요청자를 확인한다, each flow 에 Moim 정보들이 활용되므로 fetch join 해서 가져온다
        MoimMember moimMember = moimMemberRepository.findWithMoimByMemberAndMoimId(curMember.getId(), requestDto.getMoimId()).orElseThrow(
                () -> new MoimingApiException(MOIM_MEMBER_NOT_FOUND)
        );

        Moim moim = moimMember.getMoim();

        if (!moimMember.hasActivePermission()) { // ACTIVE 하지 않으면 수행할 수 없음
            throw new MoimingApiException(MOIM_MEMBER_NOT_AUTHORIZED);
        }

        // 만약 MANAGER 가 요청한 것이라면, alternative flow 가 실행된다
        if (moimMember.hasPermissionOfManager()) {
            if (moim.getCurMemberCount() == 1) { // Moim 삭제 진행
                // 후속으로 -1 과 IBW 변환은 진행되므로, moim 삭제 정보만 기록한다
                moim.markDeletedTime();

            } else { // 모임장이 나가며, 모임장 승계가 진행된다
                delegateMoimManager(moim);
            }
        }

        moimMember.changeMemberState(IBW);  // 나가려는 모임원의 상태를 IBW 로 기록, 모임원 -1 처리가 진행된다

        // 알람 형성을 위해 모임의 Manager 를 조회한다
        Member curManager = memberRepository.findById(moim.getCurManagerId()).orElseThrow(() ->
                new MoimingApiException(MEMBER_NOT_FOUND)
        );

        // 모임장에게 알림을 발생시킨다
        notificationService.createNotification(NotificationTopCategory.MOIM, NotificationSubCategory.MOIM_IBW, NotificationType.INFORM
                , curManager, "", "\"" + curMember.getNickname() + "\"님이 모임을 탈퇴하였습니다", moimMember.getMoim().getId(), null);

    }


    //    강퇴하기 (MANAGER 권한) - IBF 전환, inactiveReason 기입
    //    스스로 강퇴 불가
    public void expelMember(MoimMemberExpelReqDto requestDto, Member curMember) {

        if (requestDto.getExpelMemberId().equals(curMember.getId())) {
            log.error("{}, {}", "스스로를 강퇴하려 합니다, C999", COMMON_INVALID_SITUATION.getErrMsg());
            throw new MoimingApiException(COMMON_INVALID_SITUATION);
        }

        curMember.isMemberInfoRegistered();

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

        Member expelMember = memberRepository.findById(requestDto.getExpelMemberId()).orElseThrow(() ->
                new MoimingApiException(MEMBER_NOT_FOUND)
        );

        // 알림을 발생시킨다
        String notiBody = expelMoimMember.getMoim().getMoimName() + "에서 " + requestDto.getInactiveReason() + "의 이유로 탈퇴 처리 되었습니다";
        notificationService.createNotification(NotificationTopCategory.MOIM, NotificationSubCategory.MOIM_IBF, NotificationType.INFORM
                , expelMember, "", notiBody, expelMoimMember.getMoim().getId(), null);

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


    // 모임장 승계 로직 - 가장 오래된 멤버를 찾아서 모임장 권한을 위임한다
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
        Member curManager = memberRepository.findById(longestMoimMember.getMember().getId()).orElseThrow(() ->
                new MoimingApiException(MEMBER_NOT_FOUND)
        );

        // 4. 모임장 승계 알림을 전송한다
        String body = "기존 모임장의 탈퇴로, " + moim.getMoimName() + " 모임의 모임장으로 위임되셨습니다";
        notificationService.createNotification(NotificationTopCategory.MOIM, NotificationSubCategory.MOIM_MANAGER_DELEGATE, NotificationType.INFORM
                , curManager, "", body, moim.getId(), null);

    }


    private Member persistSecurityMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> {
            log.error("{}, persistSecurityMember :: {}", this.getClass().getName(), "Member 영속화 중 Id [" + memberId + "] 조회 불가 예외 발생");
            return new MoimingApiException(MEMBER_NOT_FOUND);
        });
    }

}
