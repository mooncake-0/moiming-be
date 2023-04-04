package com.peoplein.moiming.service;

import com.peoplein.moiming.domain.Moim;
import com.peoplein.moiming.domain.embeddable.Area;
import com.peoplein.moiming.domain.fixed.Category;
import com.peoplein.moiming.repository.MoimRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SearchService {


    private final MoimRepository moimRepository;
    private Pattern restrictCondition = Pattern.compile("[ㄱ-ㅎ]+");

    public SearchService(MoimRepository moimRepository) {
        this.moimRepository = moimRepository;
    }

    public List<Moim> searchMoim(String keywords, Area area, Category category) {
        shouldKeywordValid(keywords);
        List<String> keywordList = Arrays.stream(keywords.split(" ")).collect(Collectors.toList());
        return moimRepository.findMoimBySearchCondition(keywordList, area, category);
    }

    private void shouldKeywordValid(String keywords) {
        Matcher matcher = restrictCondition.matcher(keywords);
        if (matcher.find()) {
            throw new IllegalArgumentException("자음 불가능");
        }

        if (!StringUtils.hasText(keywords)) {
            throw new IllegalArgumentException("공백 불가능");
        }

    }
}
