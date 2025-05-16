package com.github.exadmin.ostm.github.api;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.exadmin.ostm.utils.MiscUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GitHubResponse {
    private static final Logger log = LoggerFactory.getLogger(GitHubResponse.class);

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
            this.dataMap = Collections.emptyList();

            if (!"[]".equals(jsonResponse)) {
                this.dataMap = null;

                Object obj = OBJECT_MAPPER.readValue(jsonResponse, Object.class);

                if (obj instanceof List) this.dataMap = (List<Map<String, Object>>) obj;
                if (obj instanceof Map) this.dataMap = List.of((Map<String, Object>) obj);

                if (dataMap == null) throw new IllegalStateException("Unsupported type of response: " + obj.getClass());
            }
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

    public <T> T getObject(String path) {
        return MiscUtils.getValue(dataMap.getFirst(), path);
    }
}
