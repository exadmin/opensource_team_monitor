package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.TheCellValue;
import com.github.exadmin.ostm.uimodel.TheColumn;
import com.github.exadmin.ostm.uimodel.TheColumnId;
import com.github.exadmin.ostm.uimodel.TheReportModel;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class LintTitleActionChecker extends AFilesContentChecker {
    private static final Pattern REGEXP = Pattern.compile("\\buses\\s*:\\s*amannn/action-semantic-pull-request@", Pattern.CASE_INSENSITIVE);

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_LINT_TITLE);
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        Path filePath = findYamlFile(repoDirectory, ".github", "workflows", "pr-lint-title.yaml");
        String httpRef = repo.getHttpReferenceToFileInGitHub("/.github/workflows/" + filePath.getFileName());
        return checkOneFileForContent(filePath, httpRef, REGEXP);
    }
}
