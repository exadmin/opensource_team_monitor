package com.github.exadmin.ostm.collectors.impl.repos.security;

import com.github.exadmin.ostm.collectors.impl.repos.devops.AFilesContentChecker;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GrandReportExclusionsChecker extends AFilesContentChecker {
    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        Path expFilePath = Paths.get(repoDirectory.toString(), ".qubership", "grand-report.json");
        File expFile = expFilePath.toFile();

        // String htmlUrl = repo.getHttpReferenceToFileInGitHub("/LICENSE");

        // return error if file is not found
        if (!expFile.exists() || !expFile.isFile()) {
            return new TheCellValue("Not found", 1, SeverityLevel.ERROR);
        }

        if (expFile.length() == 0) {
            return new TheCellValue("File is empty", 2, SeverityLevel.WARN);
        }

        return  new TheCellValue("Ok", 3, SeverityLevel.OK);
    }

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_SECURITY_GRAND_REPORT);
    }
}
