package com.github.exadmin.ostm.uimodel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/* Note: threads-unsafe implementation */
public class TheColumn {
    public static final String TD_CENTER_MIDDLE = "td-center-middle";
    public static final String TD_LEFT_MIDDLE = "td-left-middle";

    private final String id;
    private String title;
    private final Map<String, TheCellValue> dataMap;
    private final Map<String, TheCellValue> dataMapOverrides;
    private String cssClassName;
    private String helpUrl;
    private boolean renderId;

    TheColumn(TheColumnId id, boolean renderId) {
        this.id = id.getId();
        this.dataMap = new LinkedHashMap<>();
        this.dataMapOverrides = new LinkedHashMap<>();
        this.cssClassName = TD_CENTER_MIDDLE;
        this.renderId = renderId;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title + (renderId ? "[" + id  + "]": "");
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return id;
    }

    public void setValue(String rowId, TheCellValue cellValue) {
        if (!dataMapOverrides.containsKey(rowId))
            dataMap.put(rowId, cellValue);
    }

    public void setOverridenValue(String rowId, TheCellValue cellValue) {
        dataMapOverrides.put(rowId, cellValue);
        dataMap.put(rowId, cellValue);
    }

    public String getCssClassName() {
        return cssClassName;
    }

    public void setCssClassName(String cssClassName) {
        this.cssClassName = cssClassName;
    }

    public TheCellValue getValue(String rowId) {
        return dataMap.get(rowId);
    }

    Map<String, TheCellValue> getDataMap() {
        return dataMap;
    }

    public List<String> getRows() {
        return new ArrayList<>(dataMap.keySet());
    }

    public String getHelpUrl() {
        return helpUrl;
    }

    public void setHelpUrl(String helpUrl) {
        this.helpUrl = helpUrl;
    }
}
