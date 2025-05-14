package com.github.exadmin.ostm.github.api;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    /**
     * Goes over data-maps by path provided with keys and returns found result
     * @param keys list of key names.
     * @return single value of required type
     */
    @SuppressWarnings("unchecked")
    public <T> T getSingleValue(String ... keys) {
        Map<String, Object> currentMap = dataMap.getFirst();
        Object currentObj = null;

        for (String key : keys) {
            if (currentMap == null) throw new IllegalArgumentException("Incorrect path for the map: " + Arrays.toString(keys));
            currentObj = currentMap.get(key);

            if (currentObj == null) return null;
            if (currentObj instanceof Map) {
                currentMap = (Map<String, Object>) currentObj;
            } else {
                currentMap = null;
            }
        }

        try {
            if (currentObj == null) return null;

            return (T) currentObj;
        } catch (ClassCastException ex) {
            log.error("Class-cast exception while returning value from data-map. Current value is {}, type is {}", currentObj, currentObj.getClass(), ex);
            throw new IllegalStateException(ex);
        }
    }
}
