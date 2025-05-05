package com.github.exadmin.ostm.api.model;

import java.util.*;

public class TheSheet {
    private final String id;
    private String title;
    private final List<TheColumn> columns;
    private final List<String> rows;

    TheSheet(String id) {
        this.id = id;
        this.columns = new ArrayList<>();
        this.rows    = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TheSheet that = (TheSheet) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<TheColumn> getColumns() {
        return new ArrayList<>(columns);
    }

    public List<String> getRows() {
        return new ArrayList<>(rows);
    }

    public List<String> getRowsDirectly() {
        return rows;
    }

    @Override
    public String toString() {
        return id;
    }

    public TheColumn getColumn(String columnId, OnCreateListener<TheColumn> listener) {
        // try find existed column by provided id
        for (TheColumn next : columns) {
            if (next.getId().equals(columnId)) return next;
        }

        // creating new column
        TheColumn theColumn = new TheColumn(columnId, this);
        listener.process(theColumn);

        columns.add(theColumn);
        return theColumn;
    }
}
