package com.github.exadmin.ostm.collectors.api;

import com.github.exadmin.ostm.collectors.impl.repos.common.ListAllRepositories;
import com.github.exadmin.ostm.collectors.impl.repos.quality.NumberOfOpenedPullRequests;
import com.github.exadmin.ostm.collectors.impl.repos.quality.SonarCodeCoverage;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;
import org.junit.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class RepositoryExclusionTest {
    private static final Path ROOT = Path.of("missing-repositories");

    @Test
    public void singleChecksSkipMovedRepositoriesAndPreserveReportRowsAndOverrides() {
        GitHubRepository moved = repository("1", List.of("moved-to-monorepo"));
        GitHubRepository active = repository("2", List.of());
        GitHubRepository withoutTopics = new GitHubRepository(Map.of("id", "3", "name", "repo3"));
        GitHubFacade gitHub = facade(List.of(moved, active, withoutTopics));
        TheReportModel report = GrandReportModel.getGrandReportInstance();
        List<String> checked = new ArrayList<>();
        NumberOfOpenedPullRequests check = new NumberOfOpenedPullRequests() {
            @Override
            public TheCellValue getNumberOfOpenedPRs(GitHubRepository repository) {
                checked.add(repository.getId());
                return new TheCellValue(0, SeverityLevel.OK);
            }
        };

        new ListAllRepositories().collectDataInto(report, gitHub, ROOT);
        check.collectDataInto(report, gitHub, ROOT);

        assertEquals(List.of("2", "3"), checked);
        assertNotNull(report.findColumn(TheColumnId.COL_REPO_NAME).getValue("1"));
        TheColumn column = report.findColumn(TheColumnId.COL_REPO_OPENED_PULL_REQUESTS_COUNT);
        assertEquals(SeverityLevel.SKIP, column.getValue("1").getSeverityLevel());
        assertTrue(column.getValue("1").getToolTipText().contains("moved-to-monorepo"));
        assertFalse(column.getValue("1").getSeverityLevel().isErroneous());

        TheCellValue override = new TheCellValue("Manual", SeverityLevel.SKIP);
        column.setOverridenValue("1", override);
        check.collectDataInto(report, gitHub, ROOT);
        assertSame(override, column.getValue("1"));
        assertEquals(List.of("2", "3", "2", "3"), checked);
    }

    @Test
    public void batchChecksReceiveOnlyEligibleRepositories() {
        List<GitHubRepository> repositories = List.of(repository("1", List.of("moved-to-monorepo")),
                repository("2", List.of()));
        TheReportModel report = GrandReportModel.getGrandReportInstance();
        List<String> checked = new ArrayList<>();
        AbstractBatchRepositoryCollector check = new AbstractBatchRepositoryCollector() {
            @Override
            protected TheColumn getColumnToAddValueInto(TheReportModel model) {
                return model.findColumn(TheColumnId.COL_REPO_SONAR_CODE_COVERAGE_METRIC);
            }

            @Override
            protected void processRepositories(TheReportModel model, GitHubFacade gitHub, Path root,
                                               List<GitHubRepository> eligible, TheColumn column) {
                eligible.forEach(repository -> checked.add(repository.getId()));
                eligible.clear();
            }
        };
        check.collectDataInto(report, facade(repositories), ROOT);
        assertEquals(List.of("2"), checked);
        assertEquals(2, repositories.size());
        assertEquals(SeverityLevel.SKIP,
                report.findColumn(TheColumnId.COL_REPO_SONAR_CODE_COVERAGE_METRIC).getValue("1").getSeverityLevel());
        checked.clear();
        check.collectDataInto(report, facade(List.of(repositories.getFirst())), ROOT);
        assertTrue(checked.isEmpty());
    }

    @Test
    public void sonarDoesNotRequestDataWhenEveryRepositoryIsMoved() {
        TheReportModel report = GrandReportModel.getGrandReportInstance();
        new SonarCodeCoverage().collectDataInto(report,
                facade(List.of(repository("1", List.of("moved-to-monorepo")))), ROOT);
        assertEquals(SeverityLevel.SKIP,
                report.findColumn(TheColumnId.COL_REPO_SONAR_CODE_COVERAGE_METRIC).getValue("1").getSeverityLevel());
    }

    private static GitHubRepository repository(String id, List<String> topics) {
        return new GitHubRepository(Map.of("id", id, "name", "repo" + id,
                "html_url", "https://example.com/repo" + id, "topics", topics));
    }

    private static GitHubFacade facade(List<GitHubRepository> repositories) {
        return new GitHubFacade() {
            @Override
            public List<GitHubRepository> getAllRepositories(String ownerName) {
                return repositories;
            }
        };
    }
}
