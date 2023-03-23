package com.peoplein.moiming.domain.session;


import com.peoplein.moiming.domain.enums.SessionCategoryType;
import com.peoplein.moiming.domain.fixed.SessionCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "session_category_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SessionCategoryItem {

    public final String DEFAULT_ITEM_NAME = "DEFAULT";

    @Id
    @Column(name = "session_category_item_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String itemName;

    private int itemCost;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moim_session_id")
    private MoimSession moimSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_category_id")
    private SessionCategory sessionCategory;

}
