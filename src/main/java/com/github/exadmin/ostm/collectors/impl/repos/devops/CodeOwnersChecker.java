package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/about-code-owners#codeowners-file-location
 * To use a CODEOWNERS file, create a new file called CODEOWNERS in the .github/, root, or docs/ directory of the repository,
 * in the branch where you'd like to add the code owners. If CODEOWNERS files exist in more than one of those locations,
 * GitHub will search for them in that order and use the first one it finds.
 */
public class CodeOwnersChecker extends AFilesContentChecker {
    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_CODE_OWNERS_FILE);
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        List<String> loginsWhiteList = gitHubFacade.getLoginsOfTheTeam();

        List<String> paths = List.of(".github", "", "docs");
        for (String subPath : paths) {
            Path codeOwnersFilePath = Paths.get(repoDirectory.toString(), subPath, "CODEOWNERS");
            File codeOwnersFile = codeOwnersFilePath.toFile();

            if (!codeOwnersFile.exists() || !codeOwnersFile.isFile()) continue;

            // file is found - let's check its content
            try {
                String content = FileUtils.readFile(codeOwnersFilePath);
                return verifyCodeOwnersOwerWhiteList(content, loginsWhiteList);
            } catch (Exception ex) {
                getLog().error("Error while reading CODEOWNERS file {}", codeOwnersFilePath, ex);
                return new TheCellValue("Internal Error", 1, SeverityLevel.ERROR);
            }
        }

        return new TheCellValue("Not found", 0, SeverityLevel.ERROR);
    }

    private static final Pattern REGEXP_CATCH_LOGIN = Pattern.compile("@([^\\s]+)");

    private TheCellValue verifyCodeOwnersOwerWhiteList(String fileContent, List<String> loginsWhiteList) {
        List<String> unknownLogins = new ArrayList<>();

        List<String> lines = fileContent.lines().toList();
        for (String line : lines) {
            line = line.trim();

            // remove commented part
            int chIndex = line.indexOf("#");
            if (chIndex >= 0) line = line.substring(0, chIndex);

            line = line.trim();
            if (line.isEmpty()) continue;



            Matcher matcher = REGEXP_CATCH_LOGIN.matcher(line);
            while (matcher.find()) {
                String foundLogin = matcher.group(1);
                if (!loginsWhiteList.contains(foundLogin)) {
                    unknownLogins.add(foundLogin);
                }
            }
        }

        if (unknownLogins.isEmpty()) {
            return new TheCellValue("Ok", 3, SeverityLevel.OK);
        }

        StringBuilder sb = new StringBuilder("Unknown GitHub users:<br>");
        for (String next : unknownLogins) {
            sb.append("@").append(next).append("<br>");
        }

        return new TheCellValue("Warn", 2, SeverityLevel.WARN).withTooltip(sb.toString());
    }
}
