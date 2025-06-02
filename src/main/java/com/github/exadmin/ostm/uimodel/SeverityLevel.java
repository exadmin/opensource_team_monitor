package com.github.exadmin.ostm.uimodel;

public enum SeverityLevel {
    INFO("INFO"),
    OK("OK"),
    ERROR("ERR"),
    WARN("WARN"),
    SECURITY_WARN("SEC"),
    SKIP("SKIP"),
    PLACE1("PLACE1");


    private String text;

    SeverityLevel(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    public boolean isErroneous() {
        return (this == ERROR) || (this == WARN) || (this == SECURITY_WARN);
    }
}
