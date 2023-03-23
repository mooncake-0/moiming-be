package com.peoplein.moiming.domain.session;


import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.MemberSessionState;
import com.peoplein.moiming.domain.fixed.SessionCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "member_session_linker")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSessionLinker {

    @Id
    @Column(name = "member_session_linker_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private int singleCost;

    @Enumerated(value = EnumType.STRING)
    private MemberSessionState memberSessionState;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_session_id")
    private MoimSession moimSession;

    /*
     멤버가 해당 정산에 참여해야 하는 정산 Category 들과의 연결자들
     TODO 바로 List<Category> 가 될 수 있는 방법은 ..
     */
    @OneToMany(mappedBy = "memberSessionLinker")
    private List<MemberSessionCategoryLinker> memberSessionCategoryLinkers = new ArrayList<>();
}
