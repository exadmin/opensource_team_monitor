package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class BuildOnCommit extends AFilesContentChecker {
    private static final Pattern REGEXP_MAVEN_BUILD = Pattern.compile("\\bon\\s*:\\s+push\\s*:", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
    private static final Pattern REGEXP_GO_BUILD = Pattern.compile("\\bon\\s*:\\s+push\\s*:", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_BUILD_ON_COMMIT);
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        // check as maven-based repository
        {
            Path filePath = findYamlFile(repoDirectory,  ".github", "workflows", "maven-build.yaml");
            String httpRef = repo.getHttpReferenceToFileInGitHub("/.github/workflows/" + filePath.getFileName());
            TheCellValue value = checkOneFileForContent(filePath, httpRef, REGEXP_MAVEN_BUILD);
            if (SeverityLevel.OK.equals(value.getSeverityLevel()) || SeverityLevel.WARN.equals(value.getSeverityLevel()))
                return value;
        }

        // check as go-based repository
        Path filePath = findYamlFile(repoDirectory, ".github", "workflows", "go-build.yaml");
        String httpRef = repo.getHttpReferenceToFileInGitHub("/.github/workflows/" + filePath.getFileName());
        return checkOneFileForContent(filePath, httpRef, REGEXP_GO_BUILD);
    }
}
