package com.peoplein.moiming.domain.file;

import lombok.Getter;

@Getter
public enum FileDomain {

    USER("user"),
    MOIM("moim"),
    POST("moimPost"),
    REPORT("report");

    public final String pathName;

    FileDomain(String pathName) {
        this.pathName = pathName;
    }
}
