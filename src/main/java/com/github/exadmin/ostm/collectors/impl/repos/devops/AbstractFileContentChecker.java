package com.github.exadmin.ostm.collectors.impl.repos.devops;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.TheCellValue;
import com.github.exadmin.ostm.uimodel.TheColumn;
import com.github.exadmin.ostm.uimodel.TheReportModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class AbstractFileContentChecker extends AbstractCollector {
    @Override
    public final void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        TheColumn column = getColumnToAddValueInto(theReportModel);

        List<GitHubRepository> allRepos = gitHubFacade.getAllRepositories("Netcracker");
        for (GitHubRepository repo : allRepos) {
            String repoName = repo.getName();
            Path repoDirectory = Paths.get(parentPathForClonedRepositories.toString(), repoName);

            TheCellValue cellValue = checkOneRepository(repo, gitHubFacade, repoDirectory);
            column.addValue(repo.getId(), cellValue);
        }
    }

    protected abstract TheColumn getColumnToAddValueInto(TheReportModel theReportModel);

    protected abstract TheCellValue checkOneRepository(GitHubRepository repo, GitHubFacade gitHubFacade, Path repoDirectory);
}
