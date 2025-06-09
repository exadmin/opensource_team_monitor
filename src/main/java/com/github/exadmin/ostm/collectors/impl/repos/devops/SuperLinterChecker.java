package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class SuperLinterChecker extends AFilesContentChecker {
    private static final Pattern REGEXP_FOR_LINTER = Pattern.compile("\\buses\\s*:\\s*super-linter/super-linter(/slim)?@", Pattern.CASE_INSENSITIVE);

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_LINTER);
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        // check super linter
        {
            Path linterPath = Paths.get(repoDirectory.toString(), ".github", "workflows", "super-linter.yaml");
            String httpRef = repo.getHttpReferenceToFileInGitHub("/.github/workflows/super-linter.yaml");
            TheCellValue linterCheckResult = checkOneFileForContent(linterPath, httpRef, REGEXP_FOR_LINTER);
            if (linterCheckResult.getSeverityLevel().equals(SeverityLevel.OK) || linterCheckResult.getSeverityLevel().equals(SeverityLevel.WARN))
                return linterCheckResult;
        }

        // check prettier
        {
            Path prettierPath = Paths.get(repoDirectory.toString(), ".github", "workflows", "prettier.yaml");
            String httpRef = repo.getHttpReferenceToFileInGitHub("/.github/workflows/prettier.yaml");
            return checkOneFileForContent(prettierPath, httpRef, null);
        }
    }
}
