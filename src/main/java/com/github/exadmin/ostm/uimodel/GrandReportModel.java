package com.github.exadmin.ostm.uimodel;

import java.util.List;

import static com.github.exadmin.ostm.uimodel.TheColumId.*;
import static com.github.exadmin.ostm.uimodel.TheSheetId.*;

public class GrandReportModel {

    public static TheReportModel getGrandReportInstance() {
        TheReportModel theReportModel = new TheReportModel();

        // ***** "TEAM SUMMARY" SHEET *****
        TheSheet sheetTeamSummary = theReportModel.allocateSheet(SHEET_TEAM_SUMMARY_ID,
                newInstance -> newInstance.setTitle("Qubership Team"));

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
        TheSheet sheetAllRepos = theReportModel.allocateSheet(SHEET_ALL_REPOSITORIES,
                newSheet -> newSheet.setTitle("Code Quality"));

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

        TheColumn colTopics = theReportModel.allocateColumn(COL_REPO_TOPICS, newColumn -> {
            newColumn.setTitle("Topics");
            newColumn.setCssClassName(TheColumn.TD_CENTER_MIDDLE);
            newColumn.setRenderingOrder(2);
        });

        TheColumn colSonarMetric = theReportModel.allocateColumn(COL_REPO_SONAR_CODE_COVERAGE_METRIC, newColumn -> {
            newColumn.setTitle("Code Coverage");
            newColumn.setCssClassName(TheColumn.TD_CENTER_MIDDLE);
            newColumn.setRenderingOrder(2);
        });

        TheColumn colOpenedPRs = theReportModel.allocateColumn(COL_REPO_OPENED_PULL_REQUESTS_COUNT, newColumn -> {
            newColumn.setTitle("Opened Pull Requests Count");
            newColumn.setCssClassName(TheColumn.TD_CENTER_MIDDLE);
            newColumn.setRenderingOrder(3);
        });

        TheSheet sheetCheckList = theReportModel.allocateSheet(SHEET_REPOS_CHECK_LIST, newSheet -> {
            newSheet.setTitle("Repositories check list");
        });

        // ***** SECURITY CHECKS SHEET *****
        TheSheet sheetSecurity = theReportModel.allocateSheet(SHEET_REPOS_SECURITY, newSheet -> {
            newSheet.setTitle("Security Checks");
        });
        TheColumn colLicenseFile = theReportModel.allocateColumn(COL_REPO_LICENSE_FILE, newColumn -> {
            newColumn.setTitle("License File");
        });

        TheColumn colReadmeFile = theReportModel.allocateColumn(COL_REPO_README_FILE, newColumn -> {
            newColumn.setTitle("Readme File");
        });

        TheColumn colCLAFile = theReportModel.allocateColumn(COL_REPO_CLA_FILE, newColumn -> {
            newColumn.setTitle("CLA File");
        });

        TheColumn colCodeOwners = theReportModel.allocateColumn(COL_REPO_CODE_OWNERS_FILE, newColumn -> {
            newColumn.setTitle("Code Owners File");
        });

        // ********************************
        // * Build Grand report structure *
        // ********************************
        sheetTeamSummary.registerColumn(colGitHubLogin, true);
        sheetTeamSummary.registerColumn(colUserRealName, false);
        sheetTeamSummary.registerColumn(colUserAllContribs, false);

        // create 12 weeks back columns
        List<TheColumId> weekBackColumns = List.of(
                COL_WEEK_BACK_01_ID,
                COL_WEEK_BACK_02_ID,
                COL_WEEK_BACK_03_ID,
                COL_WEEK_BACK_04_ID,
                COL_WEEK_BACK_05_ID,
                COL_WEEK_BACK_06_ID,
                COL_WEEK_BACK_07_ID,
                COL_WEEK_BACK_08_ID,
                COL_WEEK_BACK_09_ID,
                COL_WEEK_BACK_10_ID,
                COL_WEEK_BACK_11_ID,
                COL_WEEK_BACK_12_ID
                );
        for (TheColumId id : weekBackColumns) {
            TheColumn column = theReportModel.allocateColumn(id, newColumn -> {
                newColumn.setTitle("Week back ???"); // the name will be initiated later in the right collector
                newColumn.setCssClassName(TheColumn.TD_CENTER_MIDDLE);

            });

            sheetTeamSummary.registerColumn(column, false);
        }


        sheetAllRepos.registerColumn(colRepoNumber, true);
        sheetAllRepos.registerColumn(colRepoName, true);
        sheetAllRepos.registerColumn(colTopics, false);
        sheetAllRepos.registerColumn(colSonarMetric, false);
        sheetAllRepos.registerColumn(colOpenedPRs, false);

        sheetCheckList.registerColumn(colRepoNumber, true);
        sheetCheckList.registerColumn(colRepoName, false);
        sheetCheckList.registerColumn(colTopics, false);

        sheetSecurity.registerColumn(colRepoNumber, true);
        sheetSecurity.registerColumn(colRepoName, false);
        sheetSecurity.registerColumn(colTopics, false);
        sheetSecurity.registerColumn(colLicenseFile, false);
        sheetSecurity.registerColumn(colReadmeFile, false);
        sheetSecurity.registerColumn(colCLAFile, false);
        sheetSecurity.registerColumn(colCodeOwners, false);

        return theReportModel;
    }
}
