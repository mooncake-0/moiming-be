package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.request.MoimMemberReqDto;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.peoplein.moiming.model.dto.request.MoimMemberReqDto.*;


@Service
@Transactional
@RequiredArgsConstructor
public class MoimMemberService {

    private final MoimRepository moimRepository;
    private final MoimMemberRepository moimMemberRepository;

    public List<MoimMember> getMoimMembers(Long moimId, Member curMember) {

        Moim moimOp = moimRepository.findWithMoimMembersById(moimId).orElseThrow(
                () -> new MoimingApiException("모임을 찾을 수 없습니다")
        );

        return moimOp.getMoimMembers();
    }



    // 2. 가입 요청 (Rule Join 판별 - Front 에서 걸러줄테지만 /Rule Join + 정원확인 - 모두 도메인 단에서 확인)
    public MoimMember joinMoim(MoimMemberJoinReqDto requestDto, Member curMember) {

        // 해당 모임이 있는지 확인한다
        Moim moimPs = moimRepository.findWithJoinRuleById(requestDto.getMoimId()).orElseThrow(
                () -> new MoimingApiException("요청한 모임을 찾을 수 없습니다")
        );

        // 둘 관계가 맺어진 적이 있는지 확인한다
        Optional<MoimMember> moimMemberOp = moimMemberRepository.findByMemberAndMoimId(curMember.getId(), requestDto.getMoimId());
        moimMemberOp.ifPresent(MoimMember::checkRejoinAvailable);

        // Rule Join 을 판별한다
        moimPs.getMoimJoinRule().judgeByRule(curMember);

        // 도달시 생성
        MoimMember moimMemberPs;


        if (moimMemberOp.isPresent()) {
            moimMemberPs = moimMemberOp.get();
            moimMemberPs.changeMemberState(MoimMemberState.ACTIVE);
        } else {
            moimMemberPs = MoimMember.memberJoinMoim(curMember, moimPs, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE); // 저장은 된다
        }

        return moimMemberPs;
    }



    // 3. 모임 나가기 - IBW 전환
    public String leaveMoim(MoimMemberLeaveReqDto requestDto, Member curMember) {

        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(curMember.getId(), requestDto.getMoimId()).orElseThrow(
                () -> new MoimingApiException("유저가 가입한 적 없는 모임입니다"));

        // ACTIVE 할 경우 나갈 수 있다
        if (!moimMember.getMemberState().equals(MoimMemberState.ACTIVE)) {
            throw new MoimingApiException("가입 상태가 유효한 모임이 아닙니다");
        }

        moimMember.changeMemberState(MoimMemberState.IBW);

        return "";
    }



    //    강퇴하기 (MANAGER 권한) - IBF 전환, inactiveReason 기입z
//    스스로 강퇴 불가
    public String expelMember(MoimMemberExpelReqDto requestDto, Member curMember) {

        MoimMember requestMember = moimMemberRepository.findByMemberAndMoimId(curMember.getId(), requestDto.getMoimId()).orElseThrow(
                () -> new MoimingApiException("요청하는 유저가 가입한 적 없는 모임입니다")
        );

        if (!requestMember.hasPermissionOfManager()) {
            throw new MoimingApiException("요청하는 유저는 강퇴 권한이 없습니다");
        }

        MoimMember expelMember = moimMemberRepository.findByMemberAndMoimId(requestDto.getExpelMemberId(), requestDto.getMoimId()).orElseThrow(
                () -> new MoimingApiException("모임에 없는 유저를 강퇴하려 합니다"));


        // ACTIVE 해야 강퇴할 수 있다
        if (!expelMember.getMemberState().equals(MoimMemberState.ACTIVE)) {
            throw new MoimingApiException("가입 상태가 유효한 유저가 아닙니다");
        }

        expelMember.changeMemberState(MoimMemberState.IBF);
        expelMember.setInactiveReason(requestDto.getInactiveReason());


        return "";
    }




    // 5. 운영진 임명하기 (권한으로 부여)
    public String grantMemberManager(MoimMemberGrantReqDto requestDto, Member curMember) {

        MoimMember requestMember = moimMemberRepository.findByMemberAndMoimId(curMember.getId(), requestDto.getMoimId()).orElseThrow(
                () -> new MoimingApiException("요청하는 유저가 가입한 적 없는 모임입니다")
        );

        if (!requestMember.hasPermissionOfManager()) {
            throw new MoimingApiException("요청하는 유저는 임명 권한이 없습니다");
        }

        MoimMember grantMember = moimMemberRepository.findByMemberAndMoimId(requestDto.getGrantMemberId(), requestDto.getMoimId()).orElseThrow(
                () -> new MoimingApiException("모임에 없는 유저를 임명하려 합니다"));


        if (!grantMember.getMemberState().equals(MoimMemberState.ACTIVE)) { // ACTIVE 해야 임명할 수 있다
            throw new MoimingApiException("가입 상태가 유효한 유저가 아닙니다");
        }

        grantMember.setMoimMemberRoleType(MoimMemberRoleType.MANAGER);

        return "";
    }
}
