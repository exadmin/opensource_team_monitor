package com.github.exadmin.ostm.persistence;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class JsonRootContainer {
    @JsonProperty("tables")
    private List<JsonTable> tables;

    public List<JsonTable> getTables() {
        return tables;
    }

    public void setTables(List<JsonTable> tables) {
        this.tables = tables;
    }
}
