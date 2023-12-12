package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.peoplein.moiming.domain.enums.MoimMemberState.*;
import static com.peoplein.moiming.model.dto.request.MoimMemberReqDto.*;


@Service
@Transactional
@RequiredArgsConstructor
public class MoimMemberService {

    private final MoimRepository moimRepository;
    private final MoimMemberRepository moimMemberRepository;


    // TODO :: 모임원일 경우, 모임원이 아닐경우에 대한 구분
    //         모임원이 아니여도 모임페이지에 어느정도 노출은 있는데 그 구분이 확인되어야 한다
    public List<MoimMember> getActiveMoimMembers(Long moimId, Member curMember) {

        Moim moimOp = moimRepository.findWithMoimMembersById(moimId).orElseThrow(
                () -> new MoimingApiException("모임을 찾을 수 없습니다")
        );

        return moimOp.getMoimMembers().stream().filter(moimMember -> moimMember.getMemberState().equals(ACTIVE))
                .collect(Collectors.toList());
    }



    // 2. 가입 요청
    public void joinMoim(MoimMemberJoinReqDto requestDto, Member curMember) {

        // 해당 모임이 있는지 확인한다
        Moim moimPs = moimRepository.findWithJoinRuleById(requestDto.getMoimId()).orElseThrow(
                () -> new MoimingApiException("요청한 모임을 찾을 수 없습니다")
        );

        // 둘 관계가 맺어진 적이 있는지 확인한다
        MoimMember moimMemberPs = moimMemberRepository.findByMemberAndMoimId(curMember.getId(), requestDto.getMoimId()).orElse(null);
        moimPs.judgeMemberJoinByRule(moimMemberPs, curMember);

    }



    // 3. 모임 나가기 - IBW 전환
    public void leaveMoim(MoimMemberLeaveReqDto requestDto, Member curMember) {

        MoimMember moimMember = moimMemberRepository.findByMemberAndMoimId(curMember.getId(), requestDto.getMoimId()).orElseThrow(
                () -> new MoimingApiException("요청하는 유저가 가입한 적 없는 모임입니다"));

        if (moimMember.hasPermissionOfManager()) {
            throw new MoimingApiException("운영자(개설자)는 모임을 나갈 수 없습니다");
        }

        moimMember.changeMemberState(IBW);

    }



    //    강퇴하기 (MANAGER 권한) - IBF 전환, inactiveReason 기입z
    //    스스로 강퇴 불가
    public void expelMember(MoimMemberExpelReqDto requestDto, Member curMember) {

        if (requestDto.getExpelMemberId().equals(curMember.getId())) {
            throw new MoimingApiException("스스로 강퇴하는 요청을 할 수 없습니다");
        }

        MoimMember requestMoimMember = moimMemberRepository.findByMemberAndMoimId(curMember.getId(), requestDto.getMoimId()).orElseThrow(
                () -> new MoimingApiException("요청하는 유저가 가입한 적 없는 모임입니다")
        );

        if (!requestMoimMember.hasPermissionOfManager()) {
            throw new MoimingApiException("요청하는 유저는 강퇴 권한이 없습니다");
        }

        MoimMember expelMoimMember = moimMemberRepository.findByMemberAndMoimId(requestDto.getExpelMemberId(), requestDto.getMoimId()).orElseThrow(
                () -> new MoimingApiException("에러 상황 :: 모임에 없는 유저를 강퇴하려 합니다"));


        expelMoimMember.changeMemberState(IBF);
        expelMoimMember.setInactiveReason(requestDto.getInactiveReason());

    }

}
