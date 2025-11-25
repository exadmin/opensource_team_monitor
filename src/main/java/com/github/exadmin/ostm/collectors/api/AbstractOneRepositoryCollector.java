package com.github.exadmin.ostm.collectors.api;

import com.github.exadmin.ostm.git.GitFacade;
import com.github.exadmin.ostm.git.GitRepository;
import com.github.exadmin.ostm.uimodel.TheColumn;
import com.github.exadmin.ostm.uimodel.TheReportModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class AbstractOneRepositoryCollector extends AbstractCollector {
    @Override
    public final void collectDataIntoImpl(TheReportModel theReportModel, GitFacade gitFacade, Path parentPathForClonedRepositories) {
        final TheColumn column = getColumnToAddValueInto(theReportModel);

        List<GitRepository> allRepositories = gitFacade.getAllRepositories("Netcracker");
        for (GitRepository repo : allRepositories) {
//            // here we need decide first if current check and repository exists in the grand-report-overrides settings
//
//            JsonReportOverrides overrides = theReportModel.getReportOverrides();
//            if (overrides != null) {
//                JsonOverridenValue value = overrides.findOverridenValue(column.getId(), repo.getName());
//                if (value != null) {
//                    TheCellValue overridenValue = new TheCellValue(value.getVisualValue(), value.getSortByValue(), SeverityLevel.SKIP);
//                    column.setValue(repo.getId(), overridenValue);
//                    continue;
//                }
//            }

            // continue check
            Path repoPath = Paths.get(parentPathForClonedRepositories.toString(), repo.getName());
            processRepository(theReportModel, gitFacade, repoPath, repo, column);
        }
    }

    protected abstract void processRepository(TheReportModel theReportModel, GitFacade gitFacade, Path repositoryPath, GitRepository repository, TheColumn column);

    protected abstract TheColumn getColumnToAddValueInto(TheReportModel theReportModel);
}
