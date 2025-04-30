package com.github.exadmin.ostm.api.cache;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class CacheContainer {
    @JsonProperty("http-get-query-url")
    private String httpGetQueryUrl;

    @JsonProperty("data-map-list")
    private List<Map<String, Object>> dataMapList;

    @JsonProperty("ttl-seconds")
    private long ttlInSeconds;

    @JsonProperty("created-when")
    private long createdWhen;

    public String getHttpGetQueryUrl() {
        return httpGetQueryUrl;
    }

    public void setHttpGetQueryUrl(String httpGetQueryUrl) {
        this.httpGetQueryUrl = httpGetQueryUrl;
    }

    public List<Map<String, Object>> getDataMapList() {
        return dataMapList;
    }

    public void setDataMapList(List<Map<String, Object>> dataMapList) {
        this.dataMapList = dataMapList;
    }

    public long getTtlInSeconds() {
        return ttlInSeconds;
    }

    public void setTtlInSeconds(long ttlInSeconds) {
        this.ttlInSeconds = ttlInSeconds;
    }

    public long getCreatedWhen() {
        return createdWhen;
    }

    public void setCreatedWhen(long createdWhen) {
        this.createdWhen = createdWhen;
    }
}
