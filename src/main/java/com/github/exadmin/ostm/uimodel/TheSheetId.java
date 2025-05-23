package com.github.exadmin.ostm.uimodel;

public enum TheSheetId {
    SHEET_TEAM_SUMMARY_ID("sheet:qubership-team"),
    SHEET_ALL_REPOSITORIES("sheet:code-quality"),
    SHEET_REPOS_CHECK_LIST("sheet:repos-check-list"),
    SHEET_REPOS_SECURITY("sheet:repos-security-checks");

    private final String id;

    private TheSheetId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
