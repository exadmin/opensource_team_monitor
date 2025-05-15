package com.github.exadmin.ostm.github.cache;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CachedEntity {
    @JsonProperty("full-url")
    private String url;

    @JsonProperty("request-body")
    private String requestBody;

    @JsonProperty("created-when")
    private Long createdWhen;

    @JsonProperty("valid-till")
    private Long validTill;

    @JsonProperty("response-body")
    private String responseBody;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public Long getCreatedWhen() {
        return createdWhen;
    }

    public void setCreatedWhen(Long createdWhen) {
        this.createdWhen = createdWhen;
    }

    public Long getValidTill() {
        return validTill;
    }

    public void setValidTill(Long validTill) {
        this.validTill = validTill;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
}
