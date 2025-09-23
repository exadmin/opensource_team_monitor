package com.github.exadmin.ostm.collectors.impl.repos.quality;

import com.github.exadmin.ostm.collectors.api.AbstractManyRepositoriesCollector;
import com.github.exadmin.ostm.github.api.GitHubRequest;
import com.github.exadmin.ostm.github.api.GitHubResponse;
import com.github.exadmin.ostm.github.api.HttpRequestBuilder;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.uimodel.*;
import com.github.exadmin.ostm.utils.MiscUtils;

import java.nio.file.Path;
import java.util.List;

public class NumberOfOpenedPullRequests extends AbstractManyRepositoriesCollector {
    private static final int SLEEP_BEFORE_NEXT_TRY_MILLIS = 5000;
    private static final int WARN_IF_PRS_MORE_THAN = 5;
    private static final int ERR_IF_PRS_MORE_THAN = 8;

    @Override
    public void collectDataIntoImpl(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        TheColumn column = theReportModel.findColumn(TheColumnId.COL_REPO_OPENED_PULL_REQUESTS_COUNT);

        List<GitHubRepository> allRepos = gitHubFacade.getAllRepositories("Netcracker");
        for (GitHubRepository repo : allRepos) {

            TheCellValue cellValue = getNumberOfOpenedPRs(repo);
            column.setValue(repo.getId(), cellValue);
        }
    }

    public TheCellValue getNumberOfOpenedPRs(GitHubRepository repo) {
        String url = repo.getPullsUrl();
        url = url.replaceAll("\\{/number}", "") + "?state=open";

        int attemptsCount = 3;

        while (attemptsCount > 0) {
            attemptsCount--;

            GitHubRequest request = HttpRequestBuilder.gitHubRESTCall().toURL(url).build();
            GitHubResponse response = request.execute();

            if (response.getHttpCode() == 403) {
                getLog().debug("Throttling happened. Sleep for {} ms", SLEEP_BEFORE_NEXT_TRY_MILLIS);
                MiscUtils.sleep(SLEEP_BEFORE_NEXT_TRY_MILLIS);
                continue;
            }

            if (response.getHttpCode() == 200) {
                int count = response.getDataMap().size();

                SeverityLevel severity = count > ERR_IF_PRS_MORE_THAN ? SeverityLevel.ERROR :
                        count > WARN_IF_PRS_MORE_THAN ? SeverityLevel.WARN : SeverityLevel.OK;

                return new TheCellValue(count, count, severity)
                        .withHttpReference("https://github.com/Netcracker/" + repo.getName() + "/pulls");
            }
        }

        return new TheCellValue("Error", 0, SeverityLevel.ERROR);
    }
}
