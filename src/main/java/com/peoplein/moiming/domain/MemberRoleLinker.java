package com.peoplein.moiming.domain;


import com.peoplein.moiming.domain.fixed.Role;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRoleLinker {

    @Id
    @GeneratedValue
    @Column(name = "member_role_linker_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    /*
     MEMO : Role 을 가지고 오는 유일한 방법으로, MemberRoleLinker 조회시 join 해서 들고온다
            Role 만을 가지고 오는 경로는 더 없으므로, EAGER 로 설정한다
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MemberRoleLinker grantRoleToMember(Member member, Role role) {
        MemberRoleLinker memberRoleLinker = new MemberRoleLinker(member, role);
        return memberRoleLinker;
    }

    private MemberRoleLinker(Member member, Role role) {

        this.role = role;
        this.member = member;
        this.member.addRole(this);
        this.createdAt = LocalDateTime.now();
    }

}