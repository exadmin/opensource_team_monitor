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

    List<String> getRowsDirectly() {
        return rows;
    }

    @Override
    public String toString() {
        return id;
    }

    public TheColumn findColumn(String columnId, OnCreateListener<TheColumn> listener) {
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

    public void sortBy(TheColumn column, Comparator<TheCellValue> comparator) {
        // define keys which are know and unknown to the mentioned column (as sheet can contain more data than exists in the column)
        Set<String> mentionedColumnKeys = column.getDataMap().keySet();
        List<String> unknownToColumnKeys = new ArrayList<>(rows);
        unknownToColumnKeys.removeAll(mentionedColumnKeys);

        // fulfill column with null for each unknown key
        List<Map.Entry<String, TheCellValue>> entryList = new ArrayList<>(column.getDataMap().entrySet());
        for (String rowId : unknownToColumnKeys) {
            Map.Entry<String, TheCellValue> me = new AbstractMap.SimpleEntry<>(rowId, new TheCellValue(""));
            entryList.add(me);
        }

        // do sorting
        entryList.sort((entry1, entry2) -> {
            return comparator.compare(entry1.getValue(), entry2.getValue());
        });

        // refill rows & column data-map by reordered values
        column.getDataMap().clear();
        rows.clear();

        for (Map.Entry<String, TheCellValue> entry : entryList) {
            column.getDataMap().put(entry.getKey(), entry.getValue());
            rows.add(entry.getKey());
        }

        // drop values from mentioned column for unexisted keys (can be done on prev step actually)
        column.getDataMap().keySet().retainAll(rows);
    }

    public void sortColumnsByRenderingOrder() {
        columns.sort((column1, column2) -> {
            int result = column1.getRenderingOrder() - column2.getRenderingOrder();
            if (result != 0) return result;
            String title1 = column1.getTitle() == null ? "" : column1.getTitle();
            String title2 = column2.getTitle() == null ? "" : column2.getTitle();

            return title1.compareTo(title2);
        });
    }
}
