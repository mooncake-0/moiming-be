package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.MemberInfo;
import com.peoplein.moiming.domain.MemberMoimLinker;
import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.enums.MemberGender;
import com.peoplein.moiming.domain.enums.MoimMemberStateAction;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.enums.MoimRoleType;
import com.peoplein.moiming.domain.rules.RuleJoin;
import com.peoplein.moiming.model.ResponseModel;
import com.peoplein.moiming.model.dto.domain.MoimMemberInfoDto;
import com.peoplein.moiming.model.dto.domain.MyMoimLinkerDto;
import com.peoplein.moiming.model.dto.request.MoimJoinRequestDto;
import com.peoplein.moiming.model.dto.request.MoimMemberActionRequestDto;
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

@Service
@Transactional
@RequiredArgsConstructor
public class MoimMemberService {

    private final MoimRepository moimRepository;
    private final MemberMoimLinkerRepository memberMoimLinkerRepository;

    private final MemberRepository memberRepository;

    /*
     모임 내 모든 회원 및 상태 조회
     */
    public List<MoimMemberInfoDto> viewMoimMember(Long moimId, Member curMember) {

        List<MemberMoimLinker> memberMoimLinkers = memberMoimLinkerRepository.findWithMemberInfoAndMoimByMoimId(moimId);
        List<MoimMemberInfoDto> moimMemberInfoDto = new ArrayList<>();
        memberMoimLinkers.forEach(mml -> {
            moimMemberInfoDto.add(new MoimMemberInfoDto(
                    mml.getMember().getId(), mml.getMember().getUid()
                    , mml.getMember().getMemberInfo().getMemberName(), mml.getMember().getMemberInfo().getMemberEmail()
                    , mml.getMember().getMemberInfo().getMemberGender(), mml.getMember().getMemberInfo().getMemberPfImg()
                    , mml.getMoimRoleType(), mml.getMemberState()
                    , mml.getCreatedAt(), mml.getUpdatedAt()
            ));
        });
        return moimMemberInfoDto;
    }

    /*
    특정 유저의 모임 가입 요청을 처리한다
    TODO : 다시 요청하는 경우일 수도 있는데 이거에 따른 경우 처리 필요
    */
    public MyMoimLinkerDto requestJoin(MoimJoinRequestDto moimJoinRequestDto, Member curMember) {

        Moim moim = moimRepository.findWithRulesById(moimJoinRequestDto.getMoimId());

        MoimMemberState memberState = MoimMemberState.ACTIVE;

        if (moim.isHasRuleJoin()) { // 가입조건 판별한다
            memberState = checkRuleJoinCondition(moim, curMember);
        }

        MemberMoimLinker memberMoimLinker = MemberMoimLinker.memberJoinMoim(
                curMember, moim, MoimRoleType.NORMAL, memberState
        );

        memberMoimLinkerRepository.save(memberMoimLinker);

        return new MyMoimLinkerDto(
                memberMoimLinker.getMoimRoleType(),
                memberMoimLinker.getMemberState(),
                memberMoimLinker.getCreatedAt(),
                memberMoimLinker.getUpdatedAt()
        );
    }

    /*
     모임 가입조건 판별 함수
     */
    private MoimMemberState checkRuleJoinCondition(Moim moim, Member curMember) {

        RuleJoin ruleJoin = moim.getRuleJoin();
        // MEMO :: 영컨에서 관리되고 있지 않은 curMember 를 영속화하기 위해 다시 조회, join MemberInfo
//                    SecurityContext 에는 MemberInfo 가 Join 되지 않은 Member 를 가지고 있다.
        MemberInfo memberInfo = memberRepository.findMemberAndMemberInfoById(curMember.getId()).getMemberInfo();

        // 1. 생년월일 판별
        if (ruleJoin.getBirthMax() != 0 && ruleJoin.getBirthMin() != 0) { // 판별조건이 있다면
            if (memberInfo.getMemberBirth().getYear() < ruleJoin.getBirthMin()
                    || memberInfo.getMemberBirth().getYear() > ruleJoin.getBirthMax()) {
                return MoimMemberState.WAIT_BY_AGE;
            }
        }

        // 2. 성별 판별
        if (ruleJoin.getGender() != MemberGender.N) { // 판별조건이 있다면
            if (ruleJoin.getGender() != memberInfo.getMemberGender()) {
                return MoimMemberState.WAIT_BY_GENDER;
            }
        }

        if (!ruleJoin.isDupLeaderAvailable() || !ruleJoin.isDupManagerAvailable() || ruleJoin.getMoimMaxCount() > 0) { // 겸직 조건이 하나라도 있거나 최대 모임 갯수 조건이 있음
            // 쿼리를 멤버의 모든 모임 상태를 가져온다
            List<MemberMoimLinker> memberMoimLinkers = memberMoimLinkerRepository.findByMemberId(curMember.getId());

            boolean isMemberAnyLeader = false;
            boolean isMemberAnyManager = false;
            int cntInactiveMoim = 0;

            for (MemberMoimLinker memberMoimLinker : memberMoimLinkers) {
                if (memberMoimLinker.getMoimRoleType().equals(MoimRoleType.LEADER)) {
                    isMemberAnyLeader = true;
                }
                if (memberMoimLinker.getMoimRoleType().equals(MoimRoleType.MANAGER)) {
                    isMemberAnyManager = true;
                }
                if (memberMoimLinker.getMemberState() != MoimMemberState.ACTIVE) {
                    cntInactiveMoim++;
                }
            }

            // 3. 겸직 여부 판별
            if (!ruleJoin.isDupLeaderAvailable()) { // 모임장 겸직 금지인데
                if (isMemberAnyLeader) return MoimMemberState.WAIT_BY_DUP;
            }

            if (!ruleJoin.isDupManagerAvailable()) { // 운영진 겸직 금지인데
                if (isMemberAnyManager) return MoimMemberState.WAIT_BY_DUP;
            }

            // 4. 가입 모임 수 제한
            if (ruleJoin.getMoimMaxCount() <= memberMoimLinkers.size() - cntInactiveMoim) {
                return MoimMemberState.WAIT_BY_MOIM_CNT;
            }
        }

        // 모임 가입 조건 충족시 그냥 가입된다
        return MoimMemberState.ACTIVE;
    }

