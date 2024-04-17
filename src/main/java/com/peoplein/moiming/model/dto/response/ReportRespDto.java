package com.peoplein.moiming.model.dto.response;

import com.peoplein.moiming.domain.file.File;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

public class ReportRespDto {

    @ApiModel(value = "Report API - 응답 - 신고 사진 저장")
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ReportImgRespDto {

        private List<FileDto> files;

        public ReportImgRespDto(List<File> savedFiles) {
            this.files = savedFiles.stream().map(FileDto::new).collect(Collectors.toList());
        }

        @Getter
        @Setter
        public static class FileDto {
            private Long fileId;
            private String imgUrl;

            public FileDto(File file) {
                this.fileId = file.getId();
                this.imgUrl = file.getFileUrl();
            }
        }
    }

}
