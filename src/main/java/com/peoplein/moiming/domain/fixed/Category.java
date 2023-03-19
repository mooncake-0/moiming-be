package com.peoplein.moiming.domain.fixed;

import com.peoplein.moiming.domain.enums.CategoryName;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "category")
//TEST SETTING
@Setter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @Column(name = "category_id")
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private CategoryName categoryName;

    private int categoryDepth;
    private boolean isUsing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}