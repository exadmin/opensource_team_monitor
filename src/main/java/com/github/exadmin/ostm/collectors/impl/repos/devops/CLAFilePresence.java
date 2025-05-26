package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CLAFilePresence extends AbstractFileContentChecker {
    private static final Pattern REQUIRED_CONTENT = Pattern.compile("\\buses\\s*:\\s*Netcracker/qubership-workflow-hub/.github/workflows/cla.yaml@", Pattern.CASE_INSENSITIVE);

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumId.COL_REPO_CLA_FILE);
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        Path claFilePath = Paths.get(repoDirectory.toString(), ".github", "workflows", "cla.yaml");
        File claFile = claFilePath.toFile();

        if (!claFile.exists() || !claFile.isFile()) {
            return new TheCellValue("Not found", 0, SeverityLevel.ERROR);
        }

        try {
            String content = FileUtils.readFile(claFilePath.toString());
            Matcher matcher = REQUIRED_CONTENT.matcher(content);
            if (matcher.find()) {
                return new TheCellValue("Ok", 3, SeverityLevel.OK);
            }

            return new TheCellValue("Unexpected content", 2, SeverityLevel.WARN);
        } catch (Exception ex) {
            getLog().error("Error while reading cla-file {}", claFilePath, ex);
            return new TheCellValue("Error", 1, SeverityLevel.ERROR);
        }

    }
}
