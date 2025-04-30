package com.github.exadmin.ostm.api.persistence;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonTable {
    @JsonProperty("title")
    private String title;

    @JsonProperty("columns")
    private List<JsonColumn> columns;

    @JsonProperty("data")
    private List<Map<String, Object>> dataMap;

    public JsonTable(JsonRootContainer rootContainer) {
        List<JsonTable> tables = rootContainer.getTables();
        if (tables == null) tables = new ArrayList<>();

        tables.add(this);
        rootContainer.setTables(tables);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<JsonColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<JsonColumn> columns) {
        this.columns = columns;
    }

    public List<Map<String, Object>> getDataMap() {
        return dataMap;
    }

    public void addDataMap(Map<String, Object> dataMap) {
        if (this.dataMap == null) this.dataMap = new ArrayList<>();
        this.dataMap.add(new HashMap<>(dataMap));
    }
}

