package com.peoplein.moiming.domain.enums;


public enum CategoryName {

    // 1차
    FRIENDSHIP(""),
    STUDY(""),
    HOBBY(""),


    // 2차
    FOOD(""),
    ALCOHOL(""),

    CODING(""),
    CHEMISTRY(""),

    PHOTOGRAPHY(""),
    READING("")
    ;

    private final String name;

    CategoryName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static CategoryName nameOf(String name) {
        for (CategoryName category : CategoryName.values()) {
            if (category.getName().equals(name)) {
                return category;
            }
        }

        return null;
    }

}
