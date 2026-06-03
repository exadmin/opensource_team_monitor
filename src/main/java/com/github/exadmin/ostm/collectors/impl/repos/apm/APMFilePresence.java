package com.github.exadmin.ostm.collectors.impl.repos.apm;

import com.github.exadmin.ostm.collectors.impl.repos.devops.AFilesContentChecker;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.SeverityLevel;
import com.github.exadmin.ostm.uimodel.TheCellValue;
import com.github.exadmin.ostm.uimodel.TheColumn;
import com.github.exadmin.ostm.uimodel.TheReportModel;
import com.github.exadmin.ostm.utils.FileUtils;

import java.io.File;
import java.nio.file.Path;

import static com.github.exadmin.ostm.uimodel.TheColumnId.COL_APM_FILE;

public class APMFilePresence extends AFilesContentChecker {

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(COL_APM_FILE);
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        Path claFilePath = findYamlFile(repoDirectory, "apm.yml");
        File claFile = claFilePath.toFile();

        String httpRef = repo.getHttpReferenceToFileInGitHub("/" + claFilePath.getFileName().toString());

        if (!claFile.exists() || !claFile.isFile()) {
            return new TheCellValue("Not found", SeverityLevel.ERROR).withHttpReference(httpRef);
        }


        try {
            String content = FileUtils.readFile(claFilePath);
            if (content.isEmpty()) {
                return new TheCellValue("No content", SeverityLevel.WARN).withHttpReference(httpRef);
            }
        } catch (Exception ex) {
            getLog().error("Error while processing repo " + repo, ex);
            return new TheCellValue("Exception", SeverityLevel.ERROR).withHttpReference(httpRef);
        }

        return new TheCellValue("Ok", SeverityLevel.OK).withHttpReference(httpRef);
    }
}
