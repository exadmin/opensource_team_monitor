package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.github.badwords.BadWordsManager;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.FileUtils;

import java.nio.file.Path;
import java.util.*;

// todo: optimize checking by using: "git rev-parse --short HEAD"
public class BadWordsChecker extends AFilesContentChecker {

    private static final List<String> IGNORED_EXTS = new ArrayList<>();
    static {
        IGNORED_EXTS.add(".png");
        IGNORED_EXTS.add(".gif");
        IGNORED_EXTS.add(".jpg");
        IGNORED_EXTS.add(".bmp");
    }

    @Override
    protected TheColumn getColumnToAddValueInto(TheReportModel theReportModel) {
        return theReportModel.findColumn(TheColumId.COL_REPO_SEC_BAD_WORDS_CHECKER);
    }

    @Override
    protected TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory) {
        Map<String, String> badMap = BadWordsManager.getBadMap();

        List<String> allFiles = FileUtils.findAllFilesRecursively(repoDirectory.toString(), shortFileName -> {
            for (String ext : IGNORED_EXTS) {
                if (shortFileName.endsWith(ext)) return false;
            }

            return true;
        });

        Set<String> foundIds = new HashSet<>();

        for (String nextFileName : allFiles) {
            if (badMap.isEmpty()) break; // no signatures to search for

            String fileContent = "";
            try {
                fileContent = FileUtils.readFile(nextFileName);
            } catch (Exception ex) {
                getLog().error("Error while reading file content of {}", nextFileName, ex);
                return new TheCellValue("Internal error", 1, SeverityLevel.ERROR);
            }

            for (Map.Entry<String, String> me : badMap.entrySet()) {
                if (fileContent.contains(me.getValue())) {
                    foundIds.add(me.getKey());
                }
            }

            badMap.keySet().removeAll(foundIds); // reduce number of signatures
        }

        if (!foundIds.isEmpty()) {
            StringBuilder sb = new StringBuilder("Bad words found:<br>");
            for (String foundId : foundIds) {
                sb.append("&nbsp;&nbsp;").append(foundId).append("<br>");
            }
            return new TheCellValue(sb.toString(), 2, SeverityLevel.ERROR);
        }

        return new TheCellValue("Ok", 0, SeverityLevel.OK);
    }
}
