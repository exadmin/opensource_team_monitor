package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.FileUtils;
import com.github.exadmin.ostm.utils.MiscUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LicenseFilePresence extends AbstractCollector {
    // SHA256 hash of "https://www.apache.org/licenses/LICENSE-2.0.txt" trimmed string.
    private static final String LICENSE_SHA256_BASE64_EXP_VALUE_v1 = "KD6mzCmXoacNoASeCa35MXu2DKG1Enm2UZa4OmnhmWs=";

    @Override
    public void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        TheColumn column = theReportModel.findColumn(TheColumId.COL_REPO_LICENSE_FILE);

        List<GitHubRepository> allRepos = gitHubFacade.getAllRepositories("Netcracker");
        for (GitHubRepository repo : allRepos) {
            TheCellValue cellValue = checkFileForRepository(repo, parentPathForClonedRepositories);
            column.addValue(repo.getId(), cellValue);
        }
    }

    private TheCellValue checkFileForRepository(GitHubRepository repo, Path parentPathForClonedRepositories) {
        String repoName = repo.getName();

        Path licenseFilePath = Paths.get(parentPathForClonedRepositories.toString(), repoName, "LICENSE");
        File licenseFile = licenseFilePath.toFile();

        // return error if file is not found
        if (!licenseFile.exists() || !licenseFile.isFile()) {
            return new TheCellValue("Not found", 0, SeverityLevel.ERROR);
        }

        // check file content
        try {
            String fileContent = FileUtils.readFile(licenseFile.toString());
            fileContent = fileContent.trim();
            String sha256 = MiscUtils.getSHA256FromString(fileContent);

            if (LICENSE_SHA256_BASE64_EXP_VALUE_v1.equals(sha256)) {
                return new TheCellValue("Apache 2.0", 3, SeverityLevel.OK);
            } else {
                return new TheCellValue("Non Apache 2.0", 2, SeverityLevel.WARN);
            }
        } catch (IOException ex) {
            getLog().error("Error while reading license file {}", licenseFile, ex);
            return new TheCellValue("Error while reading", 1, SeverityLevel.ERROR);
        }

    }
}
