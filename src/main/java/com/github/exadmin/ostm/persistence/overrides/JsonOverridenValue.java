package com.github.exadmin.ostm.persistence.overrides;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JsonOverridenValue {
    @JsonProperty("name")
    String repoName;

    @JsonProperty("visual-value")
    String visualValue;

    @JsonProperty("sort-by-value")
    String sortByValue;

    @JsonProperty("severity")
    String severity;

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getVisualValue() {
        return visualValue;
    }

    public void setVisualValue(String visualValue) {
        this.visualValue = visualValue;
    }

    public String getSortByValue() {
        return sortByValue;
    }

    public void setSortByValue(String sortByValue) {
        this.sortByValue = sortByValue;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
