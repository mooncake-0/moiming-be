package com.peoplein.moiming.model.dto.inner;

import com.peoplein.moiming.domain.MoimPost;
import com.peoplein.moiming.domain.file.RelatedFile;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class MoimPostWithFileDto {

    private MoimPost moimPost;
    private List<RelatedFile> relatedFiles;
}
