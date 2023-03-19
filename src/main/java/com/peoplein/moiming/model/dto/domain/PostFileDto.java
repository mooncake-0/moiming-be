package com.peoplein.moiming.model.dto.domain;


import com.peoplein.moiming.domain.enums.FileType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostFileDto {

    private String filePath;
    private String fileName;
    private FileType fileType;

}
