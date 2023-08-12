package com.peoplein.moiming.domain.fixed;

import com.peoplein.moiming.domain.enums.RoleType;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    @Id
    @Column(name = "role_id")
    private Long id;

    private String roleDesc;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

}