package com.github.exadmin.ostm.uimodel;

import java.util.List;

import static com.github.exadmin.ostm.uimodel.TheColumnId.*;
import static com.github.exadmin.ostm.uimodel.TheSheetId.*;

public class GrandReportModel {

    public static TheReportModel getGrandReportInstance() {
        TheReportModel theReportModel = new TheReportModel();

        // ***** "TEAM SUMMARY" SHEET *****
        TheSheet sheetTeamSummary = theReportModel.allocateSheet(SHEET_TEAM_SUMMARY_ID,
                newInstance -> newInstance.setTitle("Qubership Team Members"));

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
        TheSheet sheetCodeQuality = theReportModel.allocateSheet(SHEET_ALL_REPOSITORIES,
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
            newColumn.setHelpUrl("https://wiki.qubership.org/en/Personal-space/Larkin/sonar-integration-guide");
        });

        TheColumn colOpenedPRs = theReportModel.allocateColumn(COL_REPO_OPENED_PULL_REQUESTS_COUNT, newColumn -> {
            newColumn.setTitle("Opened Pull Requests Count");
            newColumn.setCssClassName(TheColumn.TD_CENTER_MIDDLE);
            newColumn.setRenderingOrder(3);
        });

        TheSheet sheetCheckList = theReportModel.allocateSheet(SHEET_REPOS_CHECK_LIST, newSheet -> {
            newSheet.setTitle("Security check list");
        });

        // ***** DEVOPS CHECKS SHEET *****
        TheSheet devOpsWorkflowsSheet = theReportModel.allocateSheet(SHEET_REPOS_SECURITY, newSheet -> {
            newSheet.setTitle("DevOps Workflows");
        });

        TheColumn colLicenseFile = theReportModel.allocateColumn(COL_REPO_LICENSE_FILE, newColumn -> {
            newColumn.setTitle("License File Presence");
            newColumn.setHelpUrl("https://www.apache.org/licenses/LICENSE-2.0.txt");
        });

        TheColumn colReadmeFile = theReportModel.allocateColumn(COL_REPO_README_FILE, newColumn -> {
            newColumn.setTitle("README.md (size in bytes)");
        });

        TheColumn colCLAFile = theReportModel.allocateColumn(COL_REPO_CLA_FILE, newColumn -> {
            newColumn.setTitle("CLA File");
            newColumn.setHelpUrl("https://github.com/Netcracker/qubership-workflow-hub?tab=readme-ov-file#cla");
        });

        TheColumn colCodeOwners = theReportModel.allocateColumn(COL_REPO_CODE_OWNERS_FILE, newColumn -> {
            newColumn.setTitle("Code Owners File");
            newColumn.setHelpUrl("https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners");
        });

        TheColumn colBadListedWords = theReportModel.allocateColumn(COL_REPO_SEC_BAD_WORDS_CHECKER, newColumn -> {
            newColumn.setTitle("Restricted signatures");
        });

        TheColumn colConventionalCommits = theReportModel.allocateColumn(COL_REPO_CONVENTIONAL_COMMITS_ACTION, newCol -> {
            newCol.setTitle("Conventional Commits");
            newCol.setHelpUrl("https://github.com/Netcracker/qubership-workflow-hub?tab=readme-ov-file#conventional-commits-pr-check");
        });

        TheColumn colLinter = theReportModel.allocateColumn(COL_REPO_LINTER, newCol -> {
            newCol.setTitle("Linter/Prettier");
            newCol.setHelpUrl("https://github.com/Netcracker/qubership-workflow-hub?tab=readme-ov-file#lint-code-base-super-linter");
        });

        TheColumn colLabeler = theReportModel.allocateColumn(COL_REPO_LABELER, newCol -> {
            newCol.setTitle("Automatic Labeler");
            newCol.setHelpUrl("https://github.com/Netcracker/qubership-workflow-hub?tab=readme-ov-file#automatic-pr-labels-based-on-conventional-commits");
        });

        TheColumn colLintTitle = theReportModel.allocateColumn(COL_REPO_LINT_TITLE, newCol -> {
            newCol.setTitle("Lint Title");
            newCol.setHelpUrl("https://github.com/Netcracker/qubership-workflow-hub?tab=readme-ov-file#lint-pr-title");
        });

        TheColumn colProfanity = theReportModel.allocateColumn(COL_REPO_PROFANITY_ACTION, newCol -> {
            newCol.setTitle("Profanity Checker");
            newCol.setHelpUrl("https://github.com/Netcracker/qubership-workflow-hub?tab=readme-ov-file#profanity-filter");
        });

