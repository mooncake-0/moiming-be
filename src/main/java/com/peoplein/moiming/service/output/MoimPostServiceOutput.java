package com.peoplein.moiming.service.output;

import com.peoplein.moiming.domain.MoimPost;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MoimPostServiceOutput {

    private MoimPost createdMoimPost;

}
