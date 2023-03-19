package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.fixed.Category;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "moim_category_linker")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimCategoryLinker {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "moim_category_linker_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_id")
    private Moim moim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MoimCategoryLinker(Moim moim, Category category) {

        DomainChecker.checkWrongObjectParams(this.getClass().getName(), moim, category);
        this.moim = moim;
        this.category = category;
    }


}
