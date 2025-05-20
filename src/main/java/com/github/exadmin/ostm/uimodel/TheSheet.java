package com.github.exadmin.ostm.uimodel;

import java.util.*;

public class TheSheet {
    private final String id;
    private String title;

    private final List<TheColumn> columns;
    private TheColumn baseColumn;

    TheSheet(String id) {
        this.id = id;
        this.columns = new ArrayList<>();
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

    @Override
    public String toString() {
        return id;
    }

    /**
     * Register column to be rendered within this sheet.
     * @param theColumn TheColumn instance to be included into the sheet rendering procedure
     * @param definesInitialRenderingOrder if true - then this column will be used as an ordering base for all rows.
     */
    public void registerColumn(TheColumn theColumn, boolean definesInitialRenderingOrder) {
        columns.add(theColumn);
        if (definesInitialRenderingOrder) baseColumn = theColumn;
    }

    /**
     * Returns column instance which will be used as a base column to order rows by.
     * @return TheColumn instance
     */
    public TheColumn getBaseColumn() {
        return baseColumn;
    }
}
