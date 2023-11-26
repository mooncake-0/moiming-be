package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.PolicyAgree;
import com.peoplein.moiming.domain.enums.PolicyType;
import com.peoplein.moiming.exception.MoimingApiException;
import com.peoplein.moiming.model.dto.request.PolicyAgreeReqDto;
import com.peoplein.moiming.repository.PolicyAgreeRepository;
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
import static com.peoplein.moiming.domain.enums.PolicyType.*;
import static com.peoplein.moiming.model.dto.request.MemberReqDto.MemberSignInReqDto.*;
import static com.peoplein.moiming.model.dto.request.PolicyAgreeReqDto.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PolicyAgreeServiceTest {

    @InjectMocks
    private PolicyAgreeService policyAgreeService;

    @Mock
    private PolicyAgreeRepository policyAgreeRepository;

    private List<PolicyAgreeDto> mockPolicyReqDtoList(int length) {
        List<PolicyAgreeDto> mockList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            mockList.add(mock(PolicyAgreeDto.class));
        }
        return mockList;
    }



    // 정상 동작 - save 5번 호출됨
    @Test
    void createPolicyAgree_shouldPass_whenRightInfoPassed() {

        try (MockedStatic<PolicyAgree> mocker = mockStatic(PolicyAgree.class)) {
            // given
            Member member = mock(Member.class);
            List<PolicyAgreeDto> policyDtos = mockPolicyReqDtoList(CUR_MOIMING_REQ_POLICY_CNT);

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
        List<PolicyAgreeDto> policyDtos = mockPolicyReqDtoList(CUR_MOIMING_REQ_POLICY_CNT);

        // when
        // then
        assertThatThrownBy(() -> policyAgreeService.createPolicyAgree(member, policyDtos));

    }


    // list null
    @Test
    void createPolicyAgree_shouldThrowException_whenListNull_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        List<PolicyAgreeDto> policyDtos = null;

        // when
        // then
        assertThatThrownBy(() -> policyAgreeService.createPolicyAgree(member, policyDtos));

    }


    // 갯수 다름
    @Test
    void createPolicyAgree_shouldThrowException_whenListElementCountWrong_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        List<PolicyAgreeDto> policyDtos = mockPolicyReqDtoList(CUR_MOIMING_REQ_POLICY_CNT - 1);

        // when
        // then
        assertThatThrownBy(() -> policyAgreeService.createPolicyAgree(member, policyDtos));

    }

    //


    // updatePolicyAgree Unit Test
    // 정상 동장시 - PolicyTypeList 와 PolicyAgree 안에 있는 항목들은 반드시 같은 PolicyType 으로 묶여있다 (PolicyAgreeRepository 의 일은 여기서 검증하지 않는다)
    // 2개씩 분할 - change 2회 호출
    @Test
    void updatePolicyAgree_shouldChangeHasAgreedEach_whenRightInfoPassed() {

        // given
        Member member = mock(Member.class);
        PolicyAgreeUpdateReqDto.PolicyAgreeDto pad = mock(PolicyAgreeUpdateReqDto.PolicyAgreeDto.class);
        PolicyAgreeUpdateReqDto.PolicyAgreeDto pad2 = mock(PolicyAgreeUpdateReqDto.PolicyAgreeDto.class);
        PolicyAgree pa = mock(PolicyAgree.class);
        PolicyAgree pa2 = mock(PolicyAgree.class);
        List<PolicyAgreeUpdateReqDto.PolicyAgreeDto> mockPolicyDtos =List.of(pad, pad2);
        List<PolicyAgree> mockPolicyAgrees = List.of(pa, pa2);

        // given - stub
        when(pad.getPolicyType()).thenReturn(MARKETING_SMS);
        when(pad2.getPolicyType()).thenReturn(MARKETING_EMAIL);
        when(pa.getPolicyType()).thenReturn(MARKETING_SMS);
        when(pa2.getPolicyType()).thenReturn(MARKETING_EMAIL);
        when(policyAgreeRepository.findByMemberIdAndPolicyTypes(any(), any())).thenReturn(mockPolicyAgrees);

        // when
        policyAgreeService.updatePolicyAgree(member, mockPolicyDtos);

        // then
        verify(pa, times(1)).changeHasAgreed(anyBoolean(), any());
        verify(pa2, times(1)).changeHasAgreed(anyBoolean(), any());

    }

    // 1개씩 분할 - change 1회 호출
    @Test
    void updatePolicyAgree_shouldChangeHasAgreed_whenRightInfoPassed() {

        // given
        Member member = mock(Member.class);
        PolicyAgreeUpdateReqDto.PolicyAgreeDto pad = mock(PolicyAgreeUpdateReqDto.PolicyAgreeDto.class);
        PolicyAgree pa = mock(PolicyAgree.class);
        List<PolicyAgreeUpdateReqDto.PolicyAgreeDto> mockPolicyDtos =List.of(pad);
        List<PolicyAgree> mockPolicyAgrees = List.of(pa);

        // given - stub
        when(pad.getPolicyType()).thenReturn(MARKETING_SMS);
        when(pa.getPolicyType()).thenReturn(MARKETING_SMS);
        when(policyAgreeRepository.findByMemberIdAndPolicyTypes(any(), any())).thenReturn(mockPolicyAgrees);

        // when
        policyAgreeService.updatePolicyAgree(member, mockPolicyDtos);

        // then
        verify(pa, times(1)).changeHasAgreed(anyBoolean(), any());

    }


    // member null
    @Test
    void updatePolicyAgree_shouldThrowException_whenMemberNull_byMoimingApiException() {

        // given
        Member member = null;
        PolicyAgreeUpdateReqDto.PolicyAgreeDto pad = mock(PolicyAgreeUpdateReqDto.PolicyAgreeDto.class);
        List<PolicyAgreeUpdateReqDto.PolicyAgreeDto> mockPolicyDtos = List.of(pad);

        // when
        // then
        assertThatThrownBy(() -> policyAgreeService.updatePolicyAgree(member, mockPolicyDtos)).isInstanceOf(MoimingApiException.class);

    }


    // policyDto null
    @Test
    void updatePolicyAgree_shouldThrowException_whenDtoListNull_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        List<PolicyAgreeUpdateReqDto.PolicyAgreeDto> mockPolicyDtos = null;

        // when
        // then
        assertThatThrownBy(() -> policyAgreeService.updatePolicyAgree(member, mockPolicyDtos)).isInstanceOf(MoimingApiException.class);

    }


    // policyDto empty
    @Test
    void updatePolicyAgree_shouldThrowException_whenDtoListEmpty_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        List<PolicyAgreeUpdateReqDto.PolicyAgreeDto> mockPolicyDtos = new ArrayList<>();

        // when
        // then
        assertThatThrownBy(() -> policyAgreeService.updatePolicyAgree(member, mockPolicyDtos)).isInstanceOf(MoimingApiException.class);

    }

    // 여기서부터는 발생할 확률이 적은 상황
    // 1) policyType 반환 사이즈와 policyAgree 반환 사이즈가 다르다
    @Test
    void updatePolicyAgree_shouldThrowException_whenListLengthDiffer_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        PolicyAgreeUpdateReqDto.PolicyAgreeDto pad = mock(PolicyAgreeUpdateReqDto.PolicyAgreeDto.class);
        PolicyAgreeUpdateReqDto.PolicyAgreeDto pad2 = mock(PolicyAgreeUpdateReqDto.PolicyAgreeDto.class);
        PolicyAgree pa = mock(PolicyAgree.class);
        List<PolicyAgreeUpdateReqDto.PolicyAgreeDto> mockPolicyDtos =List.of(pad, pad2);
        List<PolicyAgree> mockPolicyAgrees = List.of(pa);

        // given - stub
        when(pad.getPolicyType()).thenReturn(MARKETING_SMS);
        when(pad2.getPolicyType()).thenReturn(MARKETING_EMAIL);
        when(policyAgreeRepository.findByMemberIdAndPolicyTypes(any(), any())).thenReturn(mockPolicyAgrees);

        // when
        // then
        assertThatThrownBy(() -> policyAgreeService.updatePolicyAgree(member, mockPolicyDtos)).isInstanceOf(MoimingApiException.class);

    }


    // 2) policyAgree 가 빈통으로 반환되었다
    @Test
    void updatePolicyAgree_shouldThrowException_whenPolicyAgreeListEmpty_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        PolicyAgreeUpdateReqDto.PolicyAgreeDto pad = mock(PolicyAgreeUpdateReqDto.PolicyAgreeDto.class);
        PolicyAgreeUpdateReqDto.PolicyAgreeDto pad2 = mock(PolicyAgreeUpdateReqDto.PolicyAgreeDto.class);
        List<PolicyAgreeUpdateReqDto.PolicyAgreeDto> mockPolicyDtos =List.of(pad, pad2);
        List<PolicyAgree> mockPolicyAgrees = new ArrayList<>();

        // given - stub
        when(pad.getPolicyType()).thenReturn(MARKETING_SMS);
        when(pad2.getPolicyType()).thenReturn(MARKETING_EMAIL);
        when(policyAgreeRepository.findByMemberIdAndPolicyTypes(any(), any())).thenReturn(mockPolicyAgrees);

        // when
        // then
        assertThatThrownBy(() -> policyAgreeService.updatePolicyAgree(member, mockPolicyDtos)).isInstanceOf(MoimingApiException.class);

    }

    // 둘이 다른게 매핑되어서 for 문을 돌지 않고 탈출하는 경우는 PolicyAgreeRepository 의 Fail Case




}
