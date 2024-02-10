package com.peoplein.moiming.model.dto.inner;

import com.peoplein.moiming.domain.MoimCategoryLinker;
import com.peoplein.moiming.domain.moim.Moim;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class MoimCategoryMapperDto {

    private List<Moim> targetMoims;
    private List<MoimCategoryLinker> categoryLinkers;
    private Map<Long, List<MoimCategoryLinker>> categoryLinkersMap;

    public MoimCategoryMapperDto(List<Moim> targetMoims, List<MoimCategoryLinker> categoryLinkers) {
        this.targetMoims = targetMoims;
        this.categoryLinkers = categoryLinkers;
        this.categoryLinkersMap = new HashMap<>();
        buildLinkersMap();
    }

    private void buildLinkersMap() {
        for (MoimCategoryLinker categoryLinker : categoryLinkers) {
            Long keyId = categoryLinker.getMoim().getId();
            if (categoryLinkersMap.containsKey(keyId)) {
                categoryLinkersMap.get(keyId).add(categoryLinker);
            } else {
                List<MoimCategoryLinker> eachCategories = new ArrayList<>();
                eachCategories.add(categoryLinker);
                categoryLinkersMap.put(keyId, eachCategories);
            }
        }
    }
}
