package com.peoplein.moiming;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peoplein.moiming.domain.enums.MemberGender;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

public class TestHelper {

    public static <T> T convert(MvcResult result, Class<T> clazz) throws Exception {
        return new ObjectMapper().readValue(result.getResponse().getContentAsString(), clazz);
    }
}