        TheColumn colBadLinks = theReportModel.allocateColumn(COL_REPO_SEC_BAD_LINKS_CHECKER, newCol -> {
            newCol.setTitle("Bad Links Checker");
        });

        TheColumn colBuildOnCommit = theReportModel.allocateColumn(COL_REPO_BUILD_ON_COMMIT, newCol -> {
            newCol.setTitle("Build on Commit");
        });

        // SUMMARY SHEET
        TheSheet sheetSummary = theReportModel.allocateSheet(SHEET_SUMMARY, newSheet-> {
            newSheet.setTitle("Summary by teams");
        });

        TheColumn colTeamName = theReportModel.allocateColumn(COL_SUMMARY_TEAM_NAME, newCol -> {
            newCol.setTitle("Team Name");
        });

        TheColumn colTotalErrors = theReportModel.allocateColumn(COL_SUMMARY_TEAM_TOTAL_ERRORS, newCol -> {
            newCol.setTitle("Total Errors");
        });

        TheColumn colTotalRepositories = theReportModel.allocateColumn(COL_SUMMARY_TEAM_TOTAL_REPOSITORIES, newCol -> {
            newCol.setTitle("Number of Repositories");
        });

        TheColumn colErrorsPerRepository = theReportModel.allocateColumn(COL_SUMMARY_TEAM_ERRS_PER_REPOSITORY, newCol -> {
            newCol.setTitle("Error per Repository");
        });

        TheColumn colRedTeamLead = theReportModel.allocateColumn(COL_SUMMARY_TEAM_RED_LEAD_NAME, newCol -> {
            newCol.setTitle("Red lead name");
        });

        TheColumn colBlueTeamLead = theReportModel.allocateColumn(COL_SUMMARY_TEAM_BLUE_LEAD_NAME, newCol -> {
            newCol.setTitle("Blue lead name");
        });

        // ********************************
        // * Build Grand report structure *
        // ********************************
        sheetTeamSummary.registerColumn(colGitHubLogin, true);
        sheetTeamSummary.registerColumn(colUserRealName, false);
        // sheetTeamSummary.registerColumn(colUserAllContribs, false);

        // create 12 weeks back columns
        List<TheColumnId> weekBackColumns = List.of(
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
        for (TheColumnId id : weekBackColumns) {
            TheColumn column = theReportModel.allocateColumn(id, newColumn -> {
                newColumn.setTitle("Week back ???"); // the name will be initiated later in the right collector
                newColumn.setCssClassName(TheColumn.TD_CENTER_MIDDLE);

            });

            // sheetTeamSummary.registerColumn(column, false);
        }


        sheetCodeQuality.registerColumn(colRepoNumber, true);
        sheetCodeQuality.registerColumn(colRepoName, true);
        sheetCodeQuality.registerColumn(colTopics, false);
        sheetCodeQuality.registerColumn(colReadmeFile, false);
        sheetCodeQuality.registerColumn(colSonarMetric, false);
        sheetCodeQuality.registerColumn(colOpenedPRs, false);

        sheetCheckList.registerColumn(colRepoNumber, true);
        sheetCheckList.registerColumn(colRepoName, false);
        sheetCheckList.registerColumn(colTopics, false);
        sheetCheckList.registerColumn(colBadListedWords, false);

        devOpsWorkflowsSheet.registerColumn(colRepoNumber, true);
        devOpsWorkflowsSheet.registerColumn(colRepoName, false);
        devOpsWorkflowsSheet.registerColumn(colTopics, false);
        devOpsWorkflowsSheet.registerColumn(colLicenseFile, false);
        devOpsWorkflowsSheet.registerColumn(colCLAFile, false);
        devOpsWorkflowsSheet.registerColumn(colConventionalCommits, false);
        devOpsWorkflowsSheet.registerColumn(colCodeOwners, false);
        devOpsWorkflowsSheet.registerColumn(colLinter, false);
        devOpsWorkflowsSheet.registerColumn(colLabeler, false);
        devOpsWorkflowsSheet.registerColumn(colLintTitle, false);
        devOpsWorkflowsSheet.registerColumn(colProfanity, false);
        devOpsWorkflowsSheet.registerColumn(colBadLinks, false);
        devOpsWorkflowsSheet.registerColumn(colBuildOnCommit, false);

        sheetSummary.registerColumn(colTeamName, true);
        sheetSummary.registerColumn(colRedTeamLead, false);
        sheetSummary.registerColumn(colBlueTeamLead, false);
        sheetSummary.registerColumn(colTotalErrors, false);
        sheetSummary.registerColumn(colTotalRepositories, false);
        sheetSummary.registerColumn(colErrorsPerRepository, false);

        return theReportModel;
    }
}
