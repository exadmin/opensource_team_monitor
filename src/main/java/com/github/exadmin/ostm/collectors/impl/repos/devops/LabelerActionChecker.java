package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.TheCellValue;
import com.github.exadmin.ostm.uimodel.TheColumn;
import com.github.exadmin.ostm.uimodel.TheColumnId;
import com.github.exadmin.ostm.uimodel.TheReportModel;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class LabelerActionChecker extends AFilesContentChecker {
    private static final Pattern REGEXP1 = Pattern.compile("\\buses\\s*:\\s*mauroalderete/action-assign-labels@", Pattern.CASE_INSENSITIVE);
    private static final Pattern REGEXP2 = Pattern.compile("\\buses\\s*:\\s*Netcracker/qubership-workflow-hub/\\.github/workflows/auto-labeler\\.yaml@", Pattern.CASE_INSENSITIVE);

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_LABELER);
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        Path filePath = findYamlFile(repoDirectory, ".github", "workflows", "automatic-pr-labeler.yaml");
        String httpRef = repo.getHttpReferenceToFileInGitHub("/.github/workflows/" + filePath.getFileName());

        return checkOneFileForContent(filePath, httpRef, REGEXP1);
    }
}
