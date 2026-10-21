package com.github.exadmin.ostm.collectors.api;

import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.TheCellValue;
import com.github.exadmin.ostm.uimodel.TheColumn;
import com.github.exadmin.ostm.uimodel.TheReportModel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Applies repository exclusions before passing an owned, mutable list to a batch check. */
public abstract class AbstractBatchRepositoryCollector extends AbstractCollector {
    @Override
    public final void collectDataIntoImpl(TheReportModel report, GitHubFacade gitHub, Path repositoriesPath) {
        TheColumn column = getColumnToAddValueInto(report);
        List<GitHubRepository> eligibleRepositories = new ArrayList<>();
        for (GitHubRepository repository : gitHub.getAllRepositories("Netcracker")) {
            TheCellValue skipped = RepositoryCheckPolicy.skippedValue(repository);
            if (skipped != null) {
                column.setValue(repository.getId(), skipped);
            } else {
                eligibleRepositories.add(repository);
            }
        }
        if (!eligibleRepositories.isEmpty()) {
            processRepositories(report, gitHub, repositoriesPath, eligibleRepositories, column);
        }
    }

    protected abstract TheColumn getColumnToAddValueInto(TheReportModel report);

    protected abstract void processRepositories(TheReportModel report, GitHubFacade gitHub, Path repositoriesPath,
                                                List<GitHubRepository> repositories, TheColumn column);
}
