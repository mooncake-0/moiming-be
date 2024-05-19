package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.member.Member;
import com.peoplein.moiming.domain.member.MemberInfo;
import com.peoplein.moiming.exception.ExceptionValue;
import com.peoplein.moiming.exception.MoimingApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.peoplein.moiming.model.dto.request.MemberInfoReqDto.*;

@Service
public class MemberInfoService {


    @Transactional
    public void registerMemberInfo(MemberInfoCreateReqDto requestDto, Member member) {

        if (requestDto == null || member == null) {
            throw new MoimingApiException(ExceptionValue.COMMON_INVALID_PARAM);
        }

        MemberInfo memberInfo = MemberInfo.saveMemberInfo(member, "", requestDto.getMemberGender(), requestDto.getMemberBirth());

    }
}
