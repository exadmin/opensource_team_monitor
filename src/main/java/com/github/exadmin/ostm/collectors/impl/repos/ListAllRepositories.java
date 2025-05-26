package com.github.exadmin.ostm.collectors.impl.repos;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.MiscUtils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListAllRepositories extends AbstractCollector {

    @Override
    public void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        final TheColumn colRepoNumber = theReportModel.findColumn(TheColumId.COL_REPO_NUMBER);
        final TheColumn colRepoName = theReportModel.findColumn(TheColumId.COL_REPO_NAME);

        // Collect known repositories into the map
        List<GitHubRepository> allRepos = gitHubFacade.getAllRepositories("Netcracker");
        Map<String, String> repoNames = new HashMap<>();
        Map<String, String> repoRefs  = new HashMap<>();
        for (GitHubRepository nextRepo : allRepos) {
            repoNames.put(nextRepo.getId(), nextRepo.getName().toLowerCase());
            repoRefs.put(nextRepo.getId(), nextRepo.getUrl());
        }

        // Sort map by repository name
        repoNames = MiscUtils.sortMapByValues(repoNames, Map.Entry.comparingByValue());

        int number = 1;
        for (Map.Entry<String, String> me : repoNames.entrySet()) {
            String refToRepo = repoRefs.get(me.getKey());

            colRepoNumber.addValue(me.getKey(), new TheCellValue(number, number, SeverityLevel.INFO));
            colRepoName.addValue(me.getKey(), new TheCellValue(me.getValue(), me.getValue(), SeverityLevel.INFO).withHttpReference(refToRepo));

            number++;
        }
    }
}
