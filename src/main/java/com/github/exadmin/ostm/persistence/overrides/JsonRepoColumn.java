package com.github.exadmin.ostm.persistence.overrides;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class JsonRepoColumn {
    @JsonProperty("column-id")
    String columnId;

    @JsonProperty("repositories")
    List<JsonOverridenValue> repositories;

    public List<JsonOverridenValue> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<JsonOverridenValue> repositories) {
        this.repositories = repositories;
    }

    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }
}
