package com.github.exadmin.ostm.collectors.impl.repos.security;

import com.github.exadmin.ostm.collectors.impl.repos.devops.AFilesContentChecker;
import com.github.exadmin.ostm.github.badwords.AttentionSignaturesManager;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.FileUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// todo: optimize checking by using: "git rev-parse --short HEAD"
public class AttentionSignaturesChecker extends AFilesContentChecker {

    private static final List<String> IGNORED_EXTS = new ArrayList<>();
    static {
        IGNORED_EXTS.add(".png");
        IGNORED_EXTS.add(".gif");
        IGNORED_EXTS.add(".jpg");
        IGNORED_EXTS.add(".bmp");
        IGNORED_EXTS.add(".ico");
    }

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumnId.COL_REPO_SEC_BAD_WORDS_CHECKER);
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        if ("disable".equalsIgnoreCase(System.getenv("BWC"))) return new TheCellValue("Disabled", 0, SeverityLevel.WARN);

        Map<String, Pattern> badMap = AttentionSignaturesManager.getBadMap();

        List<String> allFiles = FileUtils.findAllFilesRecursively(repoDirectory.toString(), shortFileName -> {
            for (String ext : IGNORED_EXTS) {
                if (shortFileName.endsWith(ext)) return false;
            }

            return true;
        });

        Set<String> foundIds = ConcurrentHashMap.newKeySet();
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        for (String nextFileName : allFiles) {
            if (badMap.isEmpty()) break; // no signatures to search for

            final String fileContent;
            try {
                fileContent = FileUtils.readFile(nextFileName);
            } catch (Exception ex) {
                getLog().error("Error while reading file content of {}", nextFileName, ex);
                return new TheCellValue("Internal error", 1, SeverityLevel.ERROR);
            }

            CompletableFuture<?>[] futures = badMap.entrySet().stream()
                    .map(me -> CompletableFuture.supplyAsync( () ->
                            {
                                Matcher matcher = me.getValue().matcher(fileContent);
                                if (matcher.find()) {
                                    if (approveFoundPattern(nextFileName, fileContent, me.getKey(), me.getValue(), matcher)) {
                                        foundIds.add(me.getKey());
                                        getLog().debug("Pattern-id {} was found in the file {}, start = {}, end = {}", me.getKey(), nextFileName, matcher.start(), matcher.end());
                                    } else {
                                        getLog().debug("Pattern-id {} was skipped for the file {}", me.getKey(), nextFileName);
                                    }
                                }
                                    return null;
                            }, executor)
                    )
                    .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();

            badMap.keySet().removeAll(foundIds); // reduce number of signatures to work with in scope of this repository
        }

        if (!foundIds.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String foundId : foundIds) {
                sb.append("&nbsp;&nbsp;").append(foundId).append("<br>");
            }
            return new TheCellValue(sb.toString(), 2, SeverityLevel.ERROR);
        }

        return new TheCellValue("Ok", 0, SeverityLevel.OK);
    }

    /**
     * Makes double-check - if found value is not false-positive.
     * For instance, there is "IP-Address" reg-exp, but some addresses are valid to be published in the repository.
     * This listener allows not make additional check without complication of initial regexp.
     * @param filePath
     * @param fileContent
     * @param patternId
     * @param regExp
     * @param matcher
     * @return
     */
    protected boolean approveFoundPattern(String filePath, String fileContent, String patternId, Pattern regExp, Matcher matcher) {
        // analyze IP addresses for false-positives
        if ("OTH-IP-ADDR".equals(patternId)) {
            String ipAddressValue = matcher.group();
            getLog().debug("Checking IP Address value = {}", ipAddressValue);
            if ("0.0.0.0".equals(ipAddressValue) || "127.0.0.1".equals(ipAddressValue)) return false;
        }

        if ("INT-004".equals(patternId)) {
            String value = matcher.group().toLowerCase();
            if ("pages.netcracker.com".equalsIgnoreCase(value)) return false;
        }

        if ("INT-006".equals(patternId)) {
            String value = matcher.group().toLowerCase();
            if ("opensourcegroup@netcracker.com".equals(value)) return false;
        }

        return true;
    }
}
