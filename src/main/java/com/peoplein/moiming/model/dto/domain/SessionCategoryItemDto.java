package com.peoplein.moiming.model.dto.domain;

import com.peoplein.moiming.domain.session.SessionCategoryItem;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class SessionCategoryItemDto {

    private Long itemId; // 요청시 부재
    private String itemName;
    private int itemCost;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /*
     Constructor - 1
     Entity 를 통해 Domain Dto 를 형성
     */
    public SessionCategoryItemDto(SessionCategoryItem sessionCategoryItem) {

        this.itemId = sessionCategoryItem.getId();
        this.itemName = sessionCategoryItem.getItemName();
        this.itemCost = sessionCategoryItem.getItemCost();
        this.createdAt = sessionCategoryItem.getCreatedAt();
        this.updatedAt = sessionCategoryItem.getUpdatedAt();

    }

}
