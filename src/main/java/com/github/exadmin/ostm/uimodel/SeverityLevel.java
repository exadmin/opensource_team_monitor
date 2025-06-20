package com.github.exadmin.ostm.uimodel;

// see report.js rendering rules
public enum SeverityLevel {
    INFO("INFO"),
    OK("OK"),
    ERROR("ERR"),
    WARN("WARN"),
    SECURITY_WARN("SEC"),
    SKIP("SKIP"),
    PLACE1("PLACE1"),
    INFO_PUBLIC("INFO_PUB"),
    INFO_PRIVATE("INFO_PRIV"),
    INFO_ARCHIVED("INFO_ARCH");


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
