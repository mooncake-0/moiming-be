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

    private final MemberService memberService;
    private final MemberRepository memberRepository;


    @Transactional
    public void registerMemberInfo(MemberInfoCreateReqDto requestDto, Member member) {

        if (requestDto == null || member == null) {
            throw new MoimingApiException(COMMON_INVALID_PARAM);
        }

        // Token Member 가 아닌 실제 Member 의 정보 활용 필요, 영속화하여 변경 사항 저장 필요
        member = memberService.persistMember(member, false);

        if (member.getMemberInfo() != null) {
            throw new MoimingApiException(MEMBER_INFO_ALREADY_REGISTERED);
        }

        MemberInfo memberInfo = MemberInfo.saveMemberInfo(member, "", requestDto.getMemberGender(), requestDto.getMemberBirth());
        memberRepository.saveMemberInfo(memberInfo);

    }
}
