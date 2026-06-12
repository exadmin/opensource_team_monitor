package com.github.exadmin.ostm.collectors.impl.repos.quality;

import com.github.exadmin.ostm.collectors.impl.repos.devops.AFilesContentChecker;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class AllowedEmailsChecker extends AFilesContentChecker {
    // Align RegExp with CyberFerret Dictionary
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Z0-9._+-]+@[A-Z]+\\.[A-Z]{2,}\\b", Pattern.CASE_INSENSITIVE);
    private static final Set<String> ALLOWED_EMAILS = Set.of(
            "opensourcegroup@netcracker.com",
            "apisupport@netcracker.com",
            "example@example.com"
    );

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_EMAIL_IS_GOOD);
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        getLog().info("Processing " + repoDirectory);

        if (!repoDirectory.toFile().exists()) {
            return new TheCellValue("Was not downloaded", 0, SeverityLevel.ERROR);
        }

        int notAllowedEmailsCount = 0;

        try (Stream<Path> stream = Files.walk(repoDirectory)) {
            List<Path> files = stream.filter(Files::isRegularFile).toList();
            notAllowedEmailsCount = files
                    .parallelStream()
                    .filter(filePath -> {
                        if (FileUtils.isBinaryFile(filePath)) return false;

                        String content = FileUtils.readFile(filePath);
                        return content.contains("@");
                    })
                    .mapToInt(filePath -> {
                        String content = FileUtils.readFile(filePath);
                        Matcher matcher = EMAIL_PATTERN.matcher(content);
                        int count = 0;
                        while (matcher.find()) {
                            String email = matcher.group().toLowerCase();
                            if (!ALLOWED_EMAILS.contains(email)) {
                                count++;
                            }
                        }
                        return count;
                    })
                    .sum();
        } catch (IOException ex) {
            getLog().error("Error while processing repo {}", repo, ex);
            return new TheCellValue("Exception", SeverityLevel.ERROR);
        }

        if (notAllowedEmailsCount == 0) {
            return new TheCellValue("Ok", SeverityLevel.OK);
        }

        return new TheCellValue("" + notAllowedEmailsCount, notAllowedEmailsCount, SeverityLevel.ERROR);
    }
}
