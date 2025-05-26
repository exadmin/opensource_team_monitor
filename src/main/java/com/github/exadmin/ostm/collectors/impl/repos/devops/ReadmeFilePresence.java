package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReadmeFilePresence extends AbstractFileContentChecker {
    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumId.COL_REPO_README_FILE);
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        Path readmeFilePath = Paths.get(repoDirectory.toString(), "README.md");
        File readmeFile = readmeFilePath.toFile();

        // if README file is absent
        if (!readmeFile.exists() || !readmeFile.isFile()) {
            return new TheCellValue("Not found", 0, SeverityLevel.ERROR);
        }

        // if README file has very small size
        long fileSize = readmeFile.length();
        if (fileSize < 1024) {
            return new TheCellValue("Too small: " + fileSize, fileSize, SeverityLevel.ERROR);
        }

        return new TheCellValue(fileSize, fileSize, SeverityLevel.OK);
    }
}
