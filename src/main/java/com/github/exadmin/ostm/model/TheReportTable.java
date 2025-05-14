package com.github.exadmin.ostm.model;

import java.util.ArrayList;
import java.util.List;

public class TheReportTable {
    private final List<TheSheet> sheets = new ArrayList<>();

    public List<TheSheet> getSheets() {
        return new ArrayList<>(sheets);
    }

    public TheSheet findSheet(String sheetId, OnCreateListener<TheSheet> listener) {
        // find existed sheet by id
        for (TheSheet next : sheets) {
            if (next.getId().equals(sheetId)) return next;
        }

        // create new sheet instance
        TheSheet sheet = new TheSheet(sheetId);
        listener.process(sheet);

        sheets.add(sheet);
        return sheet;
    }
}
