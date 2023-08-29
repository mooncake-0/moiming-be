package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
public class MoimMemberService {

    private final MoimRepository moimRepository;
    private final MoimMemberRepository moimMemberRepository;

    // 1. 모임 내 모든 회원 및 상태 조회

    // 2. 가입 요청 (Rule Join 판별 - Front 에서 걸러줄테지만)
    public MoimMember joinMoim(Long moimId, Member curMember) {

        // 해당 모임이 있는지 확인한다
        Moim moimPs = moimRepository.findWithJoinRuleById(moimId).orElseThrow();

        // 둘 관계가 맺어진 적이 있는지 확인한다
        Optional<MoimMember> moimMemberOp = moimMemberRepository.findByMemberAndMoimId(curMember.getId(), moimId);
        if (moimMemberOp.isPresent()) {
            MoimMember moimMemberPs = moimMemberOp.get();
            if (moimMemberPs.getMemberState().equals(MoimMemberState.IBF)) {
                throw new MoimingApiException("강퇴당한 유저는 재가입할 수 없습니다");
            }
        }

        // Rule Join 을 판별한다
        moimPs.getMoimJoinRule().judgeByRule(curMember);

        // 도달시 생성
        MoimMember moimMemberPs;

        if (moimMemberOp.isPresent()) {
            moimMemberPs = moimMemberOp.get();
            moimMemberPs.changeMemberState(MoimMemberState.ACTIVE);
        }else{
            moimMemberPs = MoimMember.memberJoinMoim(curMember, moimPs, MoimMemberRoleType.NORMAL, MoimMemberState.ACTIVE); // 저장은 된다
        }

        return moimMemberPs;
    }


    // 3. 모임 나가기 - IBW 전환
    //    강퇴하기 (MANAGER 권한) - IBF 전환, inactiveReason 기입
    //    스스로 강퇴 불가

    // 5. 운영진 임명하기 (권한으로 부여)
}
