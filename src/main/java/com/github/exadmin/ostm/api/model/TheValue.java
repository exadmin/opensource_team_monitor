package com.github.exadmin.ostm.api.model;

public class TheValue {
    private String value;

    public TheValue(String value) {
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
        return "TheValue{" +
                "value='" + value + '\'' +
                '}';
    }
}
