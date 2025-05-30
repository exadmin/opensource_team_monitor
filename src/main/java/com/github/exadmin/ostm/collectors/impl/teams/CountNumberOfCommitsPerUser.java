package com.github.exadmin.ostm.collectors.impl.teams;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.api.GitHubRequest;
import com.github.exadmin.ostm.github.api.GitHubResponse;
import com.github.exadmin.ostm.github.api.HttpRequestBuilder;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.uimodel.*;

import java.nio.file.Path;
import java.util.List;

public class CountNumberOfCommitsPerUser extends AbstractCollector {
    private static final String GQL_QUERY_TEMPLATE = """
                {
                  user(login: "XXXXX") {
                    contributionsCollection(
                      organizationID: "O_kgDOBQbhhA"
                    ) {
                      contributionCalendar {
                        totalContributions
                      }
                    }
                  }
                }""";

    @Override
    public void collectDataInto(TheReportModel theReportModel, GitHubFacade gitHubFacade, Path parentPathForClonedRepositories) {
        List<String> uniqueLogins = gitHubFacade.getLoginsOfTheTeam();

        final TheColumn theColumn = theReportModel.findColumn(TheColumnId.COL_CONTRIBUTIONS_FOR_ALL_TIMES_ID);

        for (String login : uniqueLogins) {
            String newQuery = GQL_QUERY_TEMPLATE.replace("XXXXX", login);

            GitHubRequest request = HttpRequestBuilder
                    .gitHubGraphQLCall()
                    .useQuery(newQuery)
                    .build();

            GitHubResponse ghResponse = request.execute();

            Integer count = ghResponse.getObject("/data/user/contributionsCollection/contributionCalendar/totalContributions");
            if (count == null) count = 0;



            TheCellValue cellValue = new TheCellValue(count, count, SeverityLevel.INFO);
            String rowId = "row:" + login;

            theColumn.addValue(rowId, cellValue);
        }
    }
}
