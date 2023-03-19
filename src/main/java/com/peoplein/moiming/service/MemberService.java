package com.peoplein.moiming.service;

import com.peoplein.moiming.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

}
