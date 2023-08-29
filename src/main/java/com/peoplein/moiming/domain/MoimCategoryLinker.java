package com.peoplein.moiming.domain;

import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.domain.moim.Moim;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Table(name = "moim_category_linker")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoimCategoryLinker extends BaseEntity{

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


    private MoimCategoryLinker(Moim moim, Category category) {
        this.moim = moim;
        this.category = category;

        // 연관관계 매핑
        this.moim.getMoimCategoryLinkers().add(this);
    }

    public static MoimCategoryLinker addMoimCategory(Moim moim, Category category) {
        return new MoimCategoryLinker(moim, category);
    }

    public static List<String> convertLinkersToNameValues(List<MoimCategoryLinker> moimCategoryLinkers) {
        return moimCategoryLinkers.stream().map(mcl -> mcl.getCategory().getCategoryName().getValue()).collect(Collectors.toList());
    }
}
