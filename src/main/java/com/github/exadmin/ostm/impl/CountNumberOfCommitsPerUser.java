package com.github.exadmin.ostm.impl;

import com.github.exadmin.ostm.api.collector.BasicAbstractCollector;
import com.github.exadmin.ostm.api.github.GitHubContributorData;
import com.github.exadmin.ostm.api.github.GitHubFacade;
import com.github.exadmin.ostm.api.github.GitHubRepository;
import com.github.exadmin.ostm.api.github.graphql.GitHubGQLCaller;
import com.github.exadmin.ostm.api.github.rest.GitHubResponse;
import com.github.exadmin.ostm.api.model.TheCellValue;
import com.github.exadmin.ostm.api.model.TheColumn;
import com.github.exadmin.ostm.api.model.TheReportTable;
import com.github.exadmin.ostm.api.model.TheSheet;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CountNumberOfCommitsPerUser extends BasicAbstractCollector {
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
        final TheSheet theSheet = theReportTable.findSheet("sheet:team-summary", newSheet -> {
            newSheet.setTitle("Team Summary");
        });

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

        GitHubGQLCaller gqlCaller = new GitHubGQLCaller(gitHubFacade.getApplicationContext());

        for (String login : uniqueLogins) {
            String newQuery = gqlQuery.replace("XXXXX", login);
            GitHubResponse ghResponse = gqlCaller.doCall(newQuery, 8 * 60 * 60);

            List<Map<String, Object>> map = ghResponse.getDataMap();

            Integer count = 0;
            Map<String, Object> firstElement = map.getFirst();
            Map<String, Object> data = (Map<String, Object>) firstElement.get("data");
            if (data != null) {
                Map<String, Object> user = (Map<String, Object>) data.get("user");
                if (user != null) {
                    Map<String, Object> contrib = (Map<String, Object>) user.get("contributionsCollection");
                    Map<String, Object> calendar = (Map<String, Object>) contrib.get("contributionCalendar");
                    count = (Integer) calendar.get("totalContributions");
                }
            }

            TheCellValue cellValue = new TheCellValue("" + count);
            String rowId = "row:" + login;

            theColumn.addValue(rowId, cellValue);
        }
    }
}
