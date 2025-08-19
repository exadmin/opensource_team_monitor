package com.github.exadmin.ostm.persistence.overrides;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class JsonReportOverrides {
    @JsonProperty("columns")
    List<JsonRepoColumn> columns;

    public List<JsonRepoColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<JsonRepoColumn> columns) {
        this.columns = columns;
    }

    public JsonOverridenValue findOverridenValue(String columnId, String repoName) {
        for (JsonRepoColumn column : getColumns()) {
            if (column.getColumnId().equalsIgnoreCase(columnId)) {
                for (JsonOverridenValue value : column.repositories) {
                    if (value.getRepoName().equals(repoName)) return value;
                }
            }
        }

        return null;
    }
}
