package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.enums.MoimMemberRoleType;
import com.peoplein.moiming.domain.enums.MoimMemberState;
import com.peoplein.moiming.domain.moim.Moim;
import com.peoplein.moiming.domain.moim.MoimMember;
import com.peoplein.moiming.exception.MoimingApiException;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;

import static com.peoplein.moiming.domain.enums.MoimMemberRoleType.*;
import static com.peoplein.moiming.domain.enums.MoimMemberState.*;
import static com.peoplein.moiming.support.TestModelParams.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MoimMemberTest {

    @Test
    void memberJoinMoim_shouldCreateMoimMember_whenRightInfoPassed() {

        // given
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);

        // given - stub
        when(moim.getCurMemberCount()).thenReturn(1);
        when(moim.getMaxMember()).thenReturn(maxMember);

        // when
        MoimMember moimMember = MoimMember.memberJoinMoim(member, moim, NORMAL, ACTIVE);

        // then - private 생성자 확인을 위한 value 확인 필요
        assertThat(moimMember.getMoimMemberRoleType()).isEqualTo(NORMAL);
        assertThat(moimMember.getMemberState()).isEqualTo(ACTIVE);

        // then - verify - 값이랑 상관 없이 해당 함수들이 정상호출되는지 확인 (연관관계 편의 메소드들 호출 확인)
        verify(moim, times(1)).getMoimMembers();
        verify(moim, times(1)).addCurMemberCount();

    }


    @Test
    void memberJoinMoim_shouldThrowException_whenNullValPassed_byInvalidParameterException() {

        // given
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);

        // when
        // then
        assertThatThrownBy(() -> MoimMember.memberJoinMoim(null, moim, NORMAL, ACTIVE)).isInstanceOf(InvalidParameterException.class);
        assertThatThrownBy(() -> MoimMember.memberJoinMoim(member, null, NORMAL, ACTIVE)).isInstanceOf(InvalidParameterException.class);
        assertThatThrownBy(() -> MoimMember.memberJoinMoim(member, moim, null, ACTIVE)).isInstanceOf(InvalidParameterException.class);
        assertThatThrownBy(() -> MoimMember.memberJoinMoim(member, moim, NORMAL, null)).isInstanceOf(InvalidParameterException.class);
    }


    @Test
    void memberJoinMoim_shouldThrowException_whenActiveMemberFull_byMoimingApiException() {

        // given
        Member member = mock(Member.class);
        Moim moim = mock(Moim.class);

        // given - stub
        when(moim.getCurMemberCount()).thenReturn(maxMember);
        when(moim.getMaxMember()).thenReturn(maxMember);

        // when
        // then
        assertThatThrownBy(() -> MoimMember.memberJoinMoim(member, moim, NORMAL, ACTIVE)).isInstanceOf(MoimingApiException.class);
    }


    @Test
    void changeMoimMemberRoleType_shouldChangeMoimMemberRoleType_whenRightInfoPassed() {

        // given
        MoimMember moimMember = spy(MoimMember.class); // 실제 TEST 메소드 반영을 위한 SPY 사용
        MoimMemberRoleType newMoimMemberRoleType = MANAGER;

        // given - stub
        when(moimMember.getMemberState()).thenReturn(ACTIVE);

        // when
        moimMember.changeMoimMemberRoleType(newMoimMemberRoleType);

        // then
        assertThat(moimMember.getMoimMemberRoleType()).isEqualTo(newMoimMemberRoleType);
    }


    @Test
    void changeMoimMemberRoleType_shouldThrowException_whenNullValPassed_byInvalidParameterException() {

        // given
        MoimMember moimMember = spy(MoimMember.class);

        // when
        // then
        assertThatThrownBy(() -> moimMember.changeMoimMemberRoleType(null)).isInstanceOf(InvalidParameterException.class);

    }


    @Test
    void changeMoimMemberRoleType_shouldThrowException_whenNotActiveMember_byMoimingApiException() {

        // given
        MoimMember moimMember = spy(MoimMember.class);
        MoimMemberRoleType newMoimMemberRoleType = MANAGER;

        // given - stub
        when(moimMember.getMemberState()).thenReturn(IBW);

        // when
        // then
        assertThatThrownBy(() -> moimMember.changeMoimMemberRoleType(newMoimMemberRoleType)).isInstanceOf(MoimingApiException.class);

    }


    // setInactiveReason = 정상
    @Test
    void setInactiveReason_shouldSetField_whenNormalTestPassed() {

        // given
        MoimMember moimMember = spy(MoimMember.class);
        String inactiveReason = "너가 활동하지 못하는 이유는";

        // when
        moimMember.setInactiveReason(inactiveReason);

        // then
        assertThat(moimMember.getInactiveReason()).isEqualTo(inactiveReason);

    }


    // setInactiveReason = ""
    @Test
    void setInactiveReason_shouldSetField_whenEmptyValPassed() {

        // given
        MoimMember moimMember = spy(MoimMember.class);
        String inactiveReason = "";

        // when
        moimMember.setInactiveReason(inactiveReason);

        // then
        assertThat(moimMember.getInactiveReason()).isEqualTo(inactiveReason);

    }


    // setInactiveReason = Null
    @Test
    void setInactiveReason_shouldSetField_whenNullValPassed() {

        // given
        MoimMember moimMember = spy(MoimMember.class);

        // when
        moimMember.setInactiveReason(null);

        // then
        assertThat(moimMember.getInactiveReason()).isEqualTo(null);

    }
    // all 동작


    // hasPermissionOfManager --> 현 상황이 MANAGER 인지 확인
    @Test
    void hasPermissionOfManager_shouldReturnTrue_whenManager() {
        // given
        MoimMember moimMember = spy(MoimMember.class);

        // given - stub
        when(moimMember.getMoimMemberRoleType()).thenReturn(MANAGER);

        // when
        boolean result = moimMember.hasPermissionOfManager();

        // then
        assertTrue(result);

    }


    // 아닐경우 false
    @Test
    void hasPermissionOfManager_shouldReturnFalse_whenNotManager() {
        // given
        MoimMember moimMember = spy(MoimMember.class);

        // given - stub
        when(moimMember.getMoimMemberRoleType()).thenReturn(NORMAL);

        // when
        boolean result = moimMember.hasPermissionOfManager();

        // then
        assertFalse(result);

    }
    // all 동작


    // changeMemberState -> 각 상황별 동작성 확인해야함
    // NULL
    @Test
    void changeMemberState_shouldThrowException_whenNullValPassed_byInvalidParameterException() {

        // given
        MoimMember moimMember = spy(MoimMember.class);

        // when
        // then
        assertThatThrownBy(() -> moimMember.changeMemberState(null)).isInstanceOf(InvalidParameterException.class);

    }


    // 같은 상태
    @Test
    void changeMemberState_shouldThrowException_whenSameMemberStatePassed_byMoimingApiException() {

        // given
        MoimMember moimMember = spy(MoimMember.class);
        MoimMemberState changingState = ACTIVE;

        // given - stub
        when(moimMember.getMemberState()).thenReturn(changingState);

        // when
        // then
        assertThatThrownBy(() -> moimMember.changeMemberState(changingState)).isInstanceOf(MoimingApiException.class);

    }


    // NOT FOUND 에서 변경 시도
    @Test
    void changeMemberState_shouldThrowException_whenTryChangingNOTFOUND_byMoimingApiException() {
        // given
        MoimMember moimMember = spy(MoimMember.class);

        // given - stub
        when(moimMember.getMemberState()).thenReturn(NOTFOUND);

        // when
        // then
        assertThatThrownBy(() -> moimMember.changeMemberState(ACTIVE)).isInstanceOf(MoimingApiException.class);

    }


    // stubbing 없이 상태변화를 봐야한다 (value based 체킹 필요) - 실제 객체 동작성 검증 필요
    // MoimMember 에는 setMemberState 이 없어서, private field 지정 불가
    // 초기 상태를 원하는 상태로 초기화한다
    private MoimMember prepareChangeMemberState(MoimMemberState preState) {

        Moim moim = mock(Moim.class);
        Member member = mock(Member.class);

        // stubbing moim
        when(moim.getMaxMember()).thenReturn(maxMember);
        when(moim.getCurMemberCount()).thenReturn(1);

        return  MoimMember.memberJoinMoim(member, moim, NORMAL, preState);
    }


    // ACTIVE --> IBW
    @Test
    void changeMemberState_shouldChange_whenACTIVEtoIBW() {

        // given
        MoimMember moimMember = prepareChangeMemberState(ACTIVE);

        // when
        moimMember.changeMemberState(IBW);

        // then
        assertThat(moimMember.getMemberState()).isEqualTo(IBW);
    }


    // ACTIVE --> IBF
    @Test
    void changeMemberState_shouldChange_whenACTIVEtoIBF() {

        // given
        MoimMember moimMember = prepareChangeMemberState(ACTIVE);

        // when
        moimMember.changeMemberState(IBF);

        // then
        assertThat(moimMember.getMemberState()).isEqualTo(IBF);
    }


    // ACTIVE --> IBD
    @Test
    void changeMemberState_shouldChange_whenACTIVEtoIBD() {

        // given
        MoimMember moimMember = prepareChangeMemberState(ACTIVE);

        // when
        moimMember.changeMemberState(IBD);

        // then
        assertThat(moimMember.getMemberState()).isEqualTo(IBD);
    }


    // ACTIVE --> NOTFOUND
    @Test
    void changeMemberState_shouldChange_whenACTIVEtoNOTFOUND() {

        // given
        MoimMember moimMember = prepareChangeMemberState(ACTIVE);

        // when
        moimMember.changeMemberState(NOTFOUND);

        // then
        assertThat(moimMember.getMemberState()).isEqualTo(NOTFOUND);
    }


    // IBW --> ACTIVE (가능) 모임 재가입
    @Test
    void changeMemberState_shouldChange_whenIBWtoACTIVE() {

        // given
        MoimMember moimMember = prepareChangeMemberState(IBW);

        // when
        moimMember.changeMemberState(ACTIVE);

        // then
        assertThat(moimMember.getMemberState()).isEqualTo(ACTIVE);
    }


    // IBW --> IBF (불가능)
    @Test
    void changeMemberState_shouldThrowException_whenIBWtoIBF_byMoimingApiException() {

        // given
        MoimMember moimMember = prepareChangeMemberState(IBW);

        // when
        // then
        assertThatThrownBy(() -> moimMember.changeMemberState(IBF)).isInstanceOf(MoimingApiException.class);
        assertThat(moimMember.getMemberState()).isEqualTo(IBW); // 바뀌지 않음을 검증

    }


    // IBW --> IBD (가능 : 휴면 전환)
    @Test
    void changeMemberState_shouldChange_whenIBWtoIBD() {

        // given
        MoimMember moimMember = prepareChangeMemberState(IBW);

        // when
        moimMember.changeMemberState(IBD);

        // then
        assertThat(moimMember.getMemberState()).isEqualTo(IBD);
    }


    // IBW --> NOTFOUND (가능)
    @Test
    void changeMemberState_shouldChange_whenIBWtoNOTFOUND() {

        // given
        MoimMember moimMember = prepareChangeMemberState(IBW);

        // when
        moimMember.changeMemberState(NOTFOUND);

        // then
        assertThat(moimMember.getMemberState()).isEqualTo(NOTFOUND);
    }


    // IBF --> ACTIVE (불가)
    @Test
    void changeMemberState_shouldThrowException_whenIBFtoACTIVE_byMoimingApiException() {

        // given
        MoimMember moimMember = prepareChangeMemberState(IBF);

        // when
        // then
        assertThatThrownBy(() -> moimMember.changeMemberState(ACTIVE)).isInstanceOf(MoimingApiException.class);
        assertThat(moimMember.getMemberState()).isEqualTo(IBF); // 바뀌지 않음을 검증
    }


    // IBF --> IBW (불가)
    @Test
    void changeMemberState_shouldThrowException_whenIBFtoIBW_byMoimingApiException() {

        // given
        MoimMember moimMember = prepareChangeMemberState(IBF);

        // when
        // then
        assertThatThrownBy(() -> moimMember.changeMemberState(IBW)).isInstanceOf(MoimingApiException.class);
        assertThat(moimMember.getMemberState()).isEqualTo(IBF); // 바뀌지 않음을 검증
    }


    // IBF --> IBD (불가능)
    @Test
    void changeMemberState_shouldThrowException_whenIBFtoIBD_byMoimingApiException() {

        // given
        MoimMember moimMember = prepareChangeMemberState(IBF);

        // when
        // then
        assertThatThrownBy(() -> moimMember.changeMemberState(IBD)).isInstanceOf(MoimingApiException.class);
        assertThat(moimMember.getMemberState()).isEqualTo(IBF); // 바뀌지 않음을 검증
    }


    // IBF --> NOTFOUND (가능)
    @Test
    void changeMemberState_shouldChange_whenIBFtoNOTFOUND() {

        // given
        MoimMember moimMember = prepareChangeMemberState(IBF);

        // when
        moimMember.changeMemberState(NOTFOUND);

        // then
        assertThat(moimMember.getMemberState()).isEqualTo(NOTFOUND);

    }


    // IBD --> ACTIVE (가능) 계정 활성화
    @Test
    void changeMemberState_shouldChange_whenIBDtoACTIVE() {

        // given
        MoimMember moimMember = prepareChangeMemberState(IBD);

        // when
        moimMember.changeMemberState(ACTIVE);

        // then
        assertThat(moimMember.getMemberState()).isEqualTo(ACTIVE);
    }


    // IBD --> IBW (불가)
    @Test
    void changeMemberState_shouldThrowException_whenIBDtoIBW_byMoimingApiException() {

        // given
        MoimMember moimMember = prepareChangeMemberState(IBD);

        // when
        // then
        assertThatThrownBy(() -> moimMember.changeMemberState(IBW)).isInstanceOf(MoimingApiException.class);
        assertThat(moimMember.getMemberState()).isEqualTo(IBD); // 바뀌지 않음을 검증
    }


    // IBD --> IBF (불가)
    @Test
    void changeMemberState_shouldThrowException_whenIBDtoIBF_byMoimingApiException() {

        // given
        MoimMember moimMember = prepareChangeMemberState(IBD);

        // when
        // then
        assertThatThrownBy(() -> moimMember.changeMemberState(IBF)).isInstanceOf(MoimingApiException.class);
        assertThat(moimMember.getMemberState()).isEqualTo(IBD); // 바뀌지 않음을 검증
    }


    // IBD --> NOTFOUND (가능)
    @Test
    void changeMemberState_shouldChange_whenIBDtoNOTFOUND() {

        // given
        MoimMember moimMember = prepareChangeMemberState(IBD);

        // when
        moimMember.changeMemberState(NOTFOUND);

        // then
        assertThat(moimMember.getMemberState()).isEqualTo(NOTFOUND);
    }
}