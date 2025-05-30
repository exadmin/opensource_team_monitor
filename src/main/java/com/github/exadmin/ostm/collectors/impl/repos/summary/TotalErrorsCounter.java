package com.github.exadmin.ostm.collectors.impl.repos.summary;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TotalErrorsCounter extends AbstractCollector {
    private static final List<TheColumnId> COLUMNS = new ArrayList<>();
    static {
        COLUMNS.add(TheColumnId.COL_REPO_LICENSE_FILE);
        COLUMNS.add(TheColumnId.COL_REPO_CLA_FILE);
        COLUMNS.add(TheColumnId.COL_REPO_CONVENTIONAL_COMMITS_ACTION);
        COLUMNS.add(TheColumnId.COL_REPO_CODE_OWNERS_FILE);
        COLUMNS.add(TheColumnId.COL_REPO_LINTER);
        COLUMNS.add(TheColumnId.COL_REPO_LABELER);
        COLUMNS.add(TheColumnId.COL_REPO_LINT_TITLE);
        COLUMNS.add(TheColumnId.COL_REPO_PROFANITY_ACTION);
        COLUMNS.add(TheColumnId.COL_REPO_SEC_BAD_LINKS_CHECKER);
        COLUMNS.add(TheColumnId.COL_REPO_BUILD_ON_COMMIT);
        COLUMNS.add(TheColumnId.COL_REPO_SEC_BAD_WORDS_CHECKER);
        COLUMNS.add(TheColumnId.COL_REPO_TOPICS);
        COLUMNS.add(TheColumnId.COL_REPO_README_FILE);
        COLUMNS.add(TheColumnId.COL_REPO_SONAR_CODE_COVERAGE_METRIC);
        COLUMNS.add(TheColumnId.COL_REPO_OPENED_PULL_REQUESTS_COUNT);
    }
    @Override
    public void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        final TheColumn myColumn = theReportModel.findColumn(TheColumnId.COL_SUMMARY_TEAM_TOTAL_ERRORS);

        List<TheColumn> columns = new ArrayList<>();

        for (TheColumnId columnId : COLUMNS) {
            columns.add(theReportModel.findColumn(columnId));
        }

        Map<String, List<GitHubRepository>> teamsMap = getRepositoriesMappedByTeams(gitHubFacade);
        for (String rowIdWhichIsTeam : teamsMap.keySet()) {
            // calculate total number of errors per all repositories and metrics
            int errorsCount = 0;

            List<GitHubRepository> allRelatedRepositories = teamsMap.get(rowIdWhichIsTeam);
            for (GitHubRepository repo : allRelatedRepositories) {
                String rowIdWhichIsRepoId = repo.getId();

                for (TheColumn column : columns) {
                    TheCellValue value = column.getValue(rowIdWhichIsRepoId);
                    if (value.getSeverityLevel().isErroneous()) errorsCount++;
                }
            }

            myColumn.addValue(rowIdWhichIsTeam, new TheCellValue("" + errorsCount, errorsCount, SeverityLevel.INFO));
        }
    }

    /**
     * Returns map of repositories which belongs to the teams
     * @return Map of team-names which linked to list of repositories
     */
    private Map<String, List<GitHubRepository>> getRepositoriesMappedByTeams(GitHubFacade gitHubFacade) {
        Map<String, List<GitHubRepository>> resultMap = new HashMap<>();

        List<GitHubRepository> repoList = gitHubFacade.getAllRepositories("Netcracker");
        for (GitHubRepository repo : repoList) {
            List<String> topics = repo.getTopics();
            for (String topic : topics) {
                topic = topic.toLowerCase();
                if (topic.startsWith("qubership-")) {
                    List<GitHubRepository> list = resultMap.computeIfAbsent(topic, k -> new ArrayList<>());
                    list.add(repo);
                }
            }
        }

        return resultMap;
    }
}
