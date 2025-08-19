package com.github.exadmin.ostm.uimodel;

import com.github.exadmin.ostm.persistence.overrides.JsonReportOverrides;

import java.util.*;

public class TheReportModel {
    private final Map<String, TheSheet> sheetsMap = new LinkedHashMap<>();
    private final Map<String, TheColumn> columnsMap = new HashMap<>();
    private JsonReportOverrides reportOverrides;

    TheReportModel() {
    }

    public List<TheSheet> getSheets() {
        return new ArrayList<>(sheetsMap.values());
    }

    /**
     * Searches sheet by ID. In case it exists - returns it. In case it does not exist - creates new one. Never returns null.
     * @param sheetId String sheet ID to search/create sheet by
     * @param listener in case new sheet is created - this listener is called to fulfill new sheet instance
     * @return TheSheet instance.
     */
    TheSheet allocateSheet(TheSheetId sheetId, OnCreateListener<TheSheet> listener) {
        TheSheet existedSheet = findSheet(sheetId);
        if (existedSheet != null) return existedSheet;

        // create new sheet instance
        TheSheet sheet = new TheSheet(sheetId);
        listener.process(sheet);

        sheetsMap.put(sheetId.getId(), sheet);
        return sheet;
    }

    /**
     * Search sheet by id and returns it.
     * @param sheetId String sheet-id to try to return
     * @return TheSheet instance or null if nothing is found.
     */
    public TheSheet findSheet(TheSheetId sheetId) {
        return sheetsMap.get(sheetId.getId());
    }

    TheColumn allocateColumn(TheColumnId columnId, OnCreateListener<TheColumn> listener) {
        TheColumn existedColumn = findColumn(columnId);
        if (existedColumn != null) return existedColumn;

        // create new column instance
        TheColumn theColumn = new TheColumn(columnId, columnId.isRenderId());
        listener.process(theColumn);

        columnsMap.put(columnId.getId(), theColumn);
        return theColumn;
    }

    /**
     * Searches column instance by its id.
     * @param columnId String colum id to do searching by.
     * @return TheColumn instance or null if nothing is found.
     */
    public TheColumn findColumn(TheColumnId columnId) {
        if (columnId == null) return null;
        return columnsMap.get(columnId.getId());
    }

    public JsonReportOverrides getReportOverrides() {
        return reportOverrides;
    }

    public void setReportOverrides(JsonReportOverrides reportOverrides) {
        this.reportOverrides = reportOverrides;
    }
}
