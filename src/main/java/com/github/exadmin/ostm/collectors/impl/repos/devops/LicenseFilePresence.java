package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.FileUtils;
import com.github.exadmin.ostm.utils.MiscUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LicenseFilePresence extends AFilesContentChecker {
    // SHA256 hash of "https://www.apache.org/licenses/LICENSE-2.0.txt" (trimmed string)
    private static final String LICENSE_SHA256_BASE64_EXP_VALUE_v1 = "+1gg8p+90Vw4VWMz8lUTqVW1hlAbwxMr0lr2HRhKu0k=";

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        Path licenseFilePath = Paths.get(repoDirectory.toString(), "LICENSE");
        File licenseFile = licenseFilePath.toFile();

        String htmlUrl = repo.getHttpReferenceToFileInGitHub("/LICENSE");

        // return error if file is not found
        if (!licenseFile.exists() || !licenseFile.isFile()) {
            return new TheCellValue("Not found", 0, SeverityLevel.ERROR);
        }

        // check file content
        try {
            String fileContent = FileUtils.readFile(licenseFile.toPath());
            fileContent = MiscUtils.getLettersOnly(fileContent);
            String sha256 = MiscUtils.getSHA256AsBase64(fileContent);

            if (LICENSE_SHA256_BASE64_EXP_VALUE_v1.equals(sha256)) {
                return new TheCellValue("Apache 2.0", 3, SeverityLevel.OK).withHttpReference(htmlUrl);
            } else {
                return new TheCellValue("Non Apache 2.0", 2, SeverityLevel.WARN).withHttpReference(htmlUrl);
            }
        } catch (IOException ex) {
            getLog().error("Error while reading license file {}", licenseFile, ex);
            return new TheCellValue("Error while reading", 1, SeverityLevel.ERROR);
        }
    }

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_LICENSE_FILE);
    }
}