    /*
     WAIT 상태인 유저의 요청을 처리한다
     */
    public MoimMemberInfoDto decideJoin(MoimMemberActionRequestDto moimMemberActionRequestDto, Member curMember) {

        // 영속화
        MemberMoimLinker memberMoimLinker = memberMoimLinkerRepository.findWithMemberInfoByMemberAndMoimId(moimMemberActionRequestDto.getMemberId(), moimMemberActionRequestDto.getMoimId());
        MoimMemberInfoDto moimMemberInfoDto = null;

        if (moimMemberActionRequestDto.getStateAction().equals(MoimMemberStateAction.PERMIT)) {

            memberMoimLinker.changeMemberState(MoimMemberState.ACTIVE);
            memberMoimLinker.setUpdatedAt(LocalDateTime.now());

            moimMemberInfoDto = new MoimMemberInfoDto(
                    memberMoimLinker.getMember().getId(), memberMoimLinker.getMember().getUid()
                    , memberMoimLinker.getMember().getMemberInfo().getMemberName(), memberMoimLinker.getMember().getMemberInfo().getMemberEmail()
                    , memberMoimLinker.getMember().getMemberInfo().getMemberGender(), memberMoimLinker.getMember().getMemberInfo().getMemberPfImg()
                    , memberMoimLinker.getMoimRoleType(), memberMoimLinker.getMemberState()
                    , memberMoimLinker.getCreatedAt(), memberMoimLinker.getUpdatedAt()
            );

        } else if (moimMemberActionRequestDto.getStateAction().equals(MoimMemberStateAction.DECLINE)) {

            // TODO :: 이 멤버에게 [해당 모임에서 까였다고] 알림을 보내야 한다 (MEMBERINFO 를 JOIN 한 이유)
            memberMoimLinkerRepository.remove(memberMoimLinker);

        } else { // 여기 들어오면 안되는 에러 요청
            // TODO :: ERROR
        }

        return moimMemberInfoDto;
    }

    public MoimMemberInfoDto exitMoim(MoimMemberActionRequestDto moimMemberActionRequestDto, Member curMember) {

        // 영속화
        MemberMoimLinker memberMoimLinker = memberMoimLinkerRepository.findWithMemberInfoByMemberAndMoimId(moimMemberActionRequestDto.getMemberId(), moimMemberActionRequestDto.getMoimId());

        if (moimMemberActionRequestDto.getStateAction().equals(MoimMemberStateAction.IBW)) { // 특정 유저가 모임을 나가겠다고 요청

            // 요청한 유저의 MemberMoimLinker

            memberMoimLinker.changeMemberState(MoimMemberState.IBW);
            memberMoimLinker.setUpdatedAt(LocalDateTime.now());

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

            memberMoimLinker.setUpdatedAt(LocalDateTime.now());

        } else { // 여기 들어오면 안되는 에러 요청
            // TODO :: ERROR
        }

        // 그 Member 의 MoimMemberInfo 를 전달
        MoimMemberInfoDto moimMemberInfoDto = new MoimMemberInfoDto(
                memberMoimLinker.getMember().getId(), memberMoimLinker.getMember().getUid()
                , memberMoimLinker.getMember().getMemberInfo().getMemberName(), memberMoimLinker.getMember().getMemberInfo().getMemberEmail()
                , memberMoimLinker.getMember().getMemberInfo().getMemberGender(), memberMoimLinker.getMember().getMemberInfo().getMemberPfImg()
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
        memberMoimLinker.setUpdatedAt(LocalDateTime.now());

        // 그 Member 의 MoimMemberInfo 를 전달
        return MoimMemberInfoDto.createMemberInfoDto(memberMoimLinker);
    }
}
