package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConventionalCommitsActionChecker extends AFilesContentChecker {
    private static final Pattern REGEXP = Pattern.compile("\\buses\\s*:\\s*webiny/action-conventional-commits@", Pattern.CASE_INSENSITIVE);

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_CONVENTIONAL_COMMITS_ACTION);
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        Path filePath = findYamlFile(repoDirectory, ".github", "workflows", "pr-conventional-commits.yaml");
        File file = filePath.toFile();

        if (!file.exists() || !file.isFile()) {
            return new TheCellValue("Not found", 0, SeverityLevel.ERROR);
        }

        String httpRef = repo.getHttpReferenceToFileInGitHub("/.github/workflows/" + filePath.getFileName());

        try {
            String fileContent = FileUtils.readFile(filePath);
            Matcher matcher = REGEXP.matcher(fileContent);
            if (matcher.find()) {
                return new TheCellValue("Ok", 3, SeverityLevel.OK).withHttpReference(httpRef);
            } else {
                return new TheCellValue("Unexpected content", 2, SeverityLevel.WARN).withHttpReference(httpRef);
            }
        } catch (Exception ex) {
            getLog().error("Error while reading file {}", filePath, ex);
            return new TheCellValue("Internal error", 1, SeverityLevel.ERROR);
        }
    }
}
