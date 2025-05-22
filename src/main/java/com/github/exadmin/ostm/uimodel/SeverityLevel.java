package com.github.exadmin.ostm.uimodel;

public enum SeverityLevel {
    INFO("INFO"),
    OK("OK"),
    ERROR("ERR"),
    WARN("WARN"),
    SECURITY_WARN("SEC"),
    SKIP("SKIP");

    private String text;

    SeverityLevel(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
