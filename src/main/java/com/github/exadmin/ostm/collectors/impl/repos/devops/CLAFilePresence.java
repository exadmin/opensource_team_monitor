package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;

import java.nio.file.Path;
import java.util.List;

public class CLAFilePresence extends AbstractCollector {
    @Override
    public void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        TheColumn column = theReportModel.findColumn(TheColumId.COL_REPO_CLA_FILE);

        List<GitHubRepository> allRepos = gitHubFacade.getAllRepositories("Netcracker");
        for (GitHubRepository repo : allRepos) {
            TheCellValue cellValue = new TheCellValue("tbd", "0", SeverityLevel.INFO);
            column.addValue(repo.getId(), cellValue);
        }
    }
}
