package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.git.GitFacade;
import com.github.exadmin.ostm.git.GitRepository;
import com.github.exadmin.ostm.uimodel.TheCellValue;
import com.github.exadmin.ostm.uimodel.TheColumn;
import com.github.exadmin.ostm.uimodel.TheColumnId;
import com.github.exadmin.ostm.uimodel.TheReportModel;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class ProfanityChecker extends AFilesContentChecker {
    private static final Pattern REGEXP = Pattern.compile("\\buses\\s*:\\s*IEvangelist/profanity-filter@", Pattern.CASE_INSENSITIVE);

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_PROFANITY_ACTION);
    }

    @Override
    protected TheCellValue checkOneRepository(GitRepository repo, GitFacade gitFacade, Path repoDirectory) {
        Path filePath = findYamlFile(repoDirectory, ".github", "workflows", "profanity-filter.yaml");
        String httpRef = repo.getHttpReferenceToFileInGitHub("/.github/workflows/" + filePath.getFileName());

        return checkOneFileForContent(filePath, httpRef, REGEXP);
    }
}
