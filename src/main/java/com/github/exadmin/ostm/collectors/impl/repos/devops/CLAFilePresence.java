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

public class CLAFilePresence extends AFilesContentChecker {
    private static final Pattern REQUIRED_CONTENT_APPROACH1 = Pattern.compile("\\buses\\s*:\\s*Netcracker/qubership-workflow-hub/.github/workflows/cla.yaml@", Pattern.CASE_INSENSITIVE);

    private static final Pattern REQUIRED_CONTENT_APPROACH2_PART1 = Pattern.compile("\\buses\\s*:\\s*contributor-assistant\\/github-action@");
    private static final Pattern REQUIRED_CONTENT_APPROACH2_PART2 = Pattern.compile("\\bpath-to-document\\s*:\\s*'https:\\/\\/github.com\\/Netcracker\\/qubership-github-workflows\\/blob\\/main\\/CLA\\/cla.md'");


    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_CLA_FILE);
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        Path claFilePath = Paths.get(repoDirectory.toString(), ".github", "workflows", "cla.yaml");
        File claFile = claFilePath.toFile();

        String httpRef = repo.getHttpReferenceToFileInGitHub("/.github/workflows/cla.yaml");

        if (!claFile.exists() || !claFile.isFile()) {
            return new TheCellValue("Not found", 0, SeverityLevel.ERROR);
        }

        try {
            String content = FileUtils.readFile(claFilePath.toString());

            // check if cla.yaml is realized via reference "uses: ..."
            {
                Matcher matcher = REQUIRED_CONTENT_APPROACH1.matcher(content);
                if (matcher.find()) {
                    return new TheCellValue("Ok", 3, SeverityLevel.OK).withHttpReference(httpRef);
                }
            }

            // check if cla.yaml is realized via copy-paste of original content
            {
                Matcher matcher = REQUIRED_CONTENT_APPROACH2_PART1.matcher(content);
                if (matcher.find()) {
                    Matcher matcher2 = REQUIRED_CONTENT_APPROACH2_PART2.matcher(content);
                    if (matcher2.find()) {
                        return new TheCellValue("Ok", 4, SeverityLevel.OK).withHttpReference(httpRef);
                    }
                }
            }

            return new TheCellValue("Unexpected content", 2, SeverityLevel.WARN).withHttpReference(httpRef);
        } catch (Exception ex) {
            getLog().error("Error while reading cla-file {}", claFilePath, ex);
            return new TheCellValue("Error", 1, SeverityLevel.ERROR);
        }

    }
}
