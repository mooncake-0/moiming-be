package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberInfo;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.peoplein.moiming.exception.ExceptionValue.*;
import static com.peoplein.moiming.model.dto.request.MemberInfoReqDto.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberInfoService {

    private final MemberRepository memberRepository;


    @Transactional(readOnly = true)
    public Member getMemberWithMemberInfo(Long memberId) {

        return memberRepository.findWithMemberInfoById(memberId).orElseThrow(() -> {
                    log.info("{}, getMemberWithMemberInfo :: {}", this.getClass().getName(), "id: [" + memberId + "] 생성 요청자를 찾을 수 없습니다");
                    return new MoimingApiException(MEMBER_NOT_FOUND);
                }
        );
    }



    @Transactional
    public void registerMemberInfo(MemberInfoCreateReqDto requestDto, Member member) {

        if (requestDto == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        if (member.getMemberInfo() != null) {
            throw new MoimingApiException(MEMBER_INFO_ALREADY_REGISTERED);
        }

        member = persistSecurityMember(member.getId()); // 영속화하여 변경사항이 저장되도록 한다
        MemberInfo memberInfo = MemberInfo.saveMemberInfo(member, "", requestDto.getMemberGender(), requestDto.getMemberBirth());
        memberRepository.saveMemberInfo(memberInfo);

    }

    private Member persistSecurityMember(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(() -> {
            log.error("{}, persistSecurityMember :: {}", this.getClass().getName(), "Member 영속화 중 Id [" + memberId + "] 조회 불가 예외 발생, 불가능 상황");
            return new MoimingApiException(COMMON_INVALID_SITUATION);
        });

    }
}
