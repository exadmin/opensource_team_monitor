package com.github.exadmin.ostm.collectors.impl;

import com.github.exadmin.ostm.collectors.api.AbstractCollector;
import com.github.exadmin.ostm.github.api.GitHubRequest;
import com.github.exadmin.ostm.github.api.GitHubRequestBuilder;
import com.github.exadmin.ostm.github.api.GitHubResponse;
import com.github.exadmin.ostm.github.facade.GitHubContributorData;
import com.github.exadmin.ostm.github.facade.GitHubFacade;
import com.github.exadmin.ostm.github.facade.GitHubRepository;
import com.github.exadmin.ostm.model.TheCellValue;
import com.github.exadmin.ostm.model.TheColumn;
import com.github.exadmin.ostm.model.TheReportTable;
import com.github.exadmin.ostm.model.TheSheet;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CountNumberOfCommitsPerUser extends AbstractCollector {
    @Override
    public void collectDataInto(TheReportTable theReportTable, GitHubFacade gitHubFacade) {
        Set<String> uniqueLogins = new HashSet<>();

        List<GitHubRepository> allRepositories = gitHubFacade.getAllRepositories("Netcracker");
        for (GitHubRepository ghRepo : allRepositories) {
            List<GitHubContributorData> ghContributionDataList = gitHubFacade.getContributionsForRepository("Netcracker", ghRepo.getName());
            for (GitHubContributorData data: ghContributionDataList) {
                uniqueLogins.add(data.getLogin());
            }
        }

        // create report
        final TheSheet theSheet = theReportTable.findSheet("sheet:team-summary", newSheet -> newSheet.setTitle("Team Summary"));

        final TheColumn theColumn = theSheet.findColumn("column:contributions_count", newColumn -> {
            newColumn.setTitle("Contributions Count");
            newColumn.setCssClassName(TheColumn.TD_LEFT_MIDDLE);
            newColumn.setRenderingOrder(10);
        });

        // play at: https://docs.github.com/ru/graphql/overview/explorer
        String gqlQuery = "{\n" +
                "  user(login: \"XXXXX\") {\n" +
                "    contributionsCollection(\n" +
                "      organizationID: \"O_kgDOBQbhhA\"\n" +
                "    ) {\n" +
                "      contributionCalendar {\n" +
                "        totalContributions               \n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        for (String login : uniqueLogins) {
            String newQuery = gqlQuery.replace("XXXXX", login);
            // GitHubResponse ghResponse = gqlCaller.doCall(newQuery, 8 * 60 * 60);

            GitHubRequest request = GitHubRequestBuilder
                    .graphQL()
                    .useQuery(newQuery)
                    .build();

            GitHubResponse ghResponse = request.execute();

            List<Map<String, Object>> map = ghResponse.getDataMap();

            Integer count = ghResponse.getSingleValue("data", "user", "contributionsCollection", "contributionCalendar", "totalContributions");
            if (count == null) count = 0;

            TheCellValue cellValue = new TheCellValue("" + count);
            String rowId = "row:" + login;

            theColumn.addValue(rowId, cellValue);
        }
    }
}
