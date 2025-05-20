package com.github.exadmin.ostm.uimodel;

public class GrandReportFactory {
    public static final String SHEET_TEAM_SUMMARY_ID = "sheet:team-summary";
    public static final String SHEET_ALL_REPOSITORIES = "sheet:all-repos";

    public static final String COL_USER_LOGIN = "column:user_login";
    public static final String COL_USER_REAL_NAME = "column:user_real_name";
    public static final String COL_CONTRIBUTIONS_FOR_ALL_TIMES_ID = "column:contributions_for_all_times";
    public static final String COL_WEEK_BACK_ID_PREFIX = "column:week_back_";

    public static final String COL_REPO_NUMBER = "column:repo_number";
    public static final String COL_REPO_NAME = "column:repo_name";


    public static TheReportModel getGrandReportInstance() {
        TheReportModel theReportModel = new TheReportModel();


        // ***** "TEAM SUMMARY" SHEET *****
        TheSheet sheetTeamSummary = theReportModel.allocateSheet(SHEET_TEAM_SUMMARY_ID, newInstance -> newInstance.setTitle("Team Summary"));

        // User login column
        TheColumn colGitHubLogin = theReportModel.allocateColumn(COL_USER_LOGIN, newColumn -> {
            newColumn.setTitle("GitHub Login");
            newColumn.setCssClassName(TheColumn.TD_LEFT_MIDDLE);
            newColumn.setRenderingOrder(-1000);
        });

        // User real name column
        TheColumn colUserRealName = theReportModel.allocateColumn(COL_USER_REAL_NAME, newColumn -> {
            newColumn.setTitle("Real name");
            newColumn.setCssClassName(TheColumn.TD_LEFT_MIDDLE);
            newColumn.setRenderingOrder(-900);
        });

        // User contributions column
        TheColumn colUserAllContribs = theReportModel.allocateColumn(COL_CONTRIBUTIONS_FOR_ALL_TIMES_ID, newColumn -> {
            newColumn.setTitle("User contributions for All Times");
            newColumn.setCssClassName(TheColumn.TD_CENTER_MIDDLE);
            newColumn.setRenderingOrder(-800);
        });

        // ***** "ALL REPOSITORIES" SHEET *****
        TheSheet sheetAllRepos = theReportModel.allocateSheet(SHEET_ALL_REPOSITORIES, newSheet -> newSheet.setTitle("All Repositories"));

        TheColumn colRepoNumber = theReportModel.allocateColumn(COL_REPO_NUMBER, newColumn -> {
            newColumn.setTitle("Number");
            newColumn.setCssClassName(TheColumn.TD_LEFT_MIDDLE);
            newColumn.setRenderingOrder(0);
        });

        TheColumn colRepoName = theReportModel.allocateColumn(COL_REPO_NAME, newColumn -> {
            newColumn.setTitle("Repository Name");
            newColumn.setCssClassName(TheColumn.TD_LEFT_MIDDLE);
            newColumn.setRenderingOrder(1);
        });

        // Build Grand report structure
        sheetTeamSummary.registerColumn(colGitHubLogin, true);
        sheetTeamSummary.registerColumn(colUserRealName, false);
        sheetTeamSummary.registerColumn(colUserAllContribs, false);

        // create 12 weeks back columns
        for (int i = 13; i > 0; i--) {
            TheColumn colWeekBackX = theReportModel.allocateColumn(COL_WEEK_BACK_ID_PREFIX + i, newColumn -> {
                newColumn.setTitle("Week back ???"); // the name will be initiated later in the right collector
                newColumn.setCssClassName(TheColumn.TD_CENTER_MIDDLE);
            });

            sheetTeamSummary.registerColumn(colWeekBackX, false);
        }

        sheetAllRepos.registerColumn(colRepoNumber, true);
        sheetAllRepos.registerColumn(colRepoName, true);

        return theReportModel;
    }
}
