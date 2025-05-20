package com.github.exadmin.ostm.uimodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheReportModel {
    private final Map<String, TheSheet> sheetsMap = new HashMap<>();
    private final Map<String, TheColumn> columnsMap = new HashMap<>();

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
    TheSheet allocateSheet(String sheetId, OnCreateListener<TheSheet> listener) {
        TheSheet existedSheet = findSheet(sheetId);
        if (existedSheet != null) return existedSheet;

        // create new sheet instance
        TheSheet sheet = new TheSheet(sheetId);
        listener.process(sheet);

        sheetsMap.put(sheetId, sheet);
        return sheet;
    }

    /**
     * Search sheet by id and returns it.
     * @param sheetId String sheet-id to try to return
     * @return TheSheet instance or null if nothing is found.
     */
    public TheSheet findSheet(String sheetId) {
        return sheetsMap.get(sheetId);
    }

    TheColumn allocateColumn(String columnId, OnCreateListener<TheColumn> listener) {
        TheColumn existedColumn = findColumn(columnId);
        if (existedColumn != null) return existedColumn;

        // create new column instance
        TheColumn theColumn = new TheColumn(columnId);
        listener.process(theColumn);

        columnsMap.put(columnId, theColumn);
        return theColumn;
    }

    /**
     * Searches column instance by its id.
     * @param columnId String colum id to do searching by.
     * @return TheColumn instance or null if nothing is found.
     */
    public TheColumn findColumn(String columnId) {
        return columnsMap.get(columnId);
    }
}
