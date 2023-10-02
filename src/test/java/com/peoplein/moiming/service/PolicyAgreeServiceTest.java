package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.domain.enums.PolicyType;
import com.peoplein.moiming.model.dto.request.MemberReqDto;
import com.peoplein.moiming.repository.PolicyAgreeRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static com.peoplein.moiming.domain.PolicyAgree.*;
import static com.peoplein.moiming.model.dto.request.MemberReqDto.MemberSignInReqDto.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PolicyAgreeServiceTest {

    @InjectMocks
    private PolicyAgreeService policyAgreeService;

    @Mock
    private PolicyAgreeRepository policyAgreeRepository;

    private List<PolicyAgreeReqDto> mockPolicyReqDtoList(int length) {
        List<PolicyAgreeReqDto> mockList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            mockList.add(mock(PolicyAgreeReqDto.class));
        }
        return mockList;
    }


    // 정상 동작 - save 5번 호출됨
    @Test
    void createPolicyAgree_shouldPass_whenRightInfoPassed() {

        try (MockedStatic<PolicyAgree> mocker = mockStatic(PolicyAgree.class)) {
            // given
            Member member = mock(Member.class);
            List<PolicyAgreeReqDto> policyDtos = mockPolicyReqDtoList(CUR_MOIMING_REQ_POLICY_CNT);

            // given - stub
            mocker.when(() -> PolicyAgree.createPolicyAgree(Mockito.any(), Mockito.any(), Mockito.anyBoolean())).thenReturn(null);

            // when
            policyAgreeService.createPolicyAgree(member, policyDtos);

            // then
            verify(policyAgreeRepository, times(CUR_MOIMING_REQ_POLICY_CNT)).save(any()); // 5번 정상적으로 저장을 수행하려 시도
        }
    }


    // member null
    @Test
    void createPolicyAgree_shouldThrowException_whenMemberNull_byMoimingApiException() {

        // given
        Member member = null;
        List<PolicyAgreeReqDto> policyDtos = mockPolicyReqDtoList(CUR_MOIMING_REQ_POLICY_CNT);

        // when
        // then
        assertThatThrownBy(() -> policyAgreeService.createPolicyAgree(member, policyDtos));

    }


    // list null
    @Test
    void createPolicyAgree_shouldThrowException_whenListNull_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        List<PolicyAgreeReqDto> policyDtos = null;

        // when
        // then
        assertThatThrownBy(() -> policyAgreeService.createPolicyAgree(member, policyDtos));

    }


    // 갯수 다름
    @Test
    void createPolicyAgree_shouldThrowException_whenListElementCountWrong_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        List<PolicyAgreeReqDto> policyDtos = mockPolicyReqDtoList(CUR_MOIMING_REQ_POLICY_CNT - 1);

        // when
        // then
        assertThatThrownBy(() -> policyAgreeService.createPolicyAgree(member, policyDtos));


    }

}
