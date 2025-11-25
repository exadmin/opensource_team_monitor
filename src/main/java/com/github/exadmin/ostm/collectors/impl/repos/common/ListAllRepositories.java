package com.github.exadmin.ostm.collectors.impl.repos.common;

import com.github.exadmin.ostm.collectors.api.AbstractManyRepositoriesCollector;
import com.github.exadmin.ostm.git.GitFacade;
import com.github.exadmin.ostm.git.GitRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.MiscUtils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListAllRepositories extends AbstractManyRepositoriesCollector {

    @Override
    public void collectDataIntoImpl(TheReportModel theReportModel, GitFacade gitFacade, Path parentPathForClonedRepositories) {
        final TheColumn colRepoNumber = theReportModel.findColumn(TheColumnId.COL_REPO_NUMBER);
        final TheColumn colRepoName = theReportModel.findColumn(TheColumnId.COL_REPO_NAME);
        final TheColumn colRepoType = theReportModel.findColumn(TheColumnId.COL_REPO_TYPE);

        // Collect known repositories into the map
        List<GitRepository> allRepos = gitFacade.getAllRepositories("Netcracker");
        Map<String, String> repoNames           = new HashMap<>();
        Map<String, String> repoRefs            = new HashMap<>();
        Map<String, GitRepository> reposMap  = new HashMap<>();

        for (GitRepository nextRepo : allRepos) {
            reposMap.put(nextRepo.getId(), nextRepo);
            repoNames.put(nextRepo.getId(), nextRepo.getName().toLowerCase());
            repoRefs.put(nextRepo.getId(), nextRepo.getUrl());
        }

        // Sort map by repository name
        repoNames = MiscUtils.sortMapByValues(repoNames, Map.Entry.comparingByValue());

        int number = 1;
        for (Map.Entry<String, String> me : repoNames.entrySet()) {
            String refToRepo = repoRefs.get(me.getKey());

            GitRepository repo = reposMap.get(me.getKey());

            SeverityLevel repoType = SeverityLevel.INFO_PUBLIC;
            String typeText = "public";
            int sortByValue = 0;

            if (repo.isArchived()) {
                repoType = SeverityLevel.INFO_ARCHIVED;
                typeText = "archived";
                sortByValue = 1;
            }

            if (repo.isPrivate()) {
                repoType = SeverityLevel.INFO_PRIVATE;
                typeText = "private";
                sortByValue = 2;
            }

            colRepoNumber.setValue(me.getKey(), new TheCellValue(number, number, SeverityLevel.INFO));
            colRepoName.setValue(me.getKey(), new TheCellValue(me.getValue(), me.getValue(), SeverityLevel.INFO).withHttpReference(refToRepo));
            colRepoType.setValue(me.getKey(), new TheCellValue(typeText, sortByValue, repoType ));

            number++;
        }
    }
}
