package com.github.exadmin.ostm.uimodel;

import java.util.LinkedHashMap;
import java.util.Map;

public class TheColumn {
    public static final String TD_CENTER_MIDDLE = "td-center-middle";
    public static final String TD_LEFT_MIDDLE = "td-left-middle";

    private final String id;
    private String title;
    private final Map<String, TheCellValue> dataMap;
    private String cssClassName;
    private final TheSheet parent;
    private int renderingOrder;

    TheColumn(String id, TheSheet parent) {
        this.id = id;
        this.dataMap = new LinkedHashMap<>();
        this.cssClassName = TD_CENTER_MIDDLE;
        this.parent = parent;
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

    @Override
    public String toString() {
        return id;
    }

    public void addValue(String rowId, TheCellValue cellValue) {
        if (!parent.getRowsDirectly().contains(rowId)) parent.getRowsDirectly().add(rowId);
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

    public int getRenderingOrder() {
        return renderingOrder;
    }

    public void setRenderingOrder(int renderingOrder) {
        this.renderingOrder = renderingOrder;
    }
}
