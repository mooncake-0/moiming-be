package com.peoplein.moiming.domain;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberMoimMaterializedView {


    @Id
    @Column(name = "member_moim_materialized_view_id")
    @GeneratedValue
    private Long id;

    private Long count;
    private Long moimId; // Moim 객체를 같이 줘야할 듯? 다 필요는 없을 것 같고. 여기서 in 몇개만 찾아내서 주면 될 듯. ㅇㅇ..

    private MemberMoimMaterializedView(Long count, Long moimId) {
        this.count = count;
        this.moimId = moimId;
    }

    public void updateCount(Long count) {
        this.count = count;
    }


    public static MemberMoimMaterializedView create(Long count, Long moimId) {
        return new MemberMoimMaterializedView(count, moimId);
    }
}
