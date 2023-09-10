package com.peoplein.moiming.service;


import com.peoplein.moiming.model.dto.request.MoimMemberReqDto;
import com.peoplein.moiming.repository.MoimMemberRepository;
import com.peoplein.moiming.repository.MoimRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.peoplein.moiming.model.dto.request.MoimMemberReqDto.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MoimMemberServiceTest {

    @InjectMocks
    private MoimMemberService moimMemberService;

    @Mock
    private MoimRepository moimRepository;


    @Mock
    private MoimMemberRepository moimMemberRepository;


    // 모임 못찾음
    @Test
    void joinMoim_shouldThrowException_whenMoimNotFound_byMoimingApiException() {

        // given
        MoimMemberJoinReqDto requestDto = mock(MoimMemberJoinReqDto.class);

        // when
        when(moimRepository.findWithJoinRuleById(any())).thenReturn(Optional.empty());

        // then

    }

    // judgeRule 과 checkRejoinAv 는 둘다 통과 - 실패는 각자 도메인에서 잡음
    // 둘은 연관된 적이 있다 - memberPs 바꾸는지 verify
    // 둘은 연관된 적이 없다 - Static verify 검증법 확인
    //

    @Test
    void joinMoim_should_when() {

        // given

        // when

        // then

    }

}
