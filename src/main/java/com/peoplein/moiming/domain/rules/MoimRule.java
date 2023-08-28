package com.peoplein.moiming.domain.rules;


import com.peoplein.moiming.domain.moim.Moim;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

//@Entity
@Getter @Setter
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class MoimRule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "moim_rule_id")
    private Long id;

    @Column(name = "rule_type")
    protected String ruleType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id")
    protected Moim moim;

    protected LocalDateTime createdAt;
    protected Long createdMemberId;
    protected LocalDateTime updatedAt;
    protected Long updatedMemberId;

}