package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;

import java.io.File;
import java.nio.file.Path;

public class ReadmeFilePresence extends AFilesContentChecker {
    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_README_FILE);
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        // there can be readme files in different letter case
        File dir = repoDirectory.toFile();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File readmeFile : files) {
                if (readmeFile.isFile() && readmeFile.exists()) {
                    String fileName = readmeFile.getAbsolutePath();
                    if (fileName.toLowerCase().endsWith("readme.md")) {
                        String httpRef = repo.getHttpReferenceToFileInGitHub("/" + readmeFile.getName());

                        // if README file has very small size
                        long fileSize = readmeFile.length();
                        if (fileSize < 1024) {
                            return new TheCellValue("Too small: " + fileSize, fileSize, SeverityLevel.ERROR).withHttpReference(httpRef);
                        }

                        return new TheCellValue(fileSize, fileSize, SeverityLevel.OK).withHttpReference(httpRef);
                    }
                }
            }
        }

        return new TheCellValue("Not found", 0, SeverityLevel.ERROR);
    }
}
