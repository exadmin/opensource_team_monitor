package com.github.exadmin.ostm.api.github.rest;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GitHubResponse {
    private static final TypeReference<List<Map<String, Object>>> type = new TypeReference<>() {};
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION.mappedFeature());
    }

    private int httpCode;
    private List<Map<String, Object>> dataMap;

    public GitHubResponse(int httpCode, String jsonResponse) {
        try {
            this.httpCode = httpCode;
            Object obj = OBJECT_MAPPER.readValue(jsonResponse, Object.class);

            this.dataMap = null;
            if (obj instanceof List) this.dataMap = (List<Map<String, Object>>) obj;
            if (obj instanceof Map) this.dataMap = List.of((Map<String, Object>) obj);

            if (dataMap == null) throw new IllegalStateException("Unsupported type of response: " + obj.getClass());

        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
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
