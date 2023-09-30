package com.peoplein.moiming.domain.fixed;

import com.peoplein.moiming.domain.BaseEntity;
import com.peoplein.moiming.domain.enums.CategoryName;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Category extends BaseEntity {

    @Id
    @Column(name = "category_id")
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private CategoryName categoryName;

    private int categoryDepth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

}