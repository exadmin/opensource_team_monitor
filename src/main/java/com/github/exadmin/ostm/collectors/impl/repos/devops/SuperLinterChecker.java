package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.git.GitFacade;
import com.github.exadmin.ostm.git.GitRepository;
import com.github.exadmin.ostm.uimodel.*;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class SuperLinterChecker extends AFilesContentChecker {
    private static final Pattern REGEXP_FOR_LINTER = Pattern.compile("\\buses\\s*:\\s*super-linter/super-linter(/slim)?@", Pattern.CASE_INSENSITIVE);

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_LINTER);
    }

    @Override
    protected TheCellValue checkOneRepository(GitRepository repo, GitFacade gitFacade, Path repoDirectory) {
        // check super linter
        {
            Path linterPath = findYamlFile(repoDirectory, ".github", "workflows", "super-linter.yaml");
            String httpRef = repo.getHttpReferenceToFileInGitHub("/.github/workflows/" + linterPath.getFileName());
            TheCellValue linterCheckResult = checkOneFileForContent(linterPath, httpRef, REGEXP_FOR_LINTER);
            if (linterCheckResult.getSeverityLevel().equals(SeverityLevel.OK) || linterCheckResult.getSeverityLevel().equals(SeverityLevel.WARN))
                return linterCheckResult;
        }

        // check prettier
        {
            Path prettierPath = findYamlFile(repoDirectory, ".github", "workflows", "prettier.yaml");
            String httpRef = repo.getHttpReferenceToFileInGitHub("/.github/workflows/" + prettierPath.getFileName());
            return checkOneFileForContent(prettierPath, httpRef, null);
        }
    }
}
