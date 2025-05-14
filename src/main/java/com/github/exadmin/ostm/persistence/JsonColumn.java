package com.github.exadmin.ostm.persistence;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class JsonColumn {
    @JsonProperty("data")
    private String data;

    @JsonProperty("title")
    private String title;

    @JsonProperty("className")
    private String className;

    public JsonColumn(JsonTable jsonTable) {
        List<JsonColumn> columns = jsonTable.getColumns();
        if (columns == null) columns = new ArrayList<>();

        columns.add(this);
        jsonTable.setColumns(columns);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
