package com.github.exadmin.ostm.collectors.api;

import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.persistence.overrides.JsonOverridenValue;
import com.github.exadmin.ostm.persistence.overrides.JsonReportOverrides;
import com.github.exadmin.ostm.uimodel.SeverityLevel;
import com.github.exadmin.ostm.uimodel.TheCellValue;
import com.github.exadmin.ostm.uimodel.TheColumn;
import com.github.exadmin.ostm.uimodel.TheReportModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class AbstractOneRepositoryCollector extends AbstractCollector {
    @Override
    public final void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        List<GitHubRepository> allRepositories = gitHubFacade.getAllRepositories("Netcracker");
        for (GitHubRepository repo : allRepositories) {
            // here we need decide first if current check and repository exists in the grand-report-overrides settings
            TheColumn column = getColumnToAddValueInto(theReportModel);
            JsonReportOverrides overrides = theReportModel.getReportOverrides();
            if (overrides != null) {
                JsonOverridenValue value = overrides.findOverridenValue(column.getId(), repo.getName());
                if (value != null) {
                    TheCellValue overridenValue = new TheCellValue(value.getVisualValue(), value.getSortByValue(), SeverityLevel.SKIP);
                    column.addValue(repo.getId(), overridenValue);
                    return;
                }
            }

            // continue check
            Path repoPath = Paths.get(parentPathForClonedRepositories.toString(), repo.getName());
            processRepository(theReportModel, gitHubFacade, repoPath, repo, column);
        }
    }

    protected abstract void processRepository(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path repositoryPath, GitHubRepository repository, TheColumn column);

    protected abstract TheColumn getColumnToAddValueInto(TheReportModel theReportModel);
}
