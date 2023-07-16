package com.peoplein.moiming.model.dto.request;

import com.peoplein.moiming.domain.enums.PolicyType;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PolicyAgreeRequestDto {

    private PolicyType policyType;
    private boolean isAgreed;

    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
    }

    /*
     boolean 필드를 OM 에서 JSON 을 통해 받을 때 못받는 오류가 있었음
     Lombok Setter 를 사용하면 boolean 필드일 경우 setAgreed() 라고 만들어서, JSON 필드 isAgreed 를 인식 못함
     해결 방안
     1. Boolean Wrapper 클래스 사용 - null 상태 안만들기 위해서 2안 선택
     2. Setter 클래스 직점 생성
     TODO - boolean 필드를 사용한 DTO 들 모두 setter 직접 생성 필요
     */
    public void setIsAgreed(boolean isAgreed) {
        this.isAgreed = isAgreed;
    }

}
