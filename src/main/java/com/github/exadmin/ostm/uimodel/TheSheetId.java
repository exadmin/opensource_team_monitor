package com.github.exadmin.ostm.uimodel;

public enum TheSheetId {
    SHEET_TEAM_SUMMARY_ID("sheet:team-summary"),
    SHEET_ALL_REPOSITORIES("sheet:all-repos");

    private final String id;

    private TheSheetId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
