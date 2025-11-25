package com.github.exadmin.ostm.collectors.impl.repos.summary;

import com.github.exadmin.ostm.collectors.api.AbstractManyRepositoriesCollector;
import com.github.exadmin.ostm.git.GitFacade;
import com.github.exadmin.ostm.git.GitRepository;
import com.github.exadmin.ostm.uimodel.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TotalErrorsCounter extends AbstractManyRepositoriesCollector {
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
        COLUMNS.add(TheColumnId.COL_REPO_SEC_SIGNATURES_CHECKER);
        COLUMNS.add(TheColumnId.COL_REPO_TOPICS);
        COLUMNS.add(TheColumnId.COL_REPO_README_FILE);
        COLUMNS.add(TheColumnId.COL_REPO_SONAR_CODE_COVERAGE_METRIC);
        COLUMNS.add(TheColumnId.COL_REPO_OPENED_PULL_REQUESTS_COUNT);
    }



    @Override
    public void collectDataIntoImpl(TheReportModel theReportModel, GitFacade gitFacade, Path parentPathForClonedRepositories) {
        final TheColumn colTotalErrors = theReportModel.findColumn(TheColumnId.COL_SUMMARY_TEAM_TOTAL_ERRORS);
        final TheColumn colTotalRepos = theReportModel.findColumn(TheColumnId.COL_SUMMARY_TEAM_TOTAL_REPOSITORIES);
        final TheColumn colErrorsPerRepo = theReportModel.findColumn(TheColumnId.COL_SUMMARY_TEAM_ERRS_PER_REPOSITORY);

        List<TheColumn> columns = new ArrayList<>();

        for (TheColumnId columnId : COLUMNS) {
            columns.add(theReportModel.findColumn(columnId));
        }

        int minErrorsPerRepoValue = Integer.MAX_VALUE;
        List<TheCellValue> listMinErrorsPerRepoCellValues = new ArrayList<>();

        Map<String, List<GitRepository>> teamsMap = getRepositoriesMappedByTeams(gitFacade);
        for (String rowIdWhichIsTeam : teamsMap.keySet()) {
            // calculate total number of errors per all repositories and metrics
            int errorsCount = 0;

            List<GitRepository> allRelatedRepositories = teamsMap.get(rowIdWhichIsTeam);
            for (GitRepository repo : allRelatedRepositories) {
                String rowIdWhichIsRepoId = repo.getId();

                for (TheColumn column : columns) {
                    TheCellValue value = column.getValue(rowIdWhichIsRepoId);
                    if (value != null && value.getSeverityLevel().isErroneous()) errorsCount++;
                }
            }

            int numOfReposPerTeam = allRelatedRepositories.size();
            int numOfErrorsPerRepo = errorsCount / numOfReposPerTeam;

            colTotalErrors.setValue(rowIdWhichIsTeam, new TheCellValue(errorsCount, errorsCount, SeverityLevel.INFO));
            colTotalRepos.setValue(rowIdWhichIsTeam, new TheCellValue(numOfReposPerTeam, numOfReposPerTeam, SeverityLevel.INFO));

            TheCellValue cvNumOfErrorsPerRepo = new TheCellValue(numOfErrorsPerRepo, numOfErrorsPerRepo, SeverityLevel.INFO);
            colErrorsPerRepo.setValue(rowIdWhichIsTeam, cvNumOfErrorsPerRepo);

            // if new minimalistic value is met
            if (numOfErrorsPerRepo < minErrorsPerRepoValue) {
                minErrorsPerRepoValue = numOfErrorsPerRepo;
                listMinErrorsPerRepoCellValues.clear();
            }

            if (numOfErrorsPerRepo == minErrorsPerRepoValue) {
                listMinErrorsPerRepoCellValues.add(cvNumOfErrorsPerRepo);
            }
        }

        // mark best/worst cell-values
        for (TheCellValue cellValue : listMinErrorsPerRepoCellValues) {
            cellValue.setSeverityLevel(SeverityLevel.PLACE1);
        }
    }

    /**
     * Returns map of repositories which belongs to the teams
     * @return Map of team-names which linked to list of repositories
     */
    private Map<String, List<GitRepository>> getRepositoriesMappedByTeams(GitFacade gitFacade) {
        Map<String, List<GitRepository>> resultMap = new HashMap<>();

        List<GitRepository> repoList = gitFacade.getAllRepositories("Netcracker");
        for (GitRepository repo : repoList) {
            List<String> topics = repo.getTopics();
            for (String topic : topics) {
                topic = topic.toLowerCase();
                if (topic.startsWith("qubership-")) {
                    List<GitRepository> list = resultMap.computeIfAbsent(topic, k -> new ArrayList<>());
                    list.add(repo);
                }
            }
        }

        return resultMap;
    }
}
