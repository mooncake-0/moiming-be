package com.peoplein.moiming.temp.session;


import com.peoplein.moiming.domain.fixed.SessionCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

//@Entity
@Getter
//@Table(name = "member_session_category_linker")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSessionCategoryLinker {

//    @Id
//    @Column(name = "member_session_category_linker_id")
//    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    /*
     관리 대상이 아니므로,
     create, update 에 관한 필드도 불필요
     */

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_session_linker_id")
    private MemberSessionLinker memberSessionLinker;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "session_category_id")
    private SessionCategory sessionCategory;


    public MemberSessionCategoryLinker(MemberSessionLinker memberSessionLinker, SessionCategory sessionCategory) {

        this.memberSessionLinker = memberSessionLinker;
        this.sessionCategory = sessionCategory;
        this.memberSessionLinker.getMemberSessionCategoryLinkers().add(this);
    }


}