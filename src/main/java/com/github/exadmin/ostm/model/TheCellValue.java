package com.github.exadmin.ostm.model;

public class TheCellValue {
    private String value;

    public TheCellValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
