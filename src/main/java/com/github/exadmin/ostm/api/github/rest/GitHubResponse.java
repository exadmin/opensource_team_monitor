package com.github.exadmin.ostm.api.github.rest;

import java.util.List;
import java.util.Map;

public class GitHubResponse {
    private int httpCode;
    private List<Map<String, Object>> dataMap;

    public GitHubResponse(int httpCode, List<Map<String, Object>> dataMap) {
        this.httpCode = httpCode;
        this.dataMap = dataMap;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public List<Map<String, Object>> getDataMap() {
        return dataMap;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public void setDataMap(List<Map<String, Object>> dataMap) {
        this.dataMap = dataMap;
    }
}
