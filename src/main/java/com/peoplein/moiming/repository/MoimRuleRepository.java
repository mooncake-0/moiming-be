package com.peoplein.moiming.repository;

import com.peoplein.moiming.domain.rules.MoimRule;

import java.util.List;

public interface MoimRuleRepository {

    Long save(MoimRule moimRule);

    /*
     {moim} 의 MoimRule 을 모두 반환
     */
    List<MoimRule> findByMoim(Long moimId);

    /*
     {moim} 의 특정 Rule 을 반환
     type - J : 가입 규칙
          - P : 유지 규칙
     */
    MoimRule findByMoimAndType(Long moimId, String ruleType);


    void remove(MoimRule moimRule);

}
