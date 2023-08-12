package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MemberInfo;
import com.peoplein.moiming.domain.MemberMoimLinker;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.enums.*;
import com.peoplein.moiming.domain.rules.RuleJoin;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.domain.MoimMemberInfoDto;
import com.peoplein.moiming.model.dto.domain.MyMoimLinkerDto;
import com.peoplein.moiming.model.dto.domain.NotificationDto;
import com.peoplein.moiming.model.dto.request.MoimJoinRequestDto;
import com.peoplein.moiming.model.dto.request.MoimMemberActionRequestDto;
import com.peoplein.moiming.model.inner.NotificationInput;
import com.peoplein.moiming.repository.MemberMoimLinkerRepository;
import com.peoplein.moiming.repository.MemberRepository;
import com.peoplein.moiming.repository.MoimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MoimMemberService {

    private final MoimRepository moimRepository;
    private final MemberMoimLinkerRepository memberMoimLinkerRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    /*
     모임 내 모든 회원 및 상태 조회
     */
    public List<MoimMemberInfoDto> viewMoimMember(Long moimId, Member curMember) {
        List<MemberMoimLinker> memberMoimLinkers = memberMoimLinkerRepository.findWithMemberInfoAndMoimByMoimId(moimId);
        return getMoimMemberInfos(memberMoimLinkers);
    }

    /**
     * 특정 유저의 모임 가입 요청을 처리한다
     *
     * @param moimJoinRequestDto : Moim 가입 요청 관련 데이터
     * @param curMember          : 현재 요청하는 Member 정보 (Security에서 확인)
     * @return - null : 재가입 불가능하게 강퇴당한 경우
     * - MyMoimLinkerDto : 재가입이 아닌 경우.
     */
    public MemberMoimLinker requestJoin(MoimJoinRequestDto moimJoinRequestDto, Member curMember) {

        // default
        MoimMemberState memberState = MoimMemberState.ACTIVE;

        // moimRepository.findWithRulesById()로 하면, Rule이 없는 경우 null이 반환됨.
        Moim moim = moimRepository.findById(moimJoinRequestDto.getMoimId());
        List<MemberMoimLinker> memberMoimLinkers = memberMoimLinkerRepository.findByMemberId(curMember.getId());
        Optional<MemberMoimLinker> previousMemberMoimLinker = memberMoimLinkers.stream().filter(existMemberMoimLinker -> existMemberMoimLinker.getMoim().getId().equals(moim.getId())).findFirst();

        // 재가입 요청
        if (previousMemberMoimLinker.isPresent()) {
            MemberMoimLinker previousLinker = previousMemberMoimLinker.get();
            if (previousLinker.canRejoin()) {
                memberState = MoimMemberState.WAIT_BY_BAN;
            } else {
                return null; // 재가입 불가능할 경우, null값 반환.
            }
        } else {
            if (moim.isHasRuleJoin()) { // 가입조건 판별한다
                memberState = moim.checkRuleJoinCondition(curMember.getMemberInfo(), memberMoimLinkers);
            }
        }

        MemberMoimLinker memberMoimLinker = MemberMoimLinker.processRequestJoin(curMember, moim, memberState, previousMemberMoimLinker);
        if (memberMoimLinker.shouldPersist()) {
            memberMoimLinkerRepository.save(memberMoimLinker);
        }

        return memberMoimLinker;
    }


    /*
     WAIT 상태인 유저의 요청을 처리한다
     */
    public MemberMoimLinker decideJoin(MoimMemberActionRequestDto moimMemberActionRequestDto, Member curMember) {
        MemberMoimLinker memberMoimLinker = memberMoimLinkerRepository.findWithMemberInfoByMemberAndMoimId(moimMemberActionRequestDto.getMemberId(), moimMemberActionRequestDto.getMoimId());
        memberMoimLinker.judgeJoin(moimMemberActionRequestDto.getStateAction());

        NotificationDomainCategory notificationDomainCategory = NotificationDomainCategory.DEFAULT;

        // UPDATE 되었을 테니, 정보를 통해 알림을 진행한다
        if (memberMoimLinker.getMemberState().equals(MoimMemberState.DECLINE)) { // 거절
            notificationDomainCategory = NotificationDomainCategory.MOIM_DECLINE_MEMBER;
        } else if (memberMoimLinker.getMemberState().equals(MoimMemberState.ACTIVE)) { // 수락
            notificationDomainCategory = NotificationDomainCategory.MOIM_NEW_MEMBER;
        } else {
            throw new RuntimeException("ERROR : 해당 요청으로는 [" + memberMoimLinker.getMemberState() + "] 상태로 반환될 수 없습니다");
        }

        NotificationInput notificationInput = new NotificationInput(curMember.getId(), memberMoimLinker.getMoim().getId()
                , NotificationDomain.MOIM, notificationDomainCategory);

        // 대상자에게 알림을 보낸다
        notificationService.createNotification(notificationInput, memberMoimLinker.getMember());


        return memberMoimLinker;
    }

    public MoimMemberInfoDto exitMoim(MoimMemberActionRequestDto moimMemberActionRequestDto, Member curMember) {

        // 영속화
        MemberMoimLinker memberMoimLinker = memberMoimLinkerRepository.findWithMemberInfoByMemberAndMoimId(moimMemberActionRequestDto.getMemberId(), moimMemberActionRequestDto.getMoimId());

        if (moimMemberActionRequestDto.getStateAction().equals(MoimMemberStateAction.IBW)) { // 특정 유저가 모임을 나가겠다고 요청

            // 요청한 유저의 MemberMoimLinker

            memberMoimLinker.changeMemberState(MoimMemberState.IBW);
//            memberMoimLinker.setUpdatedAt(LocalDateTime.now());

            // TODO : MoimRoleType 이 Normal 일 겨우만 수월하게 진행?

        } else if (moimMemberActionRequestDto.getStateAction().equals(MoimMemberStateAction.IBF)
                || moimMemberActionRequestDto.getStateAction().equals(MoimMemberStateAction.IBF_BY_VIOLATION)
                || moimMemberActionRequestDto.getStateAction().equals(MoimMemberStateAction.IBF_BY_NO_GREETING)
        ) {
            // 대상 유저의 MemberMoimLinker 임

            // 1. rejoin 여부 판별
            if (moimMemberActionRequestDto.isBanRejoin()) {
                memberMoimLinker.setBanRejoin(true);
            }
            // 2. 강퇴 사유 기록
            if (StringUtils.hasText(moimMemberActionRequestDto.getInactiveReason())) {
                memberMoimLinker.setInactiveReason(moimMemberActionRequestDto.getInactiveReason());
            }

            if (moimMemberActionRequestDto.getStateAction().equals(MoimMemberStateAction.IBF_BY_NO_GREETING)) {
                memberMoimLinker.changeMemberState(MoimMemberState.IBF_BY_NO_GREETING);
            } else if (moimMemberActionRequestDto.getStateAction().equals(MoimMemberStateAction.IBF_BY_VIOLATION)) {
                memberMoimLinker.changeMemberState(MoimMemberState.IBF_BY_VIOLATION);
            } else {  // IBF
                memberMoimLinker.changeMemberState(MoimMemberState.IBF);
            }

//            memberMoimLinker.setUpdatedAt(LocalDateTime.now());

        } else { // 여기 들어오면 안되는 에러 요청
            // TODO :: ERROR
        }

        // 그 Member 의 MoimMemberInfo 를 전달
        MoimMemberInfoDto moimMemberInfoDto = new MoimMemberInfoDto(
                memberMoimLinker.getMember().getId()
                , memberMoimLinker.getMember().getMemberInfo().getMemberName(), memberMoimLinker.getMember().getMemberEmail()
                , memberMoimLinker.getMember().getMemberInfo().getMemberGender()
                , memberMoimLinker.getMoimRoleType(), memberMoimLinker.getMemberState()
                , memberMoimLinker.getCreatedAt(), memberMoimLinker.getUpdatedAt()
        );

        return moimMemberInfoDto;
    }


    public MoimMemberInfoDto changeRole(MoimMemberActionRequestDto moimMemberActionRequestDto, Member curMember) {

        // 영속화
        MemberMoimLinker memberMoimLinker = memberMoimLinkerRepository.findWithMemberInfoByMemberAndMoimId(moimMemberActionRequestDto.getMemberId(), moimMemberActionRequestDto.getMoimId());

        // 모임의 역할 변경된다
        memberMoimLinker.setMoimRoleType(moimMemberActionRequestDto.getRoleAction());
//        memberMoimLinker.setUpdatedAt(LocalDateTime.now());

        // 그 Member 의 MoimMemberInfo 를 전달
        return MoimMemberInfoDto.createMemberInfoDto(memberMoimLinker);
    }

    private List<MoimMemberInfoDto> getMoimMemberInfos(List<MemberMoimLinker> memberMoimLinkers) {
        return memberMoimLinkers.stream()
                .map(MoimMemberInfoDto::new)
                .collect(Collectors.toList());
    }
}