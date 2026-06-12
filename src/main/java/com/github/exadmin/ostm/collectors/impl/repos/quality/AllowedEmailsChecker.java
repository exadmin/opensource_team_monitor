package com.github.exadmin.ostm.collectors.impl.repos.quality;

import com.github.exadmin.ostm.collectors.impl.repos.devops.AFilesContentChecker;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        int[] notAllowedEmailsCount = {0};

        try {
            Files.walkFileTree(repoDirectory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (".git".equals(dir.getFileName().toString())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (attrs.isRegularFile()) {
                        notAllowedEmailsCount[0] += countNotAllowedEmails(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    getLog().warn("Cannot visit file {}", file, exc);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception ex) {
            getLog().error("Error while processing repo {}", repo, ex);
            return new TheCellValue("Exception", 100000, SeverityLevel.ERROR);
        }

        if (notAllowedEmailsCount[0] == 0) {
            return new TheCellValue("Ok", 0, SeverityLevel.OK);
        }

        return new TheCellValue("" + notAllowedEmailsCount[0], notAllowedEmailsCount[0], SeverityLevel.ERROR);
    }

    private int countNotAllowedEmails(Path filePath) {
        if (FileUtils.isBinaryFile(filePath)) return 0;

        String content = FileUtils.readFile(filePath);
        if (!content.contains("@")) return 0;

        Matcher matcher = EMAIL_PATTERN.matcher(content);
        int count = 0;
        while (matcher.find()) {
            String email = matcher.group().toLowerCase();
            if (!ALLOWED_EMAILS.contains(email)) {
                count++;
            }
        }

        return count;
    }
}
