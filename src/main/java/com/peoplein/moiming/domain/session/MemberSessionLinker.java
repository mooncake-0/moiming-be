package com.peoplein.moiming.domain.session;


import com.peoplein.moiming.domain.DomainChecker;
import com.peoplein.moiming.domain.Member;
import com.peoplein.moiming.domain.enums.MemberSessionState;
import com.peoplein.moiming.domain.enums.SessionCategoryType;
import com.peoplein.moiming.domain.fixed.SessionCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    @OneToMany(mappedBy = "memberSessionLinker", cascade = CascadeType.ALL)
    private List<MemberSessionCategoryLinker> memberSessionCategoryLinkers = new ArrayList<>();

    public static MemberSessionLinker createMemberSessionLinker(int singleCost, MemberSessionState memberSessionState, Member member, MoimSession moimSession) {
        MemberSessionLinker memberSessionLinker = new MemberSessionLinker(singleCost, memberSessionState, member, moimSession);
        return memberSessionLinker;
    }

    private MemberSessionLinker(int singleCost, MemberSessionState memberSessionState, Member member, MoimSession moimSession) {

        DomainChecker.checkWrongObjectParams(this.getClass().getName(), memberSessionState, member, moimSession);

        this.singleCost = singleCost;
        this.memberSessionState = memberSessionState;

        // 초기화
        this.createdAt = LocalDateTime.now();

        // 연관관계 매핑
        this.member = member;
        this.moimSession = moimSession;
        this.moimSession.getMemberSessionLinkers().add(this);
    }

    public List<SessionCategoryType> getMemberSessionCategoryTypes() {
        return this.getMemberSessionCategoryLinkers().stream().map(mscl -> mscl.getSessionCategory().getCategoryType()).collect(Collectors.toList());
    }

}
