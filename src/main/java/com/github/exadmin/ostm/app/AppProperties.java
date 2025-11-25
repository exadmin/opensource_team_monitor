package com.github.exadmin.ostm.app;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AppProperties {
    @JsonProperty(value = "DIRECTORY-TO-SCAN", required = true)
    private String dirToScan;

    public String getDirToScan() {
        return dirToScan;
    }
}
