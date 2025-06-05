package com.github.exadmin.ostm.collectors.api;

import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.TheReportModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class AbstractOneRepositoryCollector extends AbstractCollector {
    @Override
    public void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        List<GitHubRepository> allRepositories = gitHubFacade.getAllRepositories("Netcracker");
        for (GitHubRepository repo : allRepositories) {
            Path repoPath = Paths.get(parentPathForClonedRepositories.toString(), repo.getName());
            processRepository(theReportModel, gitHubFacade, repoPath, repo);
        }
    }

    protected abstract void processRepository(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path repositoryPath, GitHubRepository repository);
}
