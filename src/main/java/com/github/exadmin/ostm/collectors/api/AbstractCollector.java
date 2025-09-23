package com.github.exadmin.ostm.collectors.api;

import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.persistence.overrides.JsonOverridenValue;
import com.github.exadmin.ostm.persistence.overrides.JsonRepoColumn;
import com.github.exadmin.ostm.persistence.overrides.JsonReportOverrides;
import com.github.exadmin.ostm.uimodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

abstract class AbstractCollector {
    private Logger log;

    public final void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {

        List<GitHubRepository> allRepositories = gitHubFacade.getAllRepositories("Netcracker");

        JsonReportOverrides overrides = theReportModel.getReportOverrides();
        if (overrides != null) {
            for (JsonRepoColumn jCol : overrides.getColumns()) {
                TheColumnId theColumnId = TheColumnId.findById(jCol.getColumnId());
                TheColumn theColumn = theReportModel.findColumn(theColumnId);

                for (JsonOverridenValue jVal : jCol.getRepositories()) {
                    String repoId = getRepoId(jVal.getRepoName(), allRepositories);

                    theColumn.setOverridenValue(repoId, new TheCellValue(jVal.getVisualValue(), jVal.getSortByValue(), SeverityLevel.SKIP));
                }
            }
        }

        collectDataIntoImpl(theReportModel, gitHubFacade, parentPathForClonedRepositories);
    }

    public abstract void collectDataIntoImpl(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories);

    protected Logger getLog() {
        if (log == null) {
            log = LoggerFactory.getLogger(this.getClass());
        }

        return log;
    }

    private static String getRepoId(String repoShortName, List<GitHubRepository> allRepos) {
        for (GitHubRepository repo : allRepos) {
            if (repo.getName().equals(repoShortName)) return repo.getId();
        }

        return null;
    }
}
